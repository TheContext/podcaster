package io.thecontext.ci.output.website

import io.reactivex.Scheduler
import io.reactivex.Single
import io.thecontext.ci.output.TemplateRenderer
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Person
import io.thecontext.ci.value.Podcast
import io.thecontext.ci.value.find

interface WebsiteRenderer {
    fun render(podcast: Podcast, episode: Episode, people: List<Person>): Single<String>

    class Impl(
            private val templateRenderer: TemplateRenderer,
            private val ioScheduler: Scheduler
    ) : WebsiteRenderer {

        override fun render(podcast: Podcast, episode: Episode, people: List<Person>): Single<String> = Single.fromCallable {
            val contents = mapOf(
                    "title" to episode.title,
                    "discussion_url" to episode.discussionUrl,
                    "description" to episode.description,
                    "mp3File" to episode.file.url,
                    "guests" to episode.people.guestIds.map { people.find(it) }.map { mapOf("guest" to formatPerson(it)) },
                    "hosts" to episode.people.hostIds.map { people.find(it) }.map { mapOf("host" to formatPerson(it)) },
                    "notes" to episode.notesMarkdown
            )

            templateRenderer.render(TemplateRenderer.Template.WebsiteEpisode, contents)
        }.subscribeOn(ioScheduler)


        private fun formatPerson(person: Person): String {
            val twitterLink = person.twitter?.let {
                formatLink("Twitter", "https://twitter.com/$it")
            }

            val githubLink = person.github?.let {
                formatLink("GitHub", "https://github.com/$it")
            }

            val links = listOfNotNull(twitterLink, githubLink) + person.links.map { formatLink(it.name, it.url) }

            return if (links.isEmpty()) {
                person.name
            } else {
                "${person.name}: ${links.joinToString(separator = ", ")}"
            }
        }

        private fun formatLink(title: String, url: String) = "[$title]($url)"
    }
}