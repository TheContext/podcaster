package io.thecontext.ci.validation

import io.reactivex.Single
import io.thecontext.ci.toDate
import io.thecontext.ci.value.Episode
import java.time.format.DateTimeParseException

class EpisodeValidator(
        private val urlValidator: Validator<String>
) : Validator<Episode> {

    override fun validate(value: Episode): Single<ValidationResult> {
        val urlResults = emptyList<String>()
                .plus(value.url)
                .plus(value.discussionUrl)
                .plus(value.file.url)
                .plus(value.notes.links.map { it.url })
                .map { urlValidator.validate(it) }

        val numberResult = Single.fromCallable {
            if (value.number < 0) {
                ValidationResult.Failure("Episode number is negative.")
            } else {
                ValidationResult.Success
            }
        }

        val dateResult = Single.fromCallable {
            try {
                value.date.toDate()

                ValidationResult.Success
            } catch (e: DateTimeParseException) {
                ValidationResult.Failure("Episode date is in wrong format. Should be YYYY-MM-DD.")
            }
        }

        val fileLengthResult = Single.fromCallable {
            if (value.file.length < 0) {
                ValidationResult.Failure("File length is negative.")
            } else {
                ValidationResult.Success
            }
        }

        return Single
                .merge(urlResults + listOf(numberResult, dateResult, fileLengthResult))
                .toList()
                .map { it.merge() }
    }
}
