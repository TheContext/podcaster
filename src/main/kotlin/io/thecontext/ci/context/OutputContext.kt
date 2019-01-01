package io.thecontext.ci.context

import io.thecontext.ci.output.*
import io.thecontext.ci.output.feed.FeedEpisodeRenderer
import io.thecontext.ci.output.feed.FeedRenderer
import io.thecontext.ci.output.website.WebsiteRenderer

interface OutputContext : Context {

    val outputWriter: OutputWriter

    class Impl(context: Context) : OutputContext, Context by context {

        private val htmlRenderer by lazy { HtmlRenderer.Impl() }
        private val templateRenderer by lazy { TemplateRenderer.Impl() }
        private val textWriter by lazy { TextWriter.Impl() }

        private val markdownEpisodeRenderer by lazy { MarkdownEpisodeRenderer.Impl(templateRenderer, ioScheduler) }

        private val feedEpisodeRenderer by lazy { FeedEpisodeRenderer.Impl(markdownEpisodeRenderer, htmlRenderer, ioScheduler) }
        private val feedRenderer by lazy { FeedRenderer.Impl(feedEpisodeRenderer, templateRenderer, time, ioScheduler) }
        private val websiteRenderer by lazy { WebsiteRenderer.Impl(markdownEpisodeRenderer) }

        override val outputWriter by lazy { OutputWriter.Impl(feedRenderer, websiteRenderer, textWriter, ioScheduler) }
    }
}