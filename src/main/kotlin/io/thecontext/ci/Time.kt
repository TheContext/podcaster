package io.thecontext.ci

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun String.toDate(): LocalDate = LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd"))

fun LocalDate.toRfc2822(): String = atStartOfDay(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z"))

