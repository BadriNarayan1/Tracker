package com.example.tracker.domain.model

data class ProgressEntry(
    val date: String = "",
    val completedCount: Int = 0,
    val notCompletedCount: Int = 0,
    val categoryWiseTime: Map<String, Float> = emptyMap(),
    val categoryWiseEffortScaledTime: Map<String, Float> = emptyMap()
)

