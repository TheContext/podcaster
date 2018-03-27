package io.thecontext.ci

import io.reactivex.Observable
import io.reactivex.functions.BiFunction

inline fun <reified R : Any> Observable<*>.ofType(): Observable<R> = ofType(R::class.java)

inline fun <T1, T2, R> Observable<T1>.withLatestFrom(observable: Observable<T2>, crossinline predicate: (T1, T2) -> R): Observable<R> =
        withLatestFrom(observable, BiFunction { t1, t2 -> predicate.invoke(t1, t2) })