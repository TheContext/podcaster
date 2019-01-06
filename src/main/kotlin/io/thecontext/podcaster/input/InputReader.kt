package io.thecontext.podcaster.input

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import io.reactivex.Scheduler
import io.reactivex.Single
import io.thecontext.podcaster.value.Episode
import io.thecontext.podcaster.value.Person
import io.thecontext.podcaster.value.Podcast
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
                    val podcast = try {
                        yamlReader.readPodcast(podcastFile)
                    } catch (e: MissingKotlinParameterException) {
                        return@fromCallable Result.Failure("Podcast YAML is missing value for [${e.parameter.name}].")
                    }

                    val episodes = episodeFiles.map { (episodeFile, episodeNotesFile) ->
                        val episodeSlug = episodeFile.parentFile.name
                        val episodeNotes = textReader.read(episodeNotesFile)

                        val episode = try {
                            yamlReader.readEpisode(episodeFile)
                        } catch (e: MissingKotlinParameterException) {
                            return@fromCallable Result.Failure("Episode YAML [$episodeSlug] is missing value for [${e.parameter.name}].")
                        }

                        episode.copy(slug = episodeSlug, notesMarkdown = episodeNotes)
                    }

                    val people = try {
                        yamlReader.readPeople(peopleFile).distinctBy { it.id }
                    } catch (e: MissingKotlinParameterException) {
                        return@fromCallable Result.Failure("People YAML is missing value for [${e.parameter.name}].")
                    }

                    Result.Success(podcast, episodes, people)
                }
                .subscribeOn(ioScheduler)
    }
}
