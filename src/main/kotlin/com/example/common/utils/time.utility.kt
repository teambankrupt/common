package com.example.common.utils

import java.time.Instant
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

fun Instant.dayStart(zoneId: ZoneId): Instant =
    LocalDateTime.ofInstant(this, zoneId).toLocalDate()
        .atStartOfDay(zoneId).toInstant()

fun Instant.dayEnd(zoneId: ZoneId): Instant =
    this.dayStart(zoneId)
        .plus(24, ChronoUnit.HOURS)
        .minusMillis(1)
