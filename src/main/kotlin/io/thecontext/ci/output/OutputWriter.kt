package io.thecontext.ci.output

import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Person
import io.thecontext.ci.value.Podcast
import java.io.File

interface OutputWriter {

    object FileNames {
        const val FEED = "podcast.rss"
    }

    fun write(directory: File, podcast: Podcast, episodes: List<Episode>, people: List<Person>): Single<Unit>

    class Impl(
            private val podcastXmlFormatter: PodcastXmlFormatter,
            private val episodeMarkdownFormatter: EpisodeMarkdownFormatter,
            private val textWriter: TextWriter,
            private val ioScheduler: Scheduler
    ) : OutputWriter {

        override fun write(directory: File, podcast: Podcast, episodes: List<Episode>, people: List<Person>): Single<Unit> {
            val notes = Single
                    .merge(episodes.map { episode -> episodeMarkdownFormatter.format(podcast, episode, people).map { episode to it } })
                    .toList()
                    .flatMap {
                        val operations = it.map { (episode, episodeMarkdown) ->
                            Single.fromCallable {
                                directory.mkdirs()

                                textWriter.write(File(directory, "${episode.slug}.md"), episodeMarkdown)
                            }
                        }

                        Single.merge(operations).toList()
                    }
                    .map { Unit }

            val feed = podcastXmlFormatter.format(podcast, episodes, people)
                    .flatMap { podcastXml ->
                        Single.fromCallable {
                            directory.mkdirs()

                            textWriter.write(File(directory, FileNames.FEED), podcastXml)
                        }
                    }
                    .map { Unit }

            return Single.zip(notes, feed, BiFunction<Unit, Unit, Unit> { _, _ -> Unit }).subscribeOn(ioScheduler)
        }
    }
}