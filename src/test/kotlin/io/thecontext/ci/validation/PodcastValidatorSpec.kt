package io.thecontext.ci.validation

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.dsl.specification.Specification.*
import io.reactivex.Single
import io.thecontext.ci.memoized
import io.thecontext.ci.testPerson
import io.thecontext.ci.testPodcast
import org.junit.runner.RunWith

@RunWith(Spectrum::class)
class PodcastValidatorSpec {
    init {
        val env by memoized { Environment() }

        context("value is valid") {

            it("emits result as success") {
                env.validator.validate(testPodcast)
                        .test()
                        .assertValue { it is ValidationResult.Success }
            }
        }

        context("url validation failed") {

            beforeEach {
                env.urlValidator.result = ValidationResult.Failure("nope")
            }

            it("emits result as failure") {
                env.validator.validate(testPodcast)
                        .test()
                        .assertValue { it is ValidationResult.Failure }
            }
        }

        context("owner is not available") {

            it("emits result as failure") {
                env.validator.validate(testPodcast.copy(people = testPodcast.people.copy(ownerIds = listOf("not available"))))
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
                env.validator.validate(testPodcast.copy(people = testPodcast.people.copy(ownerIds = listOf(Environment.PERSON_WITHOUT_EMAIL.id))))
                        .test()
                        .assertValue { it is ValidationResult.Failure }
            }
        }

        context("description length is too big") {

            it("emits result as failure") {
                env.validator.validate(testPodcast.copy(description = "Z".repeat(PodcastValidator.MAXIMUM_DESCRIPTION_LENGTH + 1)))
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

        val urlValidator = TestUrlValidator()

        val validator = PodcastValidator(urlValidator, listOf(PERSON, PERSON_WITHOUT_EMAIL))
    }

    private class TestUrlValidator : Validator<String> {
        var result: ValidationResult = ValidationResult.Success

        override fun validate(value: String) = Single.just(result)
    }
}