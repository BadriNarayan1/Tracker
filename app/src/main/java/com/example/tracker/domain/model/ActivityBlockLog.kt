package com.example.tracker.domain.model

enum class ActivityStatus {
    COMPLETED,
    MISSED,
    PENDING // User hasn’t ticked/marked it yet
}

data class ActivityBlockLog(
    val category: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val description: String = "",
    val score: Int = 0, // rating (1–5), 0 = unscored
    val status: ActivityStatus = ActivityStatus.PENDING
)
