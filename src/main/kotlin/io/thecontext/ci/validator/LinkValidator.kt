package io.thecontext.ci.validator

import io.reactivex.Completable
import io.reactivex.Single
import io.thecontext.ci.model.ErrorResult
import io.thecontext.ci.model.Link
import io.thecontext.ci.model.Result
import io.thecontext.ci.model.ValidResult
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

interface LinkValidator {
    fun validate(link : Link): Single<Result<Boolean>>
}


class OkHttpLinkValidator(
        private val okHttp: OkHttpClient
) : LinkValidator {

    override fun validate(link : Link): Single<Result<Boolean>> = Single.create {
        val urlStr = link.url
        val url = HttpUrl.parse(urlStr)
        if (url == null) {
            if (!it.isDisposed) {
                it.onError(IllegalArgumentException("Invalid URL: $urlStr"))
            }
        } else {
            val request = Request.Builder().url(url).head().build()
            val response = okHttp.newCall(request).execute()
            if (!it.isDisposed) {
                if (response.isSuccessful) {
                    it.onSuccess(ValidResult(true))
                } else {
                    it.onSuccess(ErrorResult("The URL '$urlStr has returned ${response.code()}"))
                }
            }
        }
    }
}
