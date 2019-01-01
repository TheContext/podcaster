package io.thecontext.ci.output

import io.reactivex.Scheduler
import io.reactivex.Single
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Person
import io.thecontext.ci.value.find

interface MarkdownEpisodeRenderer {

    fun render(template: TemplateRenderer.Template, episode: Episode, people: List<Person>): Single<String>

    class Impl(
            private val templateRenderer: TemplateRenderer,
            private val ioScheduler: Scheduler
    ) : MarkdownEpisodeRenderer {

        override fun render(template: TemplateRenderer.Template, episode: Episode, people: List<Person>) = Single
                .fromCallable {
                    val guestIds = episode.people.guestIds ?: emptyList()
                    val hostIds = episode.people.hostIds

                    val contents = mapOf(
                            "number" to episode.number,
                            "part" to (episode.part ?: Int.MIN_VALUE),
                            "part_available" to (episode.part != null),
                            "title" to episode.title,
                            "description" to episode.description,
                            "notes" to episode.notesMarkdown,
                            "guests_available" to guestIds.isNotEmpty(),
                            "guests" to guestIds.map { people.find(it) }.map { mapOf("guest" to formatPerson(it)) },
                            "hosts_available" to hostIds.isNotEmpty(),
                            "hosts" to hostIds.map { people.find(it) }.map { mapOf("host" to formatPerson(it)) },
                            "discussion_url" to episode.discussionUrl,
                            "file_url" to episode.file.url
                    )

                    templateRenderer.render(template, contents)
                }
                .subscribeOn(ioScheduler)

        private fun formatPerson(person: Person): String {
            val twitterLink = person.twitter?.let { Person.Link("Twitter", "https://twitter.com/$it") }
            val githubLink = person.github?.let { Person.Link("GitHub", "https://github.com/$it") }

            val links = listOfNotNull(twitterLink, githubLink) + person.links

            return if (links.isEmpty()) {
                person.name
            } else {
                "${person.name}: ${links.map { "[${it.name}](${it.url})" }.joinToString(separator = ", ")}"
            }
        }
    }
}