package com.example.common.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Instant.toReadableDateTime(
    zoneId: ZoneId
): String =
    this.atZone(zoneId).format(
        DateTimeFormatter.ofPattern("MMM dd, YYYY hh:mm a")
    )