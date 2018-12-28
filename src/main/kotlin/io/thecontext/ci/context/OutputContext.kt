package io.thecontext.ci.context

import io.thecontext.ci.output.*

interface OutputContext : Context {

    val outputWriter: OutputWriter

    class Impl(context: Context) : OutputContext, Context by context {

        private val markdownRenderer by lazy { MarkdownRenderer.Impl() }
        private val mustacheRenderer by lazy { MustacheRenderer.Impl() }
        private val textWriter by lazy { TextWriter.Impl() }

        private val episodeMarkdownFormatter by lazy { EpisodeMarkdownFormatter.Impl(mustacheRenderer, ioScheduler) }
        private val podcastXmlFormatter by lazy { PodcastXmlFormatter.Impl(episodeMarkdownFormatter, markdownRenderer, mustacheRenderer, ioScheduler) }
        private val websiteFormatter by lazy { WebsiteFormatter.Impl(mustacheRenderer, ioScheduler) }

        override val outputWriter by lazy { OutputWriter.Impl(podcastXmlFormatter, episodeMarkdownFormatter, websiteFormatter, textWriter, ioScheduler) }
    }
}