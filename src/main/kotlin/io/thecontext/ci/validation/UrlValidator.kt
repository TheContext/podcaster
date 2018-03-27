package io.thecontext.ci.validation

import io.reactivex.Scheduler
import io.reactivex.Single
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

class UrlValidator(
        private val httpClient: OkHttpClient,
        private val ioScheduler: Scheduler
) : Validator<String> {

    override fun validate(value: String) = Single
            .fromCallable {
                val httpUrl = HttpUrl.parse(value)

                if (httpUrl == null) {
                    ValidationResult.Failure("URL [$value] has invalid format.")
                } else {
                    val request = Request.Builder()
                            .url(httpUrl)
                            .head()
                            .build()

                    val response = httpClient
                            .newCall(request)
                            .execute()

                    if (response.isSuccessful) {
                        ValidationResult.Success
                    } else {
                        ValidationResult.Failure("URL [$value] returns [${response.code()}].")
                    }
                }
            }
            .subscribeOn(ioScheduler)
}
