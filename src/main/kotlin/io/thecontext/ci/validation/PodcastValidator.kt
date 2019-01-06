package io.thecontext.ci.validation

import io.reactivex.Single
import io.thecontext.ci.value.Person
import io.thecontext.ci.value.Podcast
import java.util.*

class PodcastValidator(
        private val urlValidator: Validator<String>,
        private val people: List<Person>
) : Validator<Podcast> {

    companion object {
        const val MAXIMUM_DESCRIPTION_LENGTH = 1000
    }

    override fun validate(value: Podcast): Single<ValidationResult> {
        val urlResults = listOf(value.url, value.artworkUrl)
                .map {
                    urlValidator.validate(it)
                }

        val peopleResults = emptyList<String>()
                .plus(value.people.ownerId)
                .plus(value.people.authorIds)
                .map { personId ->
                    Single.fromCallable {
                        if (people.find { it.id == personId } == null) {
                            ValidationResult.Failure("Podcast person [$personId] is not defined.")
                        } else {
                            ValidationResult.Success
                        }
                    }
                }

        val ownerResults = Single.fromCallable {
            if (people.find { it.id == value.people.ownerId }?.email == null) {
                ValidationResult.Failure("Podcast owner should have an email address.")
            } else {
                ValidationResult.Success
            }
        }

        val descriptionResult = Single.fromCallable {
            if (value.description.length > MAXIMUM_DESCRIPTION_LENGTH) {
                ValidationResult.Failure("Podcast description length is [${value.description.length}] symbols but should less than [$MAXIMUM_DESCRIPTION_LENGTH].")
            } else {
                ValidationResult.Success
            }
        }

        val languageResult = Single.fromCallable {
            if (!Locale.getISOLanguages().contains(value.language)) {
                ValidationResult.Failure("Podcast language should be ISO 639 code.")
            } else {
                ValidationResult.Success
            }
        }

        return Single
                .merge(urlResults + peopleResults + ownerResults + descriptionResult + languageResult)
                .toList()
                .map { it.merge() }
    }
}
