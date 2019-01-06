package io.thecontext.podcaster.validation

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.dsl.specification.Specification.context
import com.greghaskins.spectrum.dsl.specification.Specification.it
import io.reactivex.schedulers.Schedulers
import io.thecontext.podcaster.memoized
import io.thecontext.podcaster.testPerson
import io.thecontext.podcaster.testPodcast
import org.junit.runner.RunWith

@RunWith(Spectrum::class)
class PodcastValidatorSpec {
    init {
        val env by memoized { Environment() }

        context("value is valid") {

            it("emits result as success") {
                env.validator.validate(testPodcast)
                        .test()
                        .assertResult(ValidationResult.Success)
            }
        }

        context("URL is not valid") {

            it("emits result as failure") {
                env.validator.validate(testPodcast.copy(url = "not URL"))
                        .test()
                        .assertValue { it is ValidationResult.Failure }
            }
        }

        context("artwork URL is not valid") {

            it("emits result as failure") {
                env.validator.validate(testPodcast.copy(artworkUrl = "not URL"))
                        .test()
                        .assertValue { it is ValidationResult.Failure }
            }
        }

        context("owner is not available") {

            it("emits result as failure") {
                env.validator.validate(testPodcast.copy(people = testPodcast.people.copy(ownerId = "not available")))
                        .test()
                        .assertValue { it is ValidationResult.Failure }
            }
        }

        context("author is not available") {

            it("emits result as failure") {
                env.validator.validate(testPodcast.copy(people = testPodcast.people.copy(authorIds = listOf("not available"))))
                        .test()
                        .assertValue { it is ValidationResult.Failure }
            }
        }

        context("owner does not have email address") {

            it("emits result as failure") {
                env.validator.validate(testPodcast.copy(people = testPodcast.people.copy(ownerId = Environment.PERSON_WITHOUT_EMAIL.id)))
                        .test()
                        .assertValue { it is ValidationResult.Failure }
            }
        }

        context("description length is too long") {

            it("emits result as failure") {
                env.validator.validate(testPodcast.copy(description = "Z".repeat(PodcastValidator.MAXIMUM_DESCRIPTION_LENGTH + 1)))
                        .test()
                        .assertValue { it is ValidationResult.Failure }
            }
        }

        context("language is not ISO code") {

            it("emits result as failure") {
                env.validator.validate(testPodcast.copy(language = "not ISO"))
                        .test()
                        .assertValue { it is ValidationResult.Failure }
            }
        }
    }

    private class Environment {

        companion object {
            val PERSON = testPerson
            val PERSON_WITHOUT_EMAIL = testPerson.copy(id = "person without email", email = null)
        }

        private val urlValidator = UrlValidator(Schedulers.trampoline())

        val validator = PodcastValidator(urlValidator, listOf(PERSON, PERSON_WITHOUT_EMAIL))
    }
}