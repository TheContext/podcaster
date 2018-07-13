package io.thecontext.ci.input

import io.reactivex.Scheduler
import io.reactivex.Single
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Person
import io.thecontext.ci.value.Podcast
import java.io.File

interface InputReader {

    sealed class Result {
        data class Success(val podcast: Podcast, val episodes: List<Episode>, val people: List<Person>) : Result()
        data class Failure(val message: String) : Result()
    }

    fun read(peopleFile: File, podcastFile: File, episodeFiles: Map<File, File>): Single<Result>

    class Impl(
            private val yamlReader: YamlReader,
            private val textReader: TextReader,
            private val ioScheduler: Scheduler
    ) : InputReader {

        override fun read(peopleFile: File, podcastFile: File, episodeFiles: Map<File, File>) = Single
                .fromCallable {
                    val people = yamlReader.readPeople(peopleFile).distinctBy { it.id }
                    val podcast = yamlReader.readPodcast(podcastFile)

                    podcast.people.ownerIds.forEach { ownerId ->
                        if (people.find { it.id == ownerId } == null) {
                            return@fromCallable Result.Failure("Owner [$ownerId] is not defined in [${peopleFile.name}].")
                        }
                    }

                    podcast.people.authorIds.map { authorId ->
                        if (people.find { it.id == authorId } == null) {
                            return@fromCallable Result.Failure("Author [$authorId] is not defined in [${peopleFile.name}].")
                        }
                    }

                    val episodes = episodeFiles.map { (episodeFile, episodeDescriptionFile) ->
                        val episode = yamlReader.readEpisode(episodeFile)
                        val episodeSlug = episodeFile.parentFile.name
                        val episodeDescription = textReader.read(episodeDescriptionFile)

                        episode.copy(slug = episodeSlug, notes = episode.notes.copy(descriptionMarkdown = episodeDescription))
                    }

                    episodes.forEach { episode ->
                        episode.people.hostIds.map { hostId ->
                            if (people.find { it.id == hostId } == null) {
                                return@fromCallable Result.Failure("Host [$hostId] for episode [${episode.number}] is not defined in [${peopleFile.name}].")
                            }
                        }

                        episode.people.guestIds.map { guestId ->
                            if (people.find { it.id == guestId } == null) {
                                return@fromCallable Result.Failure("Guest [$guestId] for episode [${episode.number}] is not defined in [${peopleFile.name}].")
                            }
                        }
                    }

                    Result.Success(podcast, episodes, people)
                }
                .subscribeOn(ioScheduler)
    }
}
