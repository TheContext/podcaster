package io.thecontext.ci.output

import io.reactivex.Scheduler
import io.reactivex.Single
import io.thecontext.ci.INDENT
import io.thecontext.ci.joinLines
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Podcast

interface EpisodeXmlFormatter {

    fun format(podcast: Podcast, episode: Episode): Single<String>

    class Impl(
            private val markdownFormatter: EpisodeMarkdownFormatter,
            private val markdownRenderer: MarkdownRenderer,
            private val ioScheduler: Scheduler
    ) : EpisodeXmlFormatter {

        override fun format(podcast: Podcast, episode: Episode) = markdownFormatter.format(podcast, episode)
                .map { episodeMarkdown ->
                    val header = """
                        <title>${episode.title}</title>
                        <pubDate>${episode.date}</pubDate>
                        <guid>${episode.file.url}</guid>
                        <link>${episode.url}</link>
                        <enclosure url="${episode.file.url}" length="${episode.file.length}" type="audio/mpeg"/>
                        <atom:link rel="replies" type="text/html" href="${episode.discussionUrl}"/>
                        <itunes:duration>${episode.duration}</itunes:duration>
                        """

                    val guests = episode.people.guests
                            .map {
                                """
                                <atom:contributor>
                                $INDENT<atom:name>${it.name}</atom:name>
                                </atom:contributor>
                                """
                            }
                            .joinLines()

                    val summary = markdownRenderer.renderHtml(episodeMarkdown).let {
                        val summaryHeader = """
                            <itunes:summary>
                            $INDENT<![CDATA[
                            """

                        val summaryFooter = """
                            $INDENT]]>
                            </itunes:summary>
                            """

                        emptyList<String>()
                                .plus(summaryHeader.trimIndent())
                                .plus(it.prependIndent(INDENT))
                                .plus(summaryFooter.trimIndent())
                                .joinLines()
                    }

                    listOf(header, guests, summary)
                            .map { it.trimIndent() }
                            .joinLines()
                }
                .subscribeOn(ioScheduler)
    }
}