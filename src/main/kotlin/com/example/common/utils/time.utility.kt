package com.example.common.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun Instant.toReadableDateTime(
    zoneId: ZoneId
): String =
    this.atZone(zoneId).format(
        DateTimeFormatter.ofPattern("MMM dd, YYYY hh:mm a")
    )

fun isValidTimeZone(timeZoneId: String): Boolean = try {
    ZoneId.of(timeZoneId)
    true
} catch (e: Exception) {
    false
}

fun LocalDate.dayStart(zoneId: ZoneId): Instant =
    this.atStartOfDay(zoneId).toInstant()

fun LocalDate.dayEnd(zoneId: ZoneId): Instant =
    this.dayStart(zoneId)
        .plus(1, ChronoUnit.DAYS)
        .minusMillis(1)

fun Instant.dayStart(zoneId: ZoneId): Instant =
    LocalDateTime.ofInstant(this, zoneId).toLocalDate()
        .dayStart(zoneId)

fun Instant.dayEnd(zoneId: ZoneId): Instant =
    this.dayStart(zoneId)
        .plus(24, ChronoUnit.HOURS)
        .minusMillis(1)

fun Instant.isToday(zoneId: ZoneId): Boolean {
    val now = Instant.now()
    val start = now.dayStart(zoneId)
    val end = now.dayEnd(zoneId)
    return this.isAfter(start) && this.isBefore(end)
}

fun readableDateRange(
    start: Instant,
    end: Instant,
    zoneId: ZoneId
): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, YYYY")
    val startStr = start.atZone(zoneId).format(formatter)
    val endStr = end.atZone(zoneId).format(formatter)
    return "$startStr - $endStr"
}