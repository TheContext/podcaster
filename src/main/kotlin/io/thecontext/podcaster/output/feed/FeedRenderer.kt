package io.thecontext.podcaster.output.feed

import io.reactivex.Scheduler
import io.reactivex.Single
import io.thecontext.podcaster.Time
import io.thecontext.podcaster.output.TemplateRenderer
import io.thecontext.podcaster.value.Episode
import io.thecontext.podcaster.value.Person
import io.thecontext.podcaster.value.Podcast
import io.thecontext.podcaster.value.find

interface FeedRenderer {

    fun render(podcast: Podcast, episodes: List<Episode>, people: List<Person>): Single<String>

    class Impl(
            private val episodeRenderer: FeedEpisodeRenderer,
            private val templateRenderer: TemplateRenderer,
            private val time: Time,
            private val ioScheduler: Scheduler
    ) : FeedRenderer {

        override fun render(podcast: Podcast, episodes: List<Episode>, people: List<Person>) = Single
                .concat(episodes.sortedBy { time.parseIso(it.time) }.map { episode ->
                    episodeRenderer.render(episode, people).map { episode to it }
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
                            "episodes" to preparedEpisodes.map { (episode, episodeHtml) ->
                                mapOf(
                                        "id" to episode.id,
                                        "number" to episode.number,
                                        "part" to episode.part,
                                        "part_available" to (episode.part != null),
                                        "title" to episode.title,
                                        "description" to episode.description,
                                        "date" to time.formatRfc2822(time.parseIso(episode.time)),
                                        "file_url" to episode.file.url,
                                        "file_length" to episode.file.length,
                                        "discussion_url" to episode.discussionUrl,
                                        "duration" to episode.duration,
                                        "summary" to episodeHtml.trim().prependIndent(" ".repeat(10))
                                )
                            }
                    )

                    templateRenderer.render(TemplateRenderer.Template.Feed, contents)
                }
                .subscribeOn(ioScheduler)
    }
}
