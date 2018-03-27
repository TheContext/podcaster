package io.thecontext.ci.validation

import com.greghaskins.spectrum.Spectrum
import com.greghaskins.spectrum.dsl.specification.Specification.*
import io.reactivex.schedulers.Schedulers
import io.thecontext.ci.memoized
import okhttp3.*
import org.junit.runner.RunWith

@RunWith(Spectrum::class)
class UrlValidatorSpec {
    init {
        describe("non-HTTP URI") {

            listOf("schema://localhost", "asset://localhost", "file://localhost").forEach { uri ->

                context("uri [$uri]") {

                    val env by memoized { Environment() }

                    it("emits result as failure") {
                        env.validator.validate(uri)
                                .test()
                                .assertValue { it is ValidationResult.Failure }
                    }
                }
            }
        }

        describe("HTTP URL") {

            listOf("http://localhost", "https://google.com", "https://github.com").forEach { url ->

                context("url [$url]") {

                    context("link is reachable") {

                        val env by memoized { Environment(supportedUrls = listOf(url)) }

                        it("emits result as success") {
                            env.validator.validate(url)
                                    .test()
                                    .assertValue(ValidationResult.Success)
                        }
                    }

                    context("link is unreachable") {

                        val env by memoized { Environment(supportedUrls = emptyList()) }

                        it("emits link as not valid") {
                            env.validator.validate(url)
                                    .test()
                                    .assertValue { it is ValidationResult.Failure }
                        }
                    }
                }
            }
        }
    }

    private class Environment(supportedUrls: List<String> = emptyList()) {
        private val httpClient = OkHttpClient.Builder()
                .addInterceptor(ProxyInterceptor(supportedUrls))
                .build()

        val validator = UrlValidator(httpClient, Schedulers.trampoline())
    }

    private class ProxyInterceptor(
            private val supportedUrls: List<String>
    ) : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()

            val response = Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_2)
                    .message("It works!")
                    .code(if (supportedUrls.map { HttpUrl.parse(it) }.contains(request.url())) 200 else 500)
                    .body(ResponseBody.create(MediaType.parse("mime"), "content"))
                    .build()

            return response
        }
    }
}