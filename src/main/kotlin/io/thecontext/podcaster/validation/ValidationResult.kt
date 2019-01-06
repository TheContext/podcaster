package io.thecontext.podcaster.validation

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Failure(val message: String) : ValidationResult()
}

fun List<ValidationResult>.merge(): ValidationResult {
    val failureResults = filterIsInstance<ValidationResult.Failure>()

    return if (failureResults.isEmpty()) {
        ValidationResult.Success
    } else {
        val message = failureResults.map { it.message }.joinToString(separator = "\n")

        ValidationResult.Failure(message)
    }
}