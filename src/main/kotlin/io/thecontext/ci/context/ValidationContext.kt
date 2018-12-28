package io.thecontext.ci.context

import io.thecontext.ci.validation.EpisodeListValidator
import io.thecontext.ci.validation.EpisodeValidator
import io.thecontext.ci.validation.PodcastValidator
import io.thecontext.ci.validation.UrlValidator
import io.thecontext.ci.value.Person

interface ValidationContext : Context {

    val podcastValidator: PodcastValidator
    val episodeValidator: EpisodeValidator
    val episodesValidator: EpisodeListValidator

    class Impl(context: Context, people: List<Person>) : ValidationContext, Context by context {

        private val urlValidator by lazy { UrlValidator(ioScheduler) }

        override val podcastValidator by lazy { PodcastValidator(urlValidator, people) }
        override val episodeValidator by lazy { EpisodeValidator(urlValidator, people) }
        override val episodesValidator by lazy { EpisodeListValidator(ioScheduler) }
    }
}