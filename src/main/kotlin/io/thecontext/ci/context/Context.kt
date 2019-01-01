package io.thecontext.ci.context

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.thecontext.ci.Time

interface Context {

    val ioScheduler: Scheduler

    val time: Time

    class Impl : Context {

        override val ioScheduler: Scheduler by lazy { Schedulers.io() }

        override val time: Time by lazy { Time.Impl() }
    }
}