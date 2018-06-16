package io.thecontext.ci.output

import io.reactivex.Scheduler
import io.reactivex.Single
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Podcast

interface PodcastXmlFormatter {

    fun format(podcast: Podcast, episodes: List<Episode>): Single<String>

    class Impl(
            private val episodeMarkdownFormatter: EpisodeMarkdownFormatter,
            private val markdownRenderer: MarkdownRenderer,
            private val mustacheRenderer: MustacheRenderer,
            private val ioScheduler: Scheduler
    ) : PodcastXmlFormatter {

        companion object {
            private const val TEMPLATE_RESOURCE_NAME = "podcast.xml.mustache"
        }

        override fun format(podcast: Podcast, episodes: List<Episode>) = Single
                .merge(episodes.map { episode -> episodeMarkdownFormatter.format(podcast, episode).map { episode to it } })
                .toList()
                .map { episodes ->
                    val contents = mapOf(
                            "title" to podcast.title,
                            "language" to "${podcast.language.code.toLowerCase()}-${podcast.language.regionCode.toLowerCase()}",
                            "url" to podcast.url,
                            "feed_url" to podcast.feedUrl,
                            "subtitle" to podcast.subtitle,
                            "summary" to podcast.summary,
                            "artwork_url" to podcast.artworkUrl,
                            "explicit" to if (podcast.explicit) "yes" else "no",
                            "category" to podcast.category,
                            "subcategory" to podcast.subcategory,
                            "keywords" to podcast.keywords.joinToString(separator = ","),
                            "owners" to podcast.people.owners.map { mapOf("name" to it.name) },
                            "authors" to podcast.people.authors.map { mapOf("name" to it.name) },
                            "episodes" to episodes.map { (episode, episodeMarkdown) ->
                                mapOf(
                                        "title" to episode.title,
                                        "date" to episode.date,
                                        "file_url" to episode.file.url,
                                        "file_length" to episode.file.length,
                                        "url" to episode.url,
                                        "discussion_url" to episode.discussionUrl,
                                        "duration" to episode.duration,
                                        "guests" to episode.people.guests.map { mapOf("name" to it.name) },
                                        "summary" to markdownRenderer.renderHtml(episodeMarkdown).trim().prependIndent(" ".repeat(10))
                                )
                            }
                    )

                    mustacheRenderer.render(TEMPLATE_RESOURCE_NAME, contents)
                }
                .subscribeOn(ioScheduler)
    }
}