package io.thecontext.ci.output.feed

import io.reactivex.Scheduler
import io.reactivex.Single
import io.thecontext.ci.output.HtmlRenderer
import io.thecontext.ci.output.MarkdownEpisodeRenderer
import io.thecontext.ci.output.TemplateRenderer.Template
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Person

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