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
    }

    private class Environment {
        val urlValidator = TestUrlValidator()

        val validator = PodcastValidator(urlValidator, listOf(testPerson))
    }

    private class TestUrlValidator : Validator<String> {
        var result: ValidationResult = ValidationResult.Success

        override fun validate(value: String) = Single.just(result)
    }
}