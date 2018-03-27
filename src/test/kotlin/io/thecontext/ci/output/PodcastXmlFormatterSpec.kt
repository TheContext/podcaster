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
class PodcastXmlFormatterSpec {
    init {
        val env by memoized { Environment() }

        val podcast = testPodcast
        val episode = testEpisode

        context("regular podcast") {

            val episodeLine = "episode line"

            beforeEach {
                env.episodeFormatter.formatResult = listOf(episodeLine, episodeLine, episodeLine).joinLines()
            }

            it("formats") {
                val expected = """
                    <?xml version="1.0" encoding="utf-8"?>
                    <rss xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd" xmlns:atom="http://www.w3.org/2005/Atom" version="2.0">
                    <channel>
                    $INDENT<title>${podcast.title}</title>
                    $INDENT<language>${podcast.language.code.toLowerCase()}-${podcast.language.regionCode.toLowerCase()}</language>
                    $INDENT<link>${podcast.url}</link>
                    $INDENT<atom:link rel="self" type="application/rss+xml" href="${podcast.feedUrl}"/>
                    $INDENT<itunes:subtitle>${podcast.subtitle}</itunes:subtitle>
                    $INDENT<itunes:summary>${podcast.summary}</itunes:summary>
                    $INDENT<itunes:image href="${podcast.artworkUrl}"/>
                    $INDENT<itunes:explicit>${if (podcast.explicit) "yes" else "no"}</itunes:explicit>
                    $INDENT<itunes:category text="${podcast.category}">
                    $INDENT$INDENT<itunes:category text="${podcast.subcategory}"/>
                    $INDENT</itunes:category>
                    $INDENT<itunes:keywords>${podcast.keywords.joinToString(separator = ",")}</itunes:keywords>
                    $INDENT<itunes:owner>
                    $INDENT$INDENT<itunes:name>${podcast.people.owners.first().name}</itunes:name>
                    $INDENT</itunes:owner>
                    $INDENT<atom:author>
                    $INDENT$INDENT<atom:name>${podcast.people.authors.first().name}</atom:name>
                    $INDENT</atom:author>
                    $INDENT<item>
                    $INDENT$INDENT$episodeLine
                    $INDENT$INDENT$episodeLine
                    $INDENT$INDENT$episodeLine
                    $INDENT</item>
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
        val episodeFormatter = TestEpisodeXmlFomatter()

        val formatter = PodcastXmlFormatter.Impl(
                episodeFormatter = episodeFormatter,
                ioScheduler = Schedulers.trampoline()
        )
    }

    class TestEpisodeXmlFomatter : EpisodeXmlFormatter {
        var formatResult = "episode"

        override fun format(podcast: Podcast, episode: Episode) = Single.just(formatResult)
    }
}