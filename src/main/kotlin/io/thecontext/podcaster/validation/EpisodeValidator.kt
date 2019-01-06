package io.thecontext.podcaster.validation

import io.reactivex.Single
import io.thecontext.podcaster.Time
import io.thecontext.podcaster.value.Episode
import io.thecontext.podcaster.value.Person
import java.time.format.DateTimeParseException

class EpisodeValidator(
        private val markdownValidator: Validator<String>,
        private val urlValidator: Validator<String>,
        private val people: List<Person>,
        private val time: Time
) : Validator<Episode> {

    companion object {
        const val MAXIMUM_DESCRIPTION_LENGTH = 200
    }

    override fun validate(value: Episode): Single<ValidationResult> {
        val failureLocation = "Episode ${value.slug}"

        val urlResults = listOf(value.file.url, value.discussionUrl)
                .map {
                    urlValidator.validate(it)
                            .map {
                                when (it) {
                                    ValidationResult.Success -> it
                                    is ValidationResult.Failure -> it.copy("$failureLocation: ${it.message}")
                                }
                            }
                }

        val peopleResults = emptyList<String>()
                .plus(value.people.hostIds)
                .plus(value.people.guestIds ?: emptyList())
                .map { personId ->
                    Single.fromCallable {
                        if (people.find { it.id == personId } == null) {
                            ValidationResult.Failure("$failureLocation: person [$personId] is not defined.")
                        } else {
                            ValidationResult.Success
                        }
                    }
                }

        val idResult = Single.fromCallable {
            if (value.id.isBlank()) {
                ValidationResult.Failure("$failureLocation: ID is blank")
            } else {
                ValidationResult.Success
            }
        }

        val numberResult = Single.fromCallable {
            if (value.number < 0) {
                ValidationResult.Failure("$failureLocation: number is negative.")
            } else {
                ValidationResult.Success
            }
        }

        val partResult = Single.fromCallable {
            if (value.part != null && value.part < 0) {
                ValidationResult.Failure("$failureLocation: part is negative.")
            } else {
                ValidationResult.Success
            }
        }

        val dateResult = Single.fromCallable {
            try {
                time.parseIso(value.time)

                ValidationResult.Success
            } catch (e: DateTimeParseException) {
                ValidationResult.Failure("$failureLocation: time is in wrong format. Should be YYYY-MM-DDTHH:MM.")
            }
        }

        val fileLengthResult = Single.fromCallable {
            if (value.file.length < 0) {
                ValidationResult.Failure("$failureLocation: file length is negative.")
            } else {
                ValidationResult.Success
            }
        }

        val descriptionResult = Single.fromCallable {
            if (value.description.length > MAXIMUM_DESCRIPTION_LENGTH) {
                ValidationResult.Failure("$failureLocation: description length is [${value.description.length}] symbols but should less than [$MAXIMUM_DESCRIPTION_LENGTH].")
            } else {
                ValidationResult.Success
            }
        }

        val notesResult = markdownValidator.validate(value.notesMarkdown)

        return Single
                .merge(urlResults + peopleResults + idResult + numberResult + partResult + dateResult + fileLengthResult + descriptionResult + notesResult)
                .toList()
                .map { it.merge() }
    }
}