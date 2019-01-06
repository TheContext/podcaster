package io.thecontext.podcaster.output

import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.thecontext.podcaster.output.feed.FeedRenderer
import io.thecontext.podcaster.output.website.WebsiteEpisodeRenderer
import io.thecontext.podcaster.value.Episode
import io.thecontext.podcaster.value.Person
import io.thecontext.podcaster.value.Podcast
import java.io.File

interface OutputWriter {

    fun write(feedFile: File, websiteDirectory: File, podcast: Podcast, episodes: List<Episode>, people: List<Person>): Single<Unit>

    class Impl(
            private val feedRenderer: FeedRenderer,
            private val websiteEpisodeRenderer: WebsiteEpisodeRenderer,
            private val textWriter: TextWriter,
            private val ioScheduler: Scheduler
    ) : OutputWriter {

        override fun write(feedFile: File, websiteDirectory: File, podcast: Podcast, episodes: List<Episode>, people: List<Person>): Single<Unit> {
            val feed = feedRenderer.render(podcast, episodes, people)
                    .flatMap { podcastXml ->
                        Single.fromCallable {
                            feedFile.parentFile.mkdirs()

                            textWriter.write(feedFile, podcastXml)
                        }
                    }
                    .map { Unit }

            val website = Single
                    .merge(episodes.map { episode -> websiteEpisodeRenderer.render(episode, people).map { episode to it } })
                    .toList()
                    .flatMap {
                        websiteDirectory.mkdirs()
                        val operations = it.map { (episode, episodeWebsiteMarkdown) ->
                            Single.fromCallable {
                                textWriter.write(File(websiteDirectory, "${episode.slug}.md"), episodeWebsiteMarkdown)
                            }
                        }

                        Single.merge(operations).toList()
                    }
                    .map { Unit }

            return Single.zip(feed, website, BiFunction<Unit, Unit, Unit> { _, _ -> Unit }).subscribeOn(ioScheduler)
        }
    }
}