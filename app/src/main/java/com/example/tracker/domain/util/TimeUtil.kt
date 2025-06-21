package com.example.tracker.domain.util

import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun calculateDuration(start: String, end: String): Float {
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
    val startTime = LocalTime.parse(start, formatter)
    val endTime = LocalTime.parse(end, formatter)
    val duration = java.time.Duration.between(startTime, endTime).toMinutes().coerceAtLeast(0)
    return duration / 60f
}

