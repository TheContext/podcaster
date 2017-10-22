package io.thecontext.ci.serializer

import io.thecontext.ci.model.ErrorResult
import io.thecontext.ci.model.Link
import io.thecontext.ci.model.ValidResult
import io.thecontext.ci.validator.OkHttpLinkValidator
import okhttp3.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.commons.annotation.Testable

class LinkValidatorSpec : Spek({

    describe("non-HTTP links") {

        listOf("schema://localhost", "asset://localhost", "file://localhost").forEach { uri ->

            context("link [$uri]") {

                val link = Link("title", uri)

                val env by memoized { Environment() }

                it("emits link as not valid") {
                    env.validator.validate(link)
                            .test()
                            .assertValue { it is ErrorResult<Boolean> }
                }
            }
        }
    }

    describe("HTTP links") {

        listOf("http://localhost", "https://google.com", "https://github.com").forEach { url ->

            context("link [$url]") {

                val link = Link("title", url)

                context("link is reachable") {

                    val env by memoized { Environment(supportedUrls = listOf(url)) }

                    it("emits link as valid") {
                        env.validator.validate(link)
                                .test()
                                .assertValue(ValidResult<Boolean>(true))
                    }
                }

                context("link is not reachable") {

                    val env by memoized { Environment(supportedUrls = emptyList()) }

                    it("emits link as not valid") {
                        env.validator.validate(link)
                                .test()
                                .assertValue { it is ErrorResult<Boolean> }
                    }
                }
            }
        }
    }

}) {

    class Environment(supportedUrls: List<String> = emptyList()) {
        private val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(ProxyInterceptor(supportedUrls))
                .build()

        val validator = OkHttpLinkValidator(okHttpClient)
    }

    class ProxyInterceptor(
            private val supportedUrls: List<String>
    ) : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()

            val response = Response
                    .Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_2)
                    .message("It works!")
                    .code(if (supportedUrls.map { HttpUrl.parse(it) }.contains(request.url())) 200 else 500)
                    .build()

            return response
        }
    }
}