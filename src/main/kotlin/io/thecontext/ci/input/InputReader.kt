package io.thecontext.ci.input

import io.reactivex.Scheduler
import io.reactivex.Single
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Person
import io.thecontext.ci.value.Podcast
import java.io.File

interface InputReader {

    data class Result(val podcast: Podcast, val episodes: List<Episode>, val people: List<Person>)

    fun read(peopleFile: File, podcastFile: File, episodeFiles: Map<File, File>): Single<Result>

    class Impl(
            private val yamlReader: YamlReader,
            private val textReader: TextReader,
            private val ioScheduler: Scheduler
    ) : InputReader {

        override fun read(peopleFile: File, podcastFile: File, episodeFiles: Map<File, File>) = Single
                .fromCallable {
                    val podcast = yamlReader.readPodcast(podcastFile)

                    val episodes = episodeFiles.map { (episodeFile, episodeDescriptionFile) ->
                        val episode = yamlReader.readEpisode(episodeFile)
                        val episodeSlug = episodeFile.parentFile.name
                        val episodeDescription = textReader.read(episodeDescriptionFile)

                        episode.copy(slug = episodeSlug, notes = episode.notes.copy(descriptionMarkdown = episodeDescription))
                    }

                    val people = yamlReader.readPeople(peopleFile).distinctBy { it.id }

                    Result(podcast, episodes, people)
                }
                .subscribeOn(ioScheduler)
    }
}
