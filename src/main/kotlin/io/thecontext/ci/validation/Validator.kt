package io.thecontext.ci.validation

import io.reactivex.Single

interface Validator<in T> {

    fun validate(value: T): Single<ValidationResult>
}