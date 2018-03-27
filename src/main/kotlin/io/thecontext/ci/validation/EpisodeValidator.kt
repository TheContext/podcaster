package io.thecontext.ci.validation

import io.reactivex.Single
import io.thecontext.ci.value.Episode

class EpisodeValidator(
        private val urlValidator: Validator<String>
) : Validator<Episode> {

    override fun validate(value: Episode): Single<ValidationResult> {
        val urls = listOf(value.url, value.discussionUrl, value.file.url) + value.notes.links.map { it.url }

        val urlsResult = Single
                .merge(urls.map { urlValidator.validate(it) })
                .toList()
                .map { it.merge() }

        val numberResult = Single
                .fromCallable {
                    if (value.number < 0) {
                        ValidationResult.Failure("Episode number is negative.")
                    } else {
                        ValidationResult.Success
                    }
                }

        val fileLengthResult = Single
                .fromCallable {
                    if (value.file.length < 0) {
                        ValidationResult.Failure("File length is negative.")
                    } else {
                        ValidationResult.Success
                    }
                }

        return Single
                .merge(listOf(urlsResult, numberResult, fileLengthResult))
                .toList()
                .map { it.merge() }
    }
}
