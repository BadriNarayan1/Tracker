package com.example.tracker.domain.model

data class WeekTemplate(
    val templateId: String = "",
    val name: String = "",
    val weekMap: Map<String, List<Activity>> = emptyMap()
    // keys: "Monday", "Tuesday", etc.
)
