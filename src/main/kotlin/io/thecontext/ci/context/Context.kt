package io.thecontext.ci.context

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

interface Context {

    val ioScheduler: Scheduler

    class Impl : Context {

        override val ioScheduler by lazy { Schedulers.io() }
    }
}