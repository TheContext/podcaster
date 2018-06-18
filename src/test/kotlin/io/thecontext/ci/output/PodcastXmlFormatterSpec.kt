package io.thecontext.ci.output

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.dsl.specification.Specification.context
import com.greghaskins.spectrum.dsl.specification.Specification.it
import io.reactivex.schedulers.Schedulers
import io.thecontext.ci.memoized
import io.thecontext.ci.testEpisode
import io.thecontext.ci.testPodcast
import org.junit.runner.RunWith

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
                        <language>${podcast.language.code.toLowerCase()}-${podcast.language.regionCode.toLowerCase()}</language>
                        <link>${podcast.url}</link>
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
                          <itunes:name>${podcast.people.owners.first().name}</itunes:name>
                        </itunes:owner>
                        <atom:author>
                          <atom:name>${podcast.people.authors.first().name}</atom:name>
                        </atom:author>
                        <item>
                          <title>${episode.title}</title>
                          <pubDate>${episode.date}</pubDate>
                          <guid>${episode.file.url}</guid>
                          <link>${episode.url}</link>
                          <enclosure url="${episode.file.url}" length="${episode.file.length}" type="audio/mpeg"/>
                          <atom:link rel="replies" type="text/html" href="${episode.discussionUrl}"/>
                          <itunes:duration>${episode.duration}</itunes:duration>
                          <atom:contributor>
                            <atom:name>${episode.people.guests.first().name}</atom:name>
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