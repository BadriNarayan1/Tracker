package com.example.tracker.domain.model

data class TodaySchedule(
    val date: String = "",                      // yyyy-MM-dd
    val activities: List<Activity> = emptyList()
)
