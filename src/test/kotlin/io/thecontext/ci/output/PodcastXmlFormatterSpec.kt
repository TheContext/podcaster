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
        val people = listOf(testPerson, testPerson)

        context("regular podcast") {

            it("formats") {
                val expected = """
                    <?xml version="1.0" encoding="utf-8"?>
                    <rss version="2.0" xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd" xmlns:content="http://purl.org/rss/1.0/modules/content/">
                      <channel>
                        <title>${podcast.title}</title>
                        <description>${podcast.description}</description>
                        <language>${podcast.language.code.toLowerCase()}-${podcast.language.regionCode.toLowerCase()}</language>
                        <link>${podcast.url}</link>
                        <lastBuildDate>${LocalDate.now().toRfc2822()}</lastBuildDate>
                        <itunes:image href="${podcast.artworkUrl}"/>
                        <itunes:explicit>${if (podcast.explicit) "yes" else "no"}</itunes:explicit>
                        <itunes:category text="${podcast.category}">
                          <itunes:category text="${podcast.subcategory}"/>
                        </itunes:category>
                        <itunes:owner>
                          <itunes:name>${people.first().name}</itunes:name>
                          <itunes:email>${people.first().email}</itunes:email>
                        </itunes:owner>
                        <itunes:author>${people.map { it.name }.joinToString()}</itunes:author>
                        <item>
                          <title>${episode.title}</title>
                          <description>${episode.description}</description>
                          <pubDate>${episode.date.toDate().toRfc2822()}</pubDate>
                          <guid>${episode.id}</guid>
                          <link>${episode.url}</link>
                          <enclosure url="${episode.file.url}" length="${episode.file.length}" type="audio/mpeg"/>
                          <itunes:duration>${episode.duration}</itunes:duration>
                          <content:encoded>
                            <![CDATA[
                              ${env.markdownRenderer.renderResult}
                            ]]>
                          </content:encoded>
                        </item>
                      </channel>
                    </rss>

                    """

                // Note: Mustache inserts EOL in the end. It is simulated here using an empty line.
                env.formatter.format(podcast, listOf(episode), people)
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