package io.thecontext.podcaster.output.feed

import io.reactivex.Scheduler
import io.reactivex.Single
import io.thecontext.podcaster.output.HtmlRenderer
import io.thecontext.podcaster.output.MarkdownEpisodeRenderer
import io.thecontext.podcaster.output.TemplateRenderer.Template
import io.thecontext.podcaster.value.Episode
import io.thecontext.podcaster.value.Person

interface FeedEpisodeRenderer {

    fun render(episode: Episode, people: List<Person>): Single<String>

    class Impl(
            private val markdownEpisodeRenderer: MarkdownEpisodeRenderer,
            private val htmlRenderer: HtmlRenderer,
            private val ioScheduler: Scheduler
    ) : FeedEpisodeRenderer {

        override fun render(episode: Episode, people: List<Person>) = markdownEpisodeRenderer.render(Template.FeedEpisode, episode, people)
                .map { htmlRenderer.render(it) }
                .subscribeOn(ioScheduler)
    }
}