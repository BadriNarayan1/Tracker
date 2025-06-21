package com.example.tracker.domain.model

data class DayTemplate(
    val templateId: String = "", // Firebase ID (if needed)
    val name: String = "",       // e.g., "Exam Day", "Workout Focus"
    val activities: List<Activity> = emptyList()
)
