package io.thecontext.ci.output.website

import io.reactivex.Single
import io.thecontext.ci.output.MarkdownEpisodeRenderer
import io.thecontext.ci.output.TemplateRenderer.Template
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Person

interface WebsiteEpisodeRenderer {

    fun render(episode: Episode, people: List<Person>): Single<String>

    class Impl(
            private val markdownEpisodeRenderer: MarkdownEpisodeRenderer
    ) : WebsiteEpisodeRenderer {

        override fun render(episode: Episode, people: List<Person>) = markdownEpisodeRenderer.render(Template.WebsiteEpisode, episode, people)
    }
}