// File: ScheduledActivity.kt
package com.example.tracker.domain.model

data class ScheduledActivity(
    val id: String, // Unique ID for keying and reference
    val startTime: String,
    val endTime: String,
    val type: String,
    val description: String,
    val youtubeLink: String? = null,
    val spotifyLink: String? = null,
    val pdfLink: String? = null
)
