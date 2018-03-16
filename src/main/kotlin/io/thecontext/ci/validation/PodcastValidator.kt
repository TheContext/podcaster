package io.thecontext.ci.validation

import io.reactivex.Single
import io.thecontext.ci.value.Podcast

class PodcastValidator(
        private val urlValidator: Validator<String>
) : Validator<Podcast> {

    override fun validate(value: Podcast): Single<ValidationResult> {
        val urls = listOf(value.url, value.artworkUrl, value.feedUrl)

        val urlsResult = Single
                .merge(urls.map { urlValidator.validate(it) })
                .toList()
                .map { it.merge() }

        return urlsResult
    }
}
