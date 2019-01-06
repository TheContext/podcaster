package io.thecontext.ci.context

import io.thecontext.ci.validation.*
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Person
import io.thecontext.ci.value.Podcast

interface ValidationContext : Context {

    val podcastValidator: Validator<Podcast>
    val episodeValidator: Validator<Episode>
    val episodesValidator: Validator<List<Episode>>

    class Impl(context: Context, people: List<Person>) : ValidationContext, Context by context {

        private val markdownValidator: Validator<String> by lazy { MarkdownValidator(ioScheduler) }
        private val urlValidator: Validator<String> by lazy { UrlValidator(ioScheduler) }

        override val podcastValidator by lazy { PodcastValidator(urlValidator, people) }
        override val episodeValidator by lazy { EpisodeValidator(markdownValidator, urlValidator, people, time) }
        override val episodesValidator by lazy { EpisodesValidator(ioScheduler) }
    }
}