package io.thecontext.ci.output

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.dsl.specification.Specification.context
import com.greghaskins.spectrum.dsl.specification.Specification.it
import io.reactivex.schedulers.Schedulers
import io.thecontext.ci.testEpisode
import io.thecontext.ci.testPerson
import io.thecontext.ci.testPodcast
import io.thecontext.ci.value.Person
import org.junit.runner.RunWith
import kotlin.math.exp

@RunWith(Spectrum::class)
class WebsiteFormatterSpec {
    init {
        val formatter = WebsiteFormatter.Impl(
                mustacheRenderer = MustacheRenderer.Impl(),
                ioScheduler = Schedulers.trampoline()
        )

        val host1 = Person(
                id = "host1",
                github = "github1",
                name = "Person 1",
                site = "person1.com",
                twitter = "person1"
        )

        val host2 = Person(
                id = "host2",
                github = "github2",
                name = "Person 2",
                site = "person2.com",
                twitter = "person2"
        )

        val guest1 = Person(
                id = "guest1",
                github = "github3",
                name = "Person 3",
                site = "person3.com",
                twitter = "person3"
        )

        val guest2 = Person(
                id = "guest2",
                github = "github4",
                name = "Person 4",
                site = "person4.com",
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

            it("formats website") {
                val expected = """
                    ---
                    layout: episode
                    title: "${episode.title}"
                    description: "${episode.description}"
                    tags: []
                    mp3: "${episode.file.url}"
                    ---

                    ## Guests

                    * ${guest1.name}: [Twitter](https://twitter.com/${guest1.twitter}), [GitHub](https://github.com/${guest1.github}), [website](${guest1.site})
                    * ${guest2.name}: [Twitter](https://twitter.com/${guest2.twitter}), [GitHub](https://github.com/${guest2.github}), [website](${guest2.site})

                    ## Hosts

                    * ${host1.name}: [Twitter](https://twitter.com/${host1.twitter}), [GitHub](https://github.com/${host1.github}), [website](${host1.site})
                    * ${host2.name}: [Twitter](https://twitter.com/${host2.twitter}), [GitHub](https://github.com/${host2.github}), [website](${host2.site})

                    ## Notes

                    ${episode.notesMarkdown}

                    ## Discussion after the show
                    We use Github issue tracker for discussion. Do you wanna join our discussion? [Click here](${episode.discussionUrl})
                    """

                println(expected.trimIndent())
                println("--------------")
                println( formatter.format(podcast, episode, people).blockingGet())

                // Note: Mustache inserts EOL in the end. It is simulated here using an empty line.
                formatter.format(podcast, episode, people)
                        .test()
                        .assertResult(expected.trimIndent())
            }
        }
    }
}