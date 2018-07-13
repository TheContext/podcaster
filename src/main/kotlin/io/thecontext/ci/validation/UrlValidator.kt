package io.thecontext.ci.validation

import io.reactivex.Scheduler
import io.reactivex.Single
import okhttp3.HttpUrl

class UrlValidator(
        private val ioScheduler: Scheduler
) : Validator<String> {

    override fun validate(value: String) = Single
            .fromCallable {
                if (HttpUrl.parse(value) == null) {
                    ValidationResult.Failure("URL [$value] has invalid format.")
                } else {
                    ValidationResult.Success
                }
            }
            .subscribeOn(ioScheduler)
}
