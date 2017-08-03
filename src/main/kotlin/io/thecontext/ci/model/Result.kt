package io.thecontext.ci.model

sealed class Result<T>

data class ValidResult<T>(val value: T) : Result<T>()

data class ErrorResult<T>(val errors: List<ErrorMessage>) : Result<T>() {
    constructor(vararg errorMessages: String) : this(errorMessages.map { ErrorMessage(it) })
}

// TODO remove this class and just use String directly
data class ErrorMessage(val message: String, val throwable: Throwable? = null)

fun <T> Result<T>.unwrapValue(): T =
        if (this is ValidResult)
            value
        else
            throw AssertionError("You try to unwrap the result value but it is actually not a ValidResult but rather an ErrorResult. This code should never be reached. You have a error in reasoning in your code. Fix it!")



fun <T> Result<T>.unwrapErrors(): List<ErrorMessage> =
        if (this is ErrorResult)
            errors
        else
            throw AssertionError("You try to unwrap the errors but it is actually not a ErrorResult but rather an ValidResult. This code should never be reached. You have a error in reasoning in your code. Fix it!")
