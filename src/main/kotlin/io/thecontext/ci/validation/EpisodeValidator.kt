package io.thecontext.ci.validation

import io.reactivex.Single
import io.thecontext.ci.Time
import io.thecontext.ci.value.Episode
import io.thecontext.ci.value.Person
import java.time.format.DateTimeParseException

class EpisodeValidator(
        private val urlValidator: Validator<String>,
        private val people: List<Person>,
        private val time: Time
) : Validator<Episode> {

    companion object {
        const val MAXIMUM_DESCRIPTION_LENGTH = 200
    }

    override fun validate(value: Episode): Single<ValidationResult> {
        val urlResults = emptyList<String>()
                .plus(value.discussionUrl)
                .plus(value.file.url)
                .map {
                    urlValidator.validate(it)
                            .map {
                                when (it) {
                                    ValidationResult.Success -> it
                                    is ValidationResult.Failure -> it.copy("${value.slug}: ${it.message}")
                                }
                            }
                }

        val peopleResults = emptyList<String>()
                .plus(value.people.hostIds)
                .plus(value.people.guestIds ?: emptyList())
                .map { personId ->
                    Single.fromCallable {
                        if (people.find { it.id == personId } == null) {
                            ValidationResult.Failure("${value.slug}: Person [$personId] is not defined.")
                        } else {
                            ValidationResult.Success
                        }
                    }
                }

        val idResult = Single.fromCallable {
            if (value.id.isBlank()) {
                ValidationResult.Failure("${value.slug}: ID is blank")
            } else {
                ValidationResult.Success
            }
        }

        val numberResult = Single.fromCallable {
            if (value.number < 0) {
                ValidationResult.Failure("${value.slug}: Episode number is negative.")
            } else {
                ValidationResult.Success
            }
        }

        val partResult = Single.fromCallable {
            if (value.part != null && value.part < 0) {
                ValidationResult.Failure("${value.slug}: Episode part is negative.")
            } else {
                ValidationResult.Success
            }
        }

        val dateResult = Single.fromCallable {
            try {
                time.parseIso(value.time)

                ValidationResult.Success
            } catch (e: DateTimeParseException) {
                ValidationResult.Failure("${value.slug}: Episode time is in wrong format. Should be YYYY-MM-DDTHH:MM.")
            }
        }

        val fileLengthResult = Single.fromCallable {
            if (value.file.length < 0) {
                ValidationResult.Failure("${value.slug}: File length is negative.")
            } else {
                ValidationResult.Success
            }
        }

        val descriptionResult = Single.fromCallable {
            if (value.description.length > MAXIMUM_DESCRIPTION_LENGTH) {
                ValidationResult.Failure("${value.slug}: Description length is [${value.description.length}] symbols but should less than [$MAXIMUM_DESCRIPTION_LENGTH].")
            } else {
                ValidationResult.Success
            }
        }

        return Single
                .merge(urlResults + peopleResults + listOf(idResult, numberResult, partResult, dateResult, fileLengthResult, descriptionResult))
                .toList()
                .map { it.merge() }
    }
}