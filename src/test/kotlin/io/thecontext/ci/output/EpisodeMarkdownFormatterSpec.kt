package io.thecontext.ci.output

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.dsl.specification.Specification.context
import com.greghaskins.spectrum.dsl.specification.Specification.it
import io.reactivex.schedulers.Schedulers
import io.thecontext.ci.testEpisode
import io.thecontext.ci.testEpisodeLink
import io.thecontext.ci.testPerson
import io.thecontext.ci.testPodcast
import org.junit.runner.RunWith

@RunWith(Spectrum::class)
class EpisodeMarkdownFormatterSpec {
    init {
        val formatter = EpisodeMarkdownFormatter.Impl(Schedulers.trampoline())

        val podcast = testPodcast
        val person = testPerson
        val episodeLink = testEpisodeLink
        val episode = testEpisode

        context("regular episode") {

            it("formats") {
                val expected = """
                    # ${episode.title}

                    * [How to listen and subscribe](${podcast.url})
                    * [Discussion after the episode](${episode.discussionUrl})

                    ${episode.notes.descriptionMarkdown}

                    #### Guests

                    * ${person.name}: [Twitter](https://twitter.com/${person.twitter}), [GitHub](https://github.com/${person.github}), [website](${person.site})

                    #### Hosts

                    * ${person.name}: [Twitter](https://twitter.com/${person.twitter}), [GitHub](https://github.com/${person.github}), [website](${person.site})

                    #### Links

                    * [${episodeLink.title}](${episodeLink.url})
                    """

                formatter.format(podcast, episode)
                        .test()
                        .assertResult(expected.trimIndent())
            }
        }
    }
}