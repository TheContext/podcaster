package io.thecontext.ci

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

interface Time {

    fun current(): LocalDateTime

    fun parseIso(time: String): LocalDateTime
    fun formatRfc2822(time: LocalDateTime): String

    class Impl : Time {

        companion object {
            private val FORMATTER_ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            private val FORMATTER_RFC_2822 = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z")
        }

        override fun current() = LocalDateTime.now()

        override fun parseIso(time: String) = LocalDateTime.parse(time, FORMATTER_ISO)
        override fun formatRfc2822(time: LocalDateTime) = time.atZone(ZoneId.of("UTC")).format(FORMATTER_RFC_2822)
    }
}
