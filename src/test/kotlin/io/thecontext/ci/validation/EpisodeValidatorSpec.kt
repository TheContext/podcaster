package io.thecontext.ci.validation

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.dsl.specification.Specification.*
import io.reactivex.Single
import io.thecontext.ci.memoized
import io.thecontext.ci.testEpisode
import io.thecontext.ci.testPerson
import org.junit.runner.RunWith

@RunWith(Spectrum::class)
class EpisodeValidatorSpec {
    init {
        val env by memoized { Environment() }

        context("id validation failed") {

            it("emits result as failure on empty id") {
                env.validator.validate(testEpisode.copy(id = ""))
                        .test()
                        .assertValue { it is ValidationResult.Failure }
            }

            it("emits result as failure on blank id") {
                env.validator.validate(testEpisode.copy(id = " "))
                        .test()
                        .assertValue { it is ValidationResult.Failure }
            }
        }

        context("url validation failed") {

            beforeEach {
                env.urlValidator.result = ValidationResult.Failure("nope")
            }

            it("emits result as failure") {
                env.validator.validate(testEpisode)
                        .test()
                        .assertValue { it is ValidationResult.Failure }
            }
        }

        context("host is not available") {

            it("emits result as failure") {
                env.validator.validate(testEpisode.copy(people = testEpisode.people.copy(hostIds = listOf("not available"))))
                        .test()
                        .assertValue { it is ValidationResult.Failure }
            }
        }

        context("guest is not available") {

            it("emits result as failure") {
                env.validator.validate(testEpisode.copy(people = testEpisode.people.copy(guestIds = listOf("not available"))))
                        .test()
                        .assertValue { it is ValidationResult.Failure }
            }
        }

        context("date is in wrong format") {

            it("emits result as failure") {
                env.validator.validate(testEpisode.copy(date = "ZERO"))
                        .test()
                        .assertValue { it is ValidationResult.Failure }
            }
        }

        context("file length is negative") {

            it("emits result as failure") {
                env.validator.validate(testEpisode.copy(file = testEpisode.file.copy(length = Long.MIN_VALUE)))
                        .test()
                        .assertValue { it is ValidationResult.Failure }
            }
        }

        context("description length is too big") {

            it("emits result as failure") {
                env.validator.validate(testEpisode.copy(description = "Z".repeat(EpisodeValidator.MAXIMUM_DESCRIPTION_LENGTH + 1)))
                        .test()
                        .assertValue { it is ValidationResult.Failure }
            }
        }
    }

    private class Environment {
        val urlValidator = TestUrlValidator()

        val validator = EpisodeValidator(urlValidator, listOf(testPerson))
    }

    private class TestUrlValidator : Validator<String> {
        var result: ValidationResult = ValidationResult.Success

        override fun validate(value: String) = Single.just(result)
    }
}