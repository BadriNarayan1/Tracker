package com.example.tracker.domain.model

data class Activity(
    val id: String = "",              // unique per activity
    val category: String = "",
    val startTime: String = "",       // e.g. "09:00"
    val endTime: String = "",
    val description: String = "",
    val score: Int = 0,               // 0 to 5
    val status: Status = Status.PENDING
)

enum class Status {
    COMPLETED, NOT_COMPLETED, PENDING
}
