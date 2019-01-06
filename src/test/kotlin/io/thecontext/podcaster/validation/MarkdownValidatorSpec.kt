package io.thecontext.podcaster.validation

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.dsl.specification.Specification.*
import io.reactivex.schedulers.Schedulers
import io.thecontext.podcaster.memoized
import org.junit.runner.RunWith

@RunWith(Spectrum::class)
class MarkdownValidatorSpec {
    init {
        val validator by memoized { MarkdownValidator(Schedulers.trampoline()) }

        describe("Markdown with headings") {

            listOf("# H", "## H", "### H", "#### H", "##### H").forEach { markdown ->

                context("Markdown [$markdown]") {

                    it("emits result as failure") {
                        validator.validate(markdown)
                                .test()
                                .assertValue { it is ValidationResult.Failure }
                    }
                }
            }
        }

        describe("Markdown without headings") {

            listOf("text", "[link][URL]").forEach { markdown ->

                context("Markdown [$markdown]") {

                    it("emits result as success") {
                        validator.validate(markdown)
                                .test()
                                .assertResult(ValidationResult.Success)
                    }
                }
            }
        }
    }
}