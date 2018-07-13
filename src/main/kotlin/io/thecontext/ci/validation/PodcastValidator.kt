package io.thecontext.ci.validation

import io.reactivex.Single
import io.thecontext.ci.value.Person
import io.thecontext.ci.value.Podcast

class PodcastValidator(
        private val urlValidator: Validator<String>,
        private val people: List<Person>
) : Validator<Podcast> {

    companion object {
        const val MAXIMUM_DESCRIPTION_LENGTH = 1000
    }

    override fun validate(value: Podcast): Single<ValidationResult> {
        val urlResults = listOf(value.url, value.artworkUrl, value.feedUrl).map {
            urlValidator.validate(it)
        }

        val peopleResults = emptyList<String>()
                .plus(value.people.ownerIds)
                .plus(value.people.authorIds)
                .map { personId ->
                    Single.fromCallable {
                        if (people.find { it.id == personId } == null) {
                            ValidationResult.Failure("Person [$personId] is not defined.")
                        } else {
                            ValidationResult.Success
                        }
                    }
                }

        val descriptionResult = Single.fromCallable {
            if (value.description.length > MAXIMUM_DESCRIPTION_LENGTH) {
                ValidationResult.Failure("Description length is [${value.description.length}] symbols but should less than [$MAXIMUM_DESCRIPTION_LENGTH].")
            } else {
                ValidationResult.Success
            }
        }

        return Single
                .merge(urlResults + peopleResults + listOf(descriptionResult))
                .toList()
                .map { it.merge() }
    }
}
