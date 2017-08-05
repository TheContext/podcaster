package io.thecontext.ci.serializer

import io.thecontext.ci.model.Episode
import io.thecontext.ci.model.ItunesConfig
import io.thecontext.ci.model.Person
import okio.BufferedSink
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


fun writeRssFeed(unsafeSink: BufferedSink, now: LocalDateTime, itunes: ItunesConfig, hosts: List<Person>, episodes: List<Episode>) {

    unsafeSink.use { sink ->

        val estFormatter = DateTimeFormatter.ofPattern("EEE, dd MMMM yyyy hh:mm:ss Z")


        sink.writeUtf8("""<?xml version="1.0" encoding="utf-8"?>""").writeUtf8("\n")
                .writeUtf8("""<rss xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd" xmlns:atom="http://www.w3.org/2005/Atom" version="2.0">""").writeUtf8("\n")
                .writeUtf8("<channel>\n")
                .writeUtf8("\t<title>${itunes.title}</title>\n")
                .writeUtf8("\t<link>${itunes.link}</link>\n")
                .writeUtf8("\t<description>${itunes.description}</description>\n")
                .writeUtf8("\t<language>${itunes.language}</language>\n")
                .writeUtf8("\t<lastBuildDate>${now}</lastBuildDate>\n")
                .writeUtf8("\t<itunes:owner>\n")
                .writeUtf8("\t\t<itunes:email>artem.zinnatullin@gmail.com</itunes:email>\n")
                .writeUtf8("\t\t<itunes:name>Artem Zinnatullin</itunes:name>\n")
                .writeUtf8("\t</itunes:owner>\n")

        hosts.forEach { host ->
            sink.writeUtf8("\t<atom:author>\n")
                    .writeUtf8("\t\t<atom:name>${host.name}</atom:name>\n")

            host.websiteLink?.apply { sink.writeUtf8("\t\t<atom:uri>${host.websiteLink.url}</atom:uri>\n") }
//            host.email?.apply { sink.writeUtf8("\t\t<atom:email>${host.email}</atom:email>\n") }

            sink.writeUtf8("\t</atom:author>\n")
        }

        sink.writeUtf8("\t<itunes:author>${itunes.author}</itunes:author>\n")
                .writeUtf8("\t<itunes:explicit>no</itunes:explicit>\n")
                .writeUtf8("\t<itunes:keywords>${itunes.keywords}</itunes:keywords>\n")
                .writeUtf8("\t<itunes:subtitle>${itunes.subtitle}</itunes:subtitle>\n")
                .writeUtf8("\t<itunes:category text=\"Technology\">\n")

        itunes.categories.forEach {
            sink.writeUtf8("\t\t<itunes:category text=\"$it\"/>\n")
        }

        sink.writeUtf8("\t</itunes:category>\n")
                .writeUtf8("\t<itunes:image href=\"${itunes.imageUrl}\"/>\n")



        episodes.forEach { episode ->

            val markdownParser = Parser.builder().build()
            val renderer = HtmlRenderer.builder().build()
            val document = markdownParser.parse(episode.showNotes)

            /*
            val showNotesDir = Files.createDirectories(Paths.get("show_notes/"))
            val showNotesFile = File("$showNotesDir/${episode.showNotesFileName()}")
            Okio.buffer(Okio.sink(showNotesFile)).use {
                it.writeUtf8(episode.showNotes).flush()
            }
            */

            sink.writeUtf8("\t<item>\n")
                    .writeUtf8("\t\t<title>${episode.title}</title>\n")
                    .writeUtf8("\t\t<<pubDate>${episode.releaseDate}</<pubDate>\n")
                    .writeUtf8("\t\t<itunes:summary>\n")
                    .writeUtf8("\t\t\t<![CDATA[\n")
                    .writeUtf8(renderer.render(document))
                    .writeUtf8("\t\t\t]]>\n")
                    .writeUtf8("\t</item>\n")
        }


        sink.writeUtf8("</channel>\n")
                .writeUtf8("</rss>")
                .flush()
    }
}