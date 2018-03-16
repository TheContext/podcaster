package io.thecontext.ci.input

import io.reactivex.Scheduler
import io.reactivex.Single
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Podcast
import java.io.File

interface InputReader {

    sealed class Result {
        data class Success(val podcast: Podcast, val episodes: List<Episode>) : Result()
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
                    val people = yamlReader.readPeople(peopleFile)
                            .distinctBy { it.id }

                    val podcast = yamlReader.readPodcast(podcastFile).let { podcast ->
                        val owners = podcast.peopleIds.owners.map { ownerId ->
                            people.find { it.id == ownerId }
                                    ?: return@fromCallable Result.Failure("Owner [$ownerId] is not defined in [${peopleFile.name}].")
                        }

                        val authors = podcast.peopleIds.authors.map { authorId ->
                            people.find { it.id == authorId }
                                    ?: return@fromCallable Result.Failure("Author [$authorId] is not defined in [${peopleFile.name}].")
                        }

                        podcast.copy(people = Podcast.People(owners = owners, authors = authors))
                    }

                    val episodes = episodeFiles
                            .map { (episodeFile, episodeDescriptionFile) ->
                                val episode = yamlReader.readEpisode(episodeFile)
                                val episodeSlug = episodeFile.parentFile.name
                                val episodeDescription = textReader.read(episodeDescriptionFile)

                                episode.copy(slug = episodeSlug) to episodeDescription
                            }
                            .map { (episode, episodeDescription) ->
                                val hosts = episode.peopleIds.hosts.map { hostId ->
                                    people.find { it.id == hostId }
                                            ?: return@fromCallable Result.Failure("Host [$hostId] for episode [${episode.number}] is not defined in [${peopleFile.name}].")
                                }

                                val guests = episode.peopleIds.guests.map { guestId ->
                                    people.find { it.id == guestId }
                                            ?: return@fromCallable Result.Failure("Guest [$guestId] for episode [${episode.number}] is not defined in [${peopleFile.name}].")
                                }

                                episode.copy(
                                        people = episode.people.copy(hosts = hosts, guests = guests),
                                        notes = episode.notes.copy(descriptionMarkdown = episodeDescription)
                                )
                            }

                    Result.Success(podcast, episodes)
                }
                .subscribeOn(ioScheduler)
    }
}
