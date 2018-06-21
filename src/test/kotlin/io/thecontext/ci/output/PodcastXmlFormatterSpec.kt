package io.thecontext.ci.output

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.dsl.specification.Specification.context
import com.greghaskins.spectrum.dsl.specification.Specification.it
import io.reactivex.schedulers.Schedulers
import io.thecontext.ci.*
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(Spectrum::class)
class PodcastXmlFormatterSpec {
    init {
        val env by memoized { Environment() }

        val podcast = testPodcast
        val episode = testEpisode

        context("regular podcast") {

            it("formats") {
                val expected = """
                    <?xml version="1.0" encoding="utf-8"?>
                    <rss xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd" xmlns:atom="http://www.w3.org/2005/Atom" version="2.0">
                      <channel>
                        <title>${podcast.title}</title>
                        <description>${podcast.summary}</description>
                        <language>${podcast.language.code.toLowerCase()}-${podcast.language.regionCode.toLowerCase()}</language>
                        <link>${podcast.url}</link>
                        <lastBuildDate>${LocalDate.now().toRfc2822()}</lastBuildDate>
                        <atom:link rel="self" type="application/rss+xml" href="${podcast.feedUrl}"/>
                        <itunes:subtitle>${podcast.subtitle}</itunes:subtitle>
                        <itunes:summary>${podcast.summary}</itunes:summary>
                        <itunes:image href="${podcast.artworkUrl}"/>
                        <itunes:explicit>${if (podcast.explicit) "yes" else "no"}</itunes:explicit>
                        <itunes:category text="${podcast.category}">
                          <itunes:category text="${podcast.subcategory}"/>
                        </itunes:category>
                        <itunes:keywords>${podcast.keywords.joinToString(separator = ",")}</itunes:keywords>
                        <itunes:owner>
                          <itunes:name>${podcast.people.owners[0].name}</itunes:name>
                        </itunes:owner>
                        <itunes:owner>
                          <itunes:name>${podcast.people.owners[1].name}</itunes:name>
                        </itunes:owner>
                        <atom:author>
                          <atom:name>${podcast.people.authors[0].name}</atom:name>
                        </atom:author>
                        <atom:author>
                          <atom:name>${podcast.people.authors[1].name}</atom:name>
                        </atom:author>
                        <item>
                          <title>${episode.title}</title>
                          <pubDate>${episode.date.toDate().toRfc2822()}</pubDate>
                          <guid>${episode.file.url}</guid>
                          <link>${episode.url}</link>
                          <enclosure url="${episode.file.url}" length="${episode.file.length}" type="audio/mpeg"/>
                          <atom:link rel="replies" type="text/html" href="${episode.discussionUrl}"/>
                          <itunes:duration>${episode.duration}</itunes:duration>
                          <atom:author>
                            <atom:name>${episode.people.hosts[0].name}</atom:name>
                          </atom:author>
                          <atom:author>
                            <atom:name>${episode.people.hosts[1].name}</atom:name>
                          </atom:author>
                          <atom:contributor>
                            <atom:name>${episode.people.guests[0].name}</atom:name>
                          </atom:contributor>
                          <atom:contributor>
                            <atom:name>${episode.people.guests[1].name}</atom:name>
                          </atom:contributor>
                          <itunes:summary>
                            <![CDATA[
                              ${env.markdownRenderer.renderResult}
                            ]]>
                          </itunes:summary>
                        </item>
                      </channel>
                    </rss>

                    """

                // Note: Mustache inserts EOL in the end. It is simulated here using an empty line.
                env.formatter.format(podcast, listOf(episode))
                        .test()
                        .assertResult(expected.trimIndent())
            }
        }
    }

    class Environment {
        val markdownRenderer = TestMarkdownRenderer()

        val formatter = PodcastXmlFormatter.Impl(
                episodeMarkdownFormatter = EpisodeMarkdownFormatter.Impl(MustacheRenderer.Impl(), Schedulers.trampoline()),
                markdownRenderer = markdownRenderer,
                mustacheRenderer = MustacheRenderer.Impl(),
                ioScheduler = Schedulers.trampoline()
        )
    }

    class TestMarkdownRenderer : MarkdownRenderer {
        var renderResult = "html"

        override fun renderHtml(markdown: String) = renderResult
    }
}