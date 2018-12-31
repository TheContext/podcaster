package io.thecontext.ci.output.feed

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.dsl.specification.Specification.context
import com.greghaskins.spectrum.dsl.specification.Specification.it
import io.reactivex.schedulers.Schedulers
import io.thecontext.ci.*
import io.thecontext.ci.output.HtmlRenderer
import io.thecontext.ci.output.TemplateRenderer
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(Spectrum::class)
class FeedRendererSpec {
    init {
        val env by memoized { Environment() }

        val podcast = testPodcast
        val people = listOf(testPerson, testPerson)

        val episode1 = testEpisode.copy(number = 1, part = null, date = "2000-01-01")
        val episode2Part1 = testEpisode.copy(number = 2, part = 1, date = "2000-01-02")
        val episode2Part2 = testEpisode.copy(number = 2, part = 2, date = "2000-01-03")

        context("regular podcast") {

            it("renders") {
                val expected = """
                    <?xml version="1.0" encoding="utf-8"?>
                    <rss version="2.0" xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd" xmlns:content="http://purl.org/rss/1.0/modules/content/">
                      <channel>
                        <title>${podcast.title}</title>
                        <description>${podcast.description}</description>
                        <language>${podcast.language.toLowerCase()}</language>
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
                          <title>Episode ${episode1.number}: ${episode1.title}</title>
                          <description>${episode1.description}</description>
                          <pubDate>${episode1.date.toDate().toRfc2822()}</pubDate>
                          <guid>${episode1.id}</guid>
                          <link>${episode1.url}</link>
                          <enclosure url="${episode1.file.url}" length="${episode1.file.length}" type="audio/mpeg"/>
                          <itunes:duration>${episode1.duration}</itunes:duration>
                          <content:encoded>
                            <![CDATA[
                              ${env.markdownRenderer.renderResult}
                            ]]>
                          </content:encoded>
                        </item>
                        <item>
                          <title>Episode ${episode2Part1.number}, Part ${episode2Part1.part}: ${episode2Part1.title}</title>
                          <description>${episode2Part1.description}</description>
                          <pubDate>${episode2Part1.date.toDate().toRfc2822()}</pubDate>
                          <guid>${episode2Part1.id}</guid>
                          <link>${episode2Part1.url}</link>
                          <enclosure url="${episode2Part1.file.url}" length="${episode2Part1.file.length}" type="audio/mpeg"/>
                          <itunes:duration>${episode2Part1.duration}</itunes:duration>
                          <content:encoded>
                            <![CDATA[
                              ${env.markdownRenderer.renderResult}
                            ]]>
                          </content:encoded>
                        </item>
                        <item>
                          <title>Episode ${episode2Part2.number}, Part ${episode2Part2.part}: ${episode2Part2.title}</title>
                          <description>${episode2Part2.description}</description>
                          <pubDate>${episode2Part2.date.toDate().toRfc2822()}</pubDate>
                          <guid>${episode2Part2.id}</guid>
                          <link>${episode2Part2.url}</link>
                          <enclosure url="${episode2Part2.file.url}" length="${episode2Part2.file.length}" type="audio/mpeg"/>
                          <itunes:duration>${episode2Part2.duration}</itunes:duration>
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
                env.renderer.render(podcast, listOf(episode2Part2, episode1, episode2Part1), people)
                        .test()
                        .assertResult(expected.trimIndent())
            }
        }
    }

    class Environment {
        val markdownRenderer = TestHtmlRenderer()

        val renderer = FeedRenderer.Impl(
                feedEpisodeRenderer = FeedEpisodeRenderer.Impl(TemplateRenderer.Impl(), Schedulers.trampoline()),
                htmlRenderer = markdownRenderer,
                templateRenderer = TemplateRenderer.Impl(),
                ioScheduler = Schedulers.trampoline()
        )
    }

    class TestHtmlRenderer : HtmlRenderer {
        var renderResult = "html"

        override fun render(markdown: String) = renderResult
    }
}