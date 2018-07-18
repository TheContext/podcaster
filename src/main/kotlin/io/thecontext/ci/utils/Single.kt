package io.thecontext.ci.utils

import io.reactivex.Single

object Singles {
    /**
     * Little helper function that helps working more convinient with arbitary many sources that must be zipped together
     */
    inline fun <I, O> zip(iterable: Iterable<Single<I>>, crossinline zipper: (results: List<I>) -> O): Single<O> {
        return Single.zip(iterable) {
            val typesafeList = it.map {
                @Suppress("UNCHECKED_CAST")
                it as I
            }
            zipper(typesafeList)
        }
    }

    fun <T> zip(iterable: Iterable<Single<T>>) = Singles.zip(iterable) { it }
}