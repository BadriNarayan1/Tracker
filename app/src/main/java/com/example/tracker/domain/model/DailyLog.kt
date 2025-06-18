package com.example.tracker.domain.model

data class DailyLog(
    val date: String = "", // "2025-06-18" format
    val activities: List<ActivityBlockLog> = emptyList()
)
