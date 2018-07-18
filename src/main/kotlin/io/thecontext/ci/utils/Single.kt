package io.thecontext.ci.utils

import io.reactivex.Single

object Singles {
    fun <I, O> zip(iterable: Iterable<Single<I>>, zipper: (results: List<I>) -> O): Single<O> {
        return Single.zip(iterable) {
            val typesafeList = it.map {
                @Suppress("UNCHECKED_CAST")
                it as I
            }
            zipper(typesafeList)
        }
    }
}