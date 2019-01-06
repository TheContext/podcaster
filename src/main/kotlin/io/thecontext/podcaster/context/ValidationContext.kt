package io.thecontext.podcaster.context

import io.thecontext.podcaster.validation.*
import io.thecontext.podcaster.value.Episode
import io.thecontext.podcaster.value.Person
import io.thecontext.podcaster.value.Podcast

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