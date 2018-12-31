package io.thecontext.ci.output.feed

import io.reactivex.Scheduler
import io.reactivex.Single
import io.thecontext.ci.Time
import io.thecontext.ci.output.HtmlRenderer
import io.thecontext.ci.output.TemplateRenderer
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Person
import io.thecontext.ci.value.Podcast
import io.thecontext.ci.value.find

interface FeedRenderer {

    fun render(podcast: Podcast, episodes: List<Episode>, people: List<Person>): Single<String>

    class Impl(
            private val feedEpisodeRenderer: FeedEpisodeRenderer,
            private val htmlRenderer: HtmlRenderer,
            private val templateRenderer: TemplateRenderer,
            private val time: Time,
            private val ioScheduler: Scheduler
    ) : FeedRenderer {

        override fun render(podcast: Podcast, episodes: List<Episode>, people: List<Person>) = Single
                .concat(episodes.sortedBy { time.parseIso(it.time) }.map { episode ->
                    feedEpisodeRenderer.render(podcast, episode, people).map { episode to it }
                })
                .toList()
                .map { preparedEpisodes ->
                    val contents = mapOf(
                            "title" to podcast.title,
                            "language" to podcast.language.toLowerCase(),
                            "url" to podcast.url,
                            "description" to podcast.description,
                            "artwork_url" to podcast.artworkUrl,
                            "explicit" to if (podcast.explicit) "yes" else "no",
                            "category" to podcast.category,
                            "subcategory" to podcast.subcategory,
                            "owner_name" to people.find(podcast.people.ownerId).name,
                            "owner_email" to people.find(podcast.people.ownerId).email!!,
                            "authors" to podcast.people.authorIds.map { people.find(it).name }.joinToString(),
                            "build_date" to time.formatRfc2822(time.current()),
                            "episodes" to preparedEpisodes.map { (episode, episodeMarkdown) ->
                                val episodeTitle = if (episode.part == null) {
                                    "Episode ${episode.number}: ${episode.title}"
                                } else {
                                    "Episode ${episode.number}, Part ${episode.part}: ${episode.title}"
                                }

                                mapOf(
                                        "id" to episode.id,
                                        "title" to episodeTitle,
                                        "description" to episode.description,
                                        "date" to time.formatRfc2822(time.parseIso(episode.time)),
                                        "file_url" to episode.file.url,
                                        "file_length" to episode.file.length,
                                        "url" to episode.url,
                                        "discussion_url" to episode.discussionUrl,
                                        "duration" to episode.duration,
                                        "summary" to htmlRenderer.render(episodeMarkdown).trim().prependIndent(" ".repeat(10))
                                )
                            }
                    )

                    templateRenderer.render(TemplateRenderer.Template.Feed, contents)
                }
                .subscribeOn(ioScheduler)
    }
}
