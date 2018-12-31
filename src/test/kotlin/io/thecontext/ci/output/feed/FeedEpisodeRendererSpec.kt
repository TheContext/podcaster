package io.thecontext.ci.output.feed

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.dsl.specification.Specification.context
import com.greghaskins.spectrum.dsl.specification.Specification.it
import io.reactivex.schedulers.Schedulers
import io.thecontext.ci.output.TemplateRenderer
import io.thecontext.ci.testEpisode
import io.thecontext.ci.testPerson
import io.thecontext.ci.testPodcast
import org.junit.runner.RunWith

@RunWith(Spectrum::class)
class FeedEpisodeRendererSpec {
    init {
        val renderer = FeedEpisodeRenderer.Impl(
                templateRenderer = TemplateRenderer.Impl(),
                ioScheduler = Schedulers.trampoline()
        )

        val podcast = testPodcast
        val episode = testEpisode
        val people = listOf(testPerson, testPerson)

        context("regular episode") {

            it("renders") {
                val expected = """
                    ${episode.description}

                    ${episode.notesMarkdown}

                    **Guests**

                    * ${people[0].name}: [Twitter](https://twitter.com/${people[0].twitter}), [GitHub](https://github.com/${people[0].github}), [${people[0].links.first().name}](${people[0].links.first().url})
                    * ${people[1].name}: [Twitter](https://twitter.com/${people[1].twitter}), [GitHub](https://github.com/${people[1].github}), [${people[1].links.first().name}](${people[1].links.first().url})

                    **Hosts**

                    * ${people[0].name}: [Twitter](https://twitter.com/${people[0].twitter}), [GitHub](https://github.com/${people[0].github}), [${people[0].links.first().name}](${people[0].links.first().url})
                    * ${people[1].name}: [Twitter](https://twitter.com/${people[1].twitter}), [GitHub](https://github.com/${people[1].github}), [${people[1].links.first().name}](${people[1].links.first().url})

                    **Discussion**

                    [Give us your opinion on the episode](${episode.discussionUrl})!

                    """

                // Note: Mustache inserts EOL in the end. It is simulated here using an empty line.
                renderer.render(podcast, episode, people)
                        .test()
                        .assertResult(expected.trimIndent())
            }
        }
    }
}