package io.thecontext.ci.utils

/**
 * Either is a simple type that holds Left or Right value
 */
sealed class Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()
}


fun <L, R> Either<L, R>.right () = when (this) {
    is Either.Left<L> -> throw IllegalAccessException("Can't unwrap right because it's actually left")
    is Either.Right<R> -> value
}


fun <L, R> Either<L, R>.left () = when (this) {
    is Either.Left<L> -> value
    is Either.Right<R> -> throw IllegalAccessException("Can't unwrap left because it's actually right")
}