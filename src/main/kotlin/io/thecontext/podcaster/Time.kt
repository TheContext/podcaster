package io.thecontext.podcaster

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

interface Time {

    fun current(): LocalDateTime

    fun parseIso(time: String): LocalDateTime
    fun formatRfc2822(time: LocalDateTime): String

    class Impl : Time {

        companion object {
            private val LOCALE = Locale.ENGLISH

            private val FORMATTER_ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withLocale(LOCALE)
            private val FORMATTER_RFC_2822 = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z").withLocale(LOCALE)
        }

        override fun current() = LocalDateTime.now()

        override fun parseIso(time: String) = LocalDateTime.parse(time, FORMATTER_ISO)
        override fun formatRfc2822(time: LocalDateTime) = time.atZone(ZoneId.of("UTC")).format(FORMATTER_RFC_2822)
    }
}
