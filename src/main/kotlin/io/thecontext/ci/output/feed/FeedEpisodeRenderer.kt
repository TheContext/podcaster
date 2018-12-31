package io.thecontext.ci.output.feed

import io.reactivex.Scheduler
import io.reactivex.Single
import io.thecontext.ci.output.TemplateRenderer
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Person
import io.thecontext.ci.value.Podcast
import io.thecontext.ci.value.find

interface FeedEpisodeRenderer {

    fun render(podcast: Podcast, episode: Episode, people: List<Person>): Single<String>

    class Impl(
            private val templateRenderer: TemplateRenderer,
            private val ioScheduler: Scheduler
    ) : FeedEpisodeRenderer {

        override fun render(podcast: Podcast, episode: Episode, people: List<Person>) = Single
                .fromCallable {
                    val guestIds = episode.people.guestIds ?: emptyList()
                    val hostIds = episode.people.hostIds

                    val contents = mapOf(
                            "description" to episode.description,
                            "notes" to episode.notesMarkdown,
                            "guests_available" to guestIds.isNotEmpty(),
                            "guests" to guestIds.map { people.find(it) }.map { mapOf("guest" to formatPerson(it)) },
                            "hosts_available" to hostIds.isNotEmpty(),
                            "hosts" to hostIds.map { people.find(it) }.map { mapOf("host" to formatPerson(it)) },
                            "discussion_url" to episode.discussionUrl
                    )

                    templateRenderer.render(TemplateRenderer.Template.FeedEpisode, contents)
                }
                .subscribeOn(ioScheduler)

        private fun formatPerson(person: Person): String {
            val twitterLink = person.twitter?.let { Person.Link("Twitter", "https://twitter.com/$it") }
            val githubLink = person.github?.let { Person.Link("GitHub", "https://github.com/$it") }

            val links = listOfNotNull(twitterLink, githubLink) + person.links

            return if (links.isEmpty()) {
                person.name
            } else {
                "${person.name}: ${links.map { formatLink(it.name, it.url) }.joinToString(separator = ", ")}"
            }
        }

        private fun formatLink(title: String, url: String) = "[$title]($url)"
    }
}