package io.thecontext.ci.output

import io.reactivex.Scheduler
import io.reactivex.Single
import io.thecontext.ci.joinLines
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Person
import io.thecontext.ci.value.Podcast

interface EpisodeMarkdownFormatter {

    fun format(podcast: Podcast, episode: Episode): Single<String>

    class Impl(
            private val ioScheduler: Scheduler
    ) : EpisodeMarkdownFormatter {

        override fun format(podcast: Podcast, episode: Episode) = Single
                .fromCallable {
                    """
                    # ${episode.title}

                    * ${formatLink("How to listen and subscribe", podcast.url)}
                    * ${formatLink("Discussion after the episode", episode.discussionUrl)}

                    ${episode.notes.descriptionMarkdown}

                    #### Guests

                    ${formatList(episode.people.guests.map { formatPerson(it) })}

                    #### Hosts

                    ${formatList(episode.people.hosts.map { formatPerson(it) })}

                    #### Links

                    ${formatList(episode.notes.links.map { formatLink(it.title, it.url) })}
                    """
                }
                .map { it.trimIndent() }
                .subscribeOn(ioScheduler)

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

        private fun formatList(items: List<String>) =  items.map { "* $it" }.joinLines()
        private fun formatLink(title: String, url: String) = "[$title]($url)"
    }
}