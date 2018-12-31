package io.thecontext.ci.output.website

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
class WebsiteRendererSpec {
    init {
        val renderer = WebsiteRenderer.Impl(
                templateRenderer = TemplateRenderer.Impl(),
                ioScheduler = Schedulers.trampoline()
        )

        val host1 = testPerson.copy(
                id = "host1",
                github = "github1",
                name = "Person 1",
                twitter = "person1"
        )

        val host2 = testPerson.copy(
                id = "host2",
                github = "github2",
                name = "Person 2",
                twitter = "person2"
        )

        val guest1 = testPerson.copy(
                id = "guest1",
                github = "github3",
                name = "Person 3",
                twitter = "person3"
        )

        val guest2 = testPerson.copy(
                id = "guest2",
                github = "github4",
                name = "Person 4",
                twitter = "person4"
        )

        val podcast = testPodcast
        val episode = testEpisode.copy(
                people = testEpisode.people.copy(
                        hostIds = listOf(host1.id, host2.id),
                        guestIds = listOf(guest1.id, guest2.id)
                )
        )
        val people = listOf(host1, host2, guest1, guest2)

        context("regular episode") {

            it("renders") {
                val expected = """
                    ---
                    layout: episode
                    title: "${episode.title}"
                    description: "${episode.description}"
                    tags: []
                    mp3: "${episode.file.url}"
                    ---

                    ## Guests

                    * ${guest1.name}: [Twitter](https://twitter.com/${guest1.twitter}), [GitHub](https://github.com/${guest1.github}), [${guest1.links.first().name}](${guest1.links.first().url})
                    * ${guest2.name}: [Twitter](https://twitter.com/${guest2.twitter}), [GitHub](https://github.com/${guest2.github}), [${guest2.links.first().name}](${guest2.links.first().url})

                    ## Hosts

                    * ${host1.name}: [Twitter](https://twitter.com/${host1.twitter}), [GitHub](https://github.com/${host1.github}), [${host1.links.first().name}](${host1.links.first().url})
                    * ${host2.name}: [Twitter](https://twitter.com/${host2.twitter}), [GitHub](https://github.com/${host2.github}), [${host2.links.first().name}](${host2.links.first().url})

                    ## Notes

                    ${episode.notesMarkdown}

                    ## Discussion after the show
                    We use Github issue tracker for discussion. Do you wanna join our discussion? [Click here](${episode.discussionUrl})
                    """

                // Note: Mustache inserts EOL in the end. It is simulated here using an empty line.
                renderer.render(podcast, episode, people)
                        .test()
                        .assertResult(expected.trimIndent())
            }
        }
    }
}