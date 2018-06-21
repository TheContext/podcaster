package io.thecontext.ci.output

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.dsl.specification.Specification.context
import com.greghaskins.spectrum.dsl.specification.Specification.it
import io.reactivex.schedulers.Schedulers
import io.thecontext.ci.testEpisode
import io.thecontext.ci.testPodcast
import org.junit.runner.RunWith

@RunWith(Spectrum::class)
class EpisodeMarkdownFormatterSpec {
    init {
        val formatter = EpisodeMarkdownFormatter.Impl(
                mustacheRenderer = MustacheRenderer.Impl(),
                ioScheduler = Schedulers.trampoline()
        )

        val podcast = testPodcast
        val episode = testEpisode

        context("regular episode") {

            it("formats") {
                val expected = """
                    # ${episode.title}

                    * [How to listen and subscribe](${podcast.url})
                    * [Discussion after the episode](${episode.discussionUrl})

                    ${episode.notes.descriptionMarkdown}

                    ## Guests

                    * ${episode.people.guests[0].name}: [Twitter](https://twitter.com/${episode.people.guests[0].twitter}), [GitHub](https://github.com/${episode.people.guests[0].github}), [website](${episode.people.guests[0].site})
                    * ${episode.people.guests[1].name}: [Twitter](https://twitter.com/${episode.people.guests[1].twitter}), [GitHub](https://github.com/${episode.people.guests[1].github}), [website](${episode.people.guests[1].site})

                    ## Hosts

                    * ${episode.people.hosts[0].name}: [Twitter](https://twitter.com/${episode.people.hosts[0].twitter}), [GitHub](https://github.com/${episode.people.hosts[0].github}), [website](${episode.people.hosts[0].site})
                    * ${episode.people.hosts[1].name}: [Twitter](https://twitter.com/${episode.people.hosts[1].twitter}), [GitHub](https://github.com/${episode.people.hosts[1].github}), [website](${episode.people.hosts[1].site})

                    ## Links

                    * [${episode.notes.links.first().title}](${episode.notes.links.first().url})

                    """

                // Note: Mustache inserts EOL in the end. It is simulated here using an empty line.
                formatter.format(podcast, episode)
                        .test()
                        .assertResult(expected.trimIndent())
            }
        }
    }
}