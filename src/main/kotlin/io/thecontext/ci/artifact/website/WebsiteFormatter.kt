package io.thecontext.ci.artifact.website

import io.reactivex.Scheduler
import io.reactivex.Single
import io.thecontext.ci.artifact.MustacheRenderer
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Person
import io.thecontext.ci.value.Podcast

private const val TEMPLATE_RESOURCE_NAME = "website.md.mustache"

interface WebsiteFormatter {
    fun format(podcast: Podcast, episode: Episode): Single<String>

    class Impl(
            private val mustacheRenderer: MustacheRenderer,
            private val ioScheduler: Scheduler
    ) : WebsiteFormatter {

        override fun format(podcast: Podcast, episode: Episode): Single<String> = Single.fromCallable {
            val contents = mapOf(
                    "title" to episode.title,
                    "discussion_url" to episode.discussionUrl,
                    "description" to episode.notes.descriptionMarkdown,
                    "mp3File" to episode.file.url,
                    "guests" to episode.people.guests.map { mapOf("guest" to formatPerson(it)) },
                    "hosts" to episode.people.hosts.map { mapOf("host" to formatPerson(it)) },
                    "links" to episode.notes.links.map { mapOf("link" to formatLink(it.title, it.url)) }
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