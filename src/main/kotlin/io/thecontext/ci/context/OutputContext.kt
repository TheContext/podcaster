package io.thecontext.ci.context

import io.thecontext.ci.output.*
import io.thecontext.ci.output.feed.FeedEpisodeRenderer
import io.thecontext.ci.output.feed.FeedRenderer
import io.thecontext.ci.output.website.WebsiteRenderer

interface OutputContext : Context {

    val outputWriter: OutputWriter

    class Impl(context: Context) : OutputContext, Context by context {

        private val markdownRenderer by lazy { HtmlRenderer.Impl() }
        private val mustacheRenderer by lazy { TemplateRenderer.Impl() }
        private val textWriter by lazy { TextWriter.Impl() }

        private val feedEpisodeRenderer by lazy { FeedEpisodeRenderer.Impl(mustacheRenderer, ioScheduler) }
        private val feedRenderer by lazy { FeedRenderer.Impl(feedEpisodeRenderer, markdownRenderer, mustacheRenderer, ioScheduler) }
        private val websiteRenderer by lazy { WebsiteRenderer.Impl(mustacheRenderer, ioScheduler) }

        override val outputWriter by lazy { OutputWriter.Impl(feedRenderer, feedEpisodeRenderer, websiteRenderer, textWriter, ioScheduler) }
    }
}