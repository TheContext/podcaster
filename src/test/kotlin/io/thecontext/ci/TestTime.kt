package io.thecontext.ci

import java.time.LocalDateTime

class TestTime : Time by Time.Impl() {
    var currentResult = LocalDateTime.MIN

    override fun current() = currentResult
}