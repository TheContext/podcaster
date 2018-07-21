package io.thecontext.ci.validation

import io.reactivex.Single
import io.thecontext.ci.toDate
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Person
import java.time.format.DateTimeParseException

class EpisodeValidator(
        private val urlValidator: Validator<String>,
        private val people: List<Person>
) : Validator<Episode> {

    companion object {
        const val MAXIMUM_DESCRIPTION_LENGTH = 200
    }

    override fun validate(value: Episode): Single<ValidationResult> {
        val episodeIdentifierForError = value.title
        val urlResults = emptyList<String>()
                .plus(value.url)
                .plus(value.discussionUrl)
                .plus(value.file.url)
                .map { urlValidator.validate(it) }

        val peopleResults = emptyList<String>()
                .plus(value.people.hostIds)
                .plus(value.people.guestIds)
                .map { personId ->
                    Single.fromCallable {
                        if (people.find { it.id == personId } == null) {
                            ValidationResult.Failure("$episodeIdentifierForError: Person [$personId] is not defined.")
                        } else {
                            ValidationResult.Success
                        }
                    }
                }

        val guidResult = Single.fromCallable {
            if (value.guid.isEmpty()){
                ValidationResult.Failure("$episodeIdentifierForError: guid is empty")
            }

            if (value.guid.contains(' ')){
                ValidationResult.Failure("$episodeIdentifierForError: guid contains white space")
            }

            ValidationResult.Success
        }

        val numberResult = Single.fromCallable {
            if (value.number < 0) {
                ValidationResult.Failure("$episodeIdentifierForError: Episode number is negative.")
            } else {
                ValidationResult.Success
            }
        }

        val dateResult = Single.fromCallable {
            try {
                value.date.toDate()

                ValidationResult.Success
            } catch (e: DateTimeParseException) {
                ValidationResult.Failure("$episodeIdentifierForError: Episode date is in wrong format. Should be YYYY-MM-DD.")
            }
        }

        val fileLengthResult = Single.fromCallable {
            if (value.file.length < 0) {
                ValidationResult.Failure("$episodeIdentifierForError: File length is negative.")
            } else {
                ValidationResult.Success
            }
        }

        val descriptionResult = Single.fromCallable {
            if (value.description.length > MAXIMUM_DESCRIPTION_LENGTH) {
                ValidationResult.Failure("$episodeIdentifierForError: Description length is [${value.description.length}] symbols but should less than [${MAXIMUM_DESCRIPTION_LENGTH}].")
            } else {
                ValidationResult.Success
            }
        }

        return Single
                .merge(urlResults + peopleResults + listOf(guidResult, numberResult, dateResult, fileLengthResult, descriptionResult))
                .toList()
                .map { it.merge() }
    }
}
