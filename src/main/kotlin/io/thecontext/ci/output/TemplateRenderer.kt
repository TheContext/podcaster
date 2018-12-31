package io.thecontext.ci.output

import com.github.mustachejava.DefaultMustacheFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringWriter

interface TemplateRenderer {

    enum class Template(val resourcePath: String) {
        Feed("feed/feed.rss.mustache"),
        FeedEpisode("feed/feed-episode.md.mustache"),
        WebsiteEpisode("website/episode.md.mustache"),
    }

    fun render(template: Template, contents: Map<String, Any>): String

    class Impl : TemplateRenderer {

        private val factory by lazy { DefaultMustacheFactory() }

        override fun render(template: Template, contents: Map<String, Any>): String {
            val reader = BufferedReader(InputStreamReader(this.javaClass.getResourceAsStream("/${template.resourcePath}")))
            val mustache = factory.compile(reader, template.resourcePath)

            return with(StringWriter()) {
                mustache.execute(this, contents)

                toString()
            }
        }
    }
}