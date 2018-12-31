package io.thecontext.ci.output

import org.commonmark.parser.Parser

interface HtmlRenderer {

    fun render(markdown: String): String

    class Impl : HtmlRenderer {

        private val markdownParser by lazy { Parser.builder().build() }
        private val markdownRenderer by lazy { org.commonmark.renderer.html.HtmlRenderer.builder().build() }

        override fun render(markdown: String) = markdownRenderer.render(markdownParser.parse(markdown))
    }
}