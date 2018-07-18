package io.thecontext.ci.artifact.feedandshownotes

import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.thecontext.ci.artifact.ArtifactGenerationResult
import io.thecontext.ci.artifact.ArtifactGenerator
import io.thecontext.ci.artifact.DeployableArtifact
import io.thecontext.ci.artifact.TextWriter
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Podcast
import java.io.File

private const val FEED = "podcast.rss"

class FeedAndShowNotesArtifactGenerator(
        private val rssFormatter: RssFormatter,
        private val episodeMarkdownFormatter: EpisodeMarkdownFormatter,
        private val textWriter: TextWriter,
        private val ioScheduler: Scheduler,
        private val directory: File
) : ArtifactGenerator {
    // TODO write tests

    override fun generateArtifact(podcast: Podcast, episodes: List<Episode>): Single<ArtifactGenerationResult> {

        val notes = Single
                .merge(episodes.map { episode -> episodeMarkdownFormatter.format(podcast, episode).map { episode to it } })
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

        val feed = rssFormatter.format(podcast, episodes)
                .flatMap { podcastXml ->
                    Single.fromCallable {
                        directory.mkdirs()

                        textWriter.write(File(directory, FEED), podcastXml)
                    }
                }
                .map { Unit }

        return Single.zip(notes, feed, BiFunction<Unit, Unit, ArtifactGenerationResult> { _, _ ->
            ArtifactGenerationResult.Success(DeployableArtifact.FolderArtifact(directory))
        }).subscribeOn(ioScheduler)
    }
}
