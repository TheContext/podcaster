package io.thecontext.ci.validation

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.dsl.specification.Specification.*
import io.reactivex.schedulers.Schedulers
import io.thecontext.ci.Time
import io.thecontext.ci.memoized
import io.thecontext.ci.testEpisode
import io.thecontext.ci.testPerson
import org.junit.runner.RunWith

@RunWith(Spectrum::class)
class EpisodeValidatorSpec {
    init {
        val env by memoized { Environment() }

        context("value is valid") {

            it("emits result as success") {
                env.validator.validate(testEpisode)
                        .test()
                        .assertResult(ValidationResult.Success)
            }
        }

        context("ID is empty") {

            it("emits result as failure") {
                env.validator.validate(testEpisode.copy(id = ""))
                        .test()
                        .assertValue { it is ValidationResult.Failure }
            }
        }

        context("ID is blank") {

            it("emits result as failure") {
                env.validator.validate(testEpisode.copy(id = " "))
                        .test()
                        .assertValue { it is ValidationResult.Failure }
            }
        }

        context("number is negative") {

            it("emits result as failure") {
                env.validator.validate(testEpisode.copy(number = -1))
                        .test()
                        .assertValue { it is ValidationResult.Failure }
            }
        }

        context("part is negative") {

            it("emits result as failure") {
                env.validator.validate(testEpisode.copy(part = -1))
                        .test()
                        .assertValue { it is ValidationResult.Failure }
            }
        }

        context("file URL is not valid") {

            it("emits result as failure") {
                env.validator.validate(testEpisode.copy(file = testEpisode.file.copy(url = "not URL")))
                        .test()
                        .assertValue { it is ValidationResult.Failure }
            }
        }

        context("discussion URL is not valid") {

            it("emits result as failure") {
                env.validator.validate(testEpisode.copy(discussionUrl = "not URL"))
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
                env.validator.validate(testEpisode.copy(time = "ZERO"))
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

        context("description length is too long") {

            it("emits result as failure") {
                env.validator.validate(testEpisode.copy(description = "Z".repeat(EpisodeValidator.MAXIMUM_DESCRIPTION_LENGTH + 1)))
                        .test()
                        .assertValue { it is ValidationResult.Failure }
            }
        }

        context("notes contain heading") {

            it("emits result as failure") {
                env.validator.validate(testEpisode.copy(notesMarkdown = "# Heading"))
                        .test()
                        .assertValue { it is ValidationResult.Failure }
            }
        }
    }

    private class Environment {
        private val markdownValidator = MarkdownValidator(Schedulers.trampoline())
        private val urlValidator = UrlValidator(Schedulers.trampoline())
        private val time = Time.Impl()

        val validator = EpisodeValidator(markdownValidator, urlValidator, listOf(testPerson), time)
    }
}