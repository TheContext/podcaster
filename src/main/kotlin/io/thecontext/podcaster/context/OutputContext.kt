package io.thecontext.podcaster.context

import io.thecontext.podcaster.output.*
import io.thecontext.podcaster.output.feed.FeedEpisodeRenderer
import io.thecontext.podcaster.output.feed.FeedRenderer
import io.thecontext.podcaster.output.website.WebsiteEpisodeRenderer

interface OutputContext : Context {

    val outputWriter: OutputWriter

    class Impl(context: Context) : OutputContext, Context by context {

        private val htmlRenderer: HtmlRenderer by lazy { HtmlRenderer.Impl() }
        private val templateRenderer: TemplateRenderer by lazy { TemplateRenderer.Impl() }
        private val textWriter: TextWriter by lazy { TextWriter.Impl() }

        private val markdownEpisodeRenderer: MarkdownEpisodeRenderer by lazy { MarkdownEpisodeRenderer.Impl(templateRenderer, ioScheduler) }

        private val feedEpisodeRenderer: FeedEpisodeRenderer by lazy { FeedEpisodeRenderer.Impl(markdownEpisodeRenderer, htmlRenderer, ioScheduler) }
        private val feedRenderer: FeedRenderer by lazy { FeedRenderer.Impl(feedEpisodeRenderer, templateRenderer, time, ioScheduler) }
        private val websiteEpisodeRenderer: WebsiteEpisodeRenderer by lazy { WebsiteEpisodeRenderer.Impl(markdownEpisodeRenderer) }

        override val outputWriter: OutputWriter by lazy { OutputWriter.Impl(feedRenderer, websiteEpisodeRenderer, textWriter, ioScheduler) }
    }
}