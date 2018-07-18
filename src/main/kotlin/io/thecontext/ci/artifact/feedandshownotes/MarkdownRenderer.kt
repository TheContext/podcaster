package io.thecontext.ci.artifact.feedandshownotes

import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

interface MarkdownRenderer {

    fun renderHtml(markdown: String): String

    class Impl : MarkdownRenderer {

        private val markdownParser by lazy { Parser.builder().build() }
        private val markdownRenderer by lazy { HtmlRenderer.builder().build() }

        override fun renderHtml(markdown: String) = markdownRenderer.render(markdownParser.parse(markdown))
    }
}