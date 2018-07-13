package io.thecontext.ci.validation

import io.reactivex.Single
import io.thecontext.ci.value.Person
import io.thecontext.ci.value.Podcast

class PodcastValidator(
        private val urlValidator: Validator<String>,
        private val people: List<Person>
) : Validator<Podcast> {

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

        return Single
                .merge(urlResults + peopleResults)
                .toList()
                .map { it.merge() }
    }
}
