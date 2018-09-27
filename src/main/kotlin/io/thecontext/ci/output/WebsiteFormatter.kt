package io.thecontext.ci.output

import io.reactivex.Scheduler
import io.reactivex.Single
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Person
import io.thecontext.ci.value.Podcast
import io.thecontext.ci.value.find

private const val TEMPLATE_RESOURCE_NAME = "website.md.mustache"

interface WebsiteFormatter {
    fun format(podcast: Podcast, episode: Episode, people: List<Person>): Single<String>

    class Impl(
            private val mustacheRenderer: MustacheRenderer,
            private val ioScheduler: Scheduler
    ) : WebsiteFormatter {

        override fun format(podcast: Podcast, episode: Episode, people: List<Person>): Single<String> = Single.fromCallable {
            val contents = mapOf(
                    "title" to episode.title,
                    "discussion_url" to episode.discussionUrl,
                    "description" to episode.description,
                    "mp3File" to episode.file.url,
                    "guests" to episode.people.guestIds.map { people.find(it) }.map { mapOf("guest" to formatPerson(it)) },
                    "hosts" to episode.people.hostIds.map { people.find(it) }.map { mapOf("host" to formatPerson(it)) },
                    "notes" to episode.notesMarkdown
            )

            mustacheRenderer.render(TEMPLATE_RESOURCE_NAME, contents)
        }.subscribeOn(ioScheduler)


        private fun formatPerson(person: Person): String {
            val twitterLink = person.twitter?.let {
                formatLink("Twitter", "https://twitter.com/$it")
            }

            val githubLink = person.github?.let {
                formatLink("GitHub", "https://github.com/$it")
            }

            val siteLink = person.site?.let {
                formatLink("website", it)
            }

            val links = listOfNotNull(twitterLink, githubLink, siteLink)

            return if (links.isEmpty()) {
                person.name
            } else {
                "${person.name}: ${links.joinToString(separator = ", ")}"
            }
        }

        private fun formatLink(title: String, url: String) = "[$title]($url)"
    }
}