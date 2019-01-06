package io.thecontext.podcaster.output.website

import io.reactivex.Single
import io.thecontext.podcaster.output.MarkdownEpisodeRenderer
import io.thecontext.podcaster.output.TemplateRenderer.Template
import io.thecontext.podcaster.value.Episode
import io.thecontext.podcaster.value.Person

interface WebsiteEpisodeRenderer {

    fun render(episode: Episode, people: List<Person>): Single<String>

    class Impl(
            private val markdownEpisodeRenderer: MarkdownEpisodeRenderer
    ) : WebsiteEpisodeRenderer {

        override fun render(episode: Episode, people: List<Person>) = markdownEpisodeRenderer.render(Template.WebsiteEpisode, episode, people)
    }
}