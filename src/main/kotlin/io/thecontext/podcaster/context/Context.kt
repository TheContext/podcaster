package io.thecontext.podcaster.context

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.thecontext.podcaster.Time

interface Context {

    val ioScheduler: Scheduler
    val time: Time

    class Impl : Context {

        override val ioScheduler by lazy { Schedulers.io() }
        override val time: Time by lazy { Time.Impl() }
    }
}