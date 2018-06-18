package io.thecontext.ci.validation

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.dsl.specification.Specification.*
import io.reactivex.schedulers.Schedulers
import io.thecontext.ci.memoized
import org.junit.runner.RunWith

@RunWith(Spectrum::class)
class UrlValidatorSpec {
    init {
        val validator by memoized { UrlValidator(Schedulers.trampoline()) }

        describe("non-HTTP URI") {

            listOf("schema://localhost", "asset://localhost", "file://localhost").forEach { uri ->

                context("uri [$uri]") {

                    it("emits result as failure") {
                        validator.validate(uri)
                                .test()
                                .assertValue { it is ValidationResult.Failure }
                    }
                }
            }
        }

        describe("HTTP URL") {

            listOf("http://localhost", "https://google.com", "https://github.com").forEach { url ->

                context("url [$url]") {

                    it("emits result as success") {
                        validator.validate(url)
                                .test()
                                .assertValue(ValidationResult.Success)
                    }
                }
            }
        }
    }
}