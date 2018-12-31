package io.thecontext.ci

import java.time.LocalDateTime

class TestTime : Time {
    var currentResult = LocalDateTime.MIN

    var formatRfc2822Result = "2000-01-01T00:00"

    override fun current() = currentResult

    override fun parseIso(time: String) = Time.Impl().parseIso(time)
    override fun formatRfc2822(time: LocalDateTime) = formatRfc2822Result
}