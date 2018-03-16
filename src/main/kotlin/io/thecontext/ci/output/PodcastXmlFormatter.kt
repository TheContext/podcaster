package io.thecontext.ci.output

import io.reactivex.Scheduler
import io.reactivex.Single
import io.thecontext.ci.INDENT
import io.thecontext.ci.joinLines
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Podcast

interface PodcastXmlFormatter {

    fun format(podcast: Podcast, episodes: List<Episode>): Single<String>

    class Impl(
            private val episodeFormatter: EpisodeXmlFormatter,
            private val ioScheduler: Scheduler
    ) : PodcastXmlFormatter {

        override fun format(podcast: Podcast, episodes: List<Episode>) = Single
                .merge(episodes.map { episode -> episodeFormatter.format(podcast, episode).map { episode to it } })
                .toList()
                .map {
                    it.sortedBy { (episode, _) -> episode.number }.map { (_, episodeXml) -> episodeXml }
                }
                .map { episodeXmls ->
                    val channelHeader = """
                        <title>${podcast.title}</title>
                        <language>${podcast.language.code.toLowerCase()}-${podcast.language.regionCode.toLowerCase()}</language>
                        <link>${podcast.url}</link>
                        <atom:link rel="self" type="application/rss+xml" href="${podcast.feedUrl}"/>
                        <itunes:subtitle>${podcast.subtitle}</itunes:subtitle>
                        <itunes:summary>${podcast.summary}</itunes:summary>
                        <itunes:image href="${podcast.artworkUrl}"/>
                        <itunes:explicit>${if (podcast.explicit) "yes" else "no"}</itunes:explicit>
                        <itunes:category text="${podcast.category}">
                        $INDENT<itunes:category text="${podcast.subcategory}"/>
                        </itunes:category>
                        <itunes:keywords>${podcast.keywords.joinToString(separator = ",")}</itunes:keywords>
                        """

                    val channelOwners = podcast.people.owners
                            .map { owner ->
                                """
                                <itunes:owner>
                                $INDENT<itunes:name>${owner.name}</itunes:name>
                                </itunes:owner>
                                """
                            }
                            .joinLines()

                    val channelAuthors = podcast.people.authors
                            .map { author ->
                                """
                                <atom:author>
                                $INDENT<atom:name>${author.name}</atom:name>
                                </atom:author>
                                """
                            }
                            .joinLines()

                    val channelEpisodes = episodeXmls
                            .map { episodeXml ->
                                emptyList<String>()
                                        .plus("<item>")
                                        .plus(episodeXml.prependIndent(INDENT))
                                        .plus("</item>")
                                        .joinLines()
                            }
                            .joinLines()

                    val channelContent = listOf(channelHeader, channelOwners, channelAuthors, channelEpisodes)
                            .map { it.trimIndent() }
                            .joinLines()

                    val header = """
                        <?xml version="1.0" encoding="utf-8"?>
                        <rss xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd" xmlns:atom="http://www.w3.org/2005/Atom" version="2.0">
                        <channel>
                        """
                    val footer = """
                        </channel>
                        </rss>
                        """

                    emptyList<String>()
                            .plus(header.trimIndent())
                            .plus(channelContent.prependIndent(INDENT))
                            .plus(footer.trimIndent())
                            .joinLines()
                }
                .subscribeOn(ioScheduler)
    }
}