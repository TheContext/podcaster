package io.thecontext.ci.output

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.dsl.specification.Specification.*
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.thecontext.ci.*
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Podcast
import org.junit.runner.RunWith

@RunWith(Spectrum::class)
class EpisodeXmlFormatterSpec {
    init {
        val env by memoized { Environment() }

        val person = testPerson
        val episode = testEpisode

        context("regular episode") {

            val summaryLine = "html summary line"

            beforeEach {
                env.markdownRenderer.renderHtmlResult = listOf(summaryLine, summaryLine, summaryLine).joinLines()
            }

            it("formats") {
                val expected = """
                    <title>${episode.title}</title>
                    <pubDate>${episode.date}</pubDate>
                    <guid>${episode.file.url}</guid>
                    <link>${episode.url}</link>
                    <enclosure url="${episode.file.url}" length="${episode.file.length}" type="audio/mpeg"/>
                    <atom:link rel="replies" type="text/html" href="${episode.discussionUrl}"/>
                    <itunes:duration>${episode.duration}</itunes:duration>
                    <atom:contributor>
                    $INDENT<atom:name>${person.name}</atom:name>
                    </atom:contributor>
                    <itunes:summary>
                    $INDENT<![CDATA[
                    $INDENT$summaryLine
                    $INDENT$summaryLine
                    $INDENT$summaryLine
                    $INDENT]]>
                    </itunes:summary>
                    """

                env.formatter.format(testPodcast, episode)
                        .test()
                        .assertResult(expected.trimIndent())
            }
        }
    }

    private class Environment {
        val markdownFormatter = TestEpisodeMarkdownFormatter()
        val markdownRenderer = TestMarkdownRenderer()

        val formatter = EpisodeXmlFormatter.Impl(
                markdownFormatter = markdownFormatter,
                markdownRenderer = markdownRenderer,
                ioScheduler = Schedulers.trampoline()
        )
    }

    private class TestEpisodeMarkdownFormatter : EpisodeMarkdownFormatter {
        override fun format(podcast: Podcast, episode: Episode) = Single.just("episode")
    }

    private class TestMarkdownRenderer : MarkdownRenderer {
        var renderHtmlResult = "html"

        override fun renderHtml(markdown: String) = renderHtmlResult
    }
}