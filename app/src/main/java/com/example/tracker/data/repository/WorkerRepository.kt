package com.example.tracker.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.example.tracker.domain.model.Activity
import com.example.tracker.domain.model.ProgressEntry
import com.example.tracker.domain.model.Status
import com.example.tracker.domain.util.calculateDuration

suspend fun processMidnightUpdate(
    firestore: FirebaseFirestore,
    auth: FirebaseAuth
) {
    val userId = auth.currentUser?.uid ?: return
    val userRef = firestore.collection("users").document(userId)

    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val todayKey = today.format(DateTimeFormatter.ISO_DATE)
    val yesterdayKey = yesterday.format(DateTimeFormatter.ISO_DATE)
    val todayWeekday = today.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    val tomorrowWeekday = today.plusDays(1).dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }

    val activityRef = userRef.collection("activities")
    val progressRef = userRef.collection("progress")
    val todayRef = userRef.collection("today")

    // === 1️⃣ Move documents from today/ -> activities/yyyy-MM-dd
    val todayDocs = todayRef.get().await()
    val todayActivityList = todayDocs.documents.mapNotNull { it.data }

    if (todayActivityList.isNotEmpty()) {
        activityRef.document(yesterdayKey).set(mapOf("list" to todayActivityList)).await()
    }

    // === 2️⃣ Compute progress from COMPLETED activities only
    val todayActivities = todayActivityList.mapNotNull { mapToActivity(it) }

    val completedActivities = todayActivities.filter { it.status == Status.COMPLETED }
    val notCompletedCount = todayActivities.size - completedActivities.size
    val completedCount = completedActivities.size

    val categoryWiseTime = mutableMapOf<String, Float>()
    val categoryWiseEffort = mutableMapOf<String, Float>()

    for (activity in completedActivities) {
        val duration = calculateDuration(activity.startTime, activity.endTime)
        val score = activity.score.coerceIn(0, 10)
        val scaledEffort = score * duration / 10f

        categoryWiseTime[activity.category] =
            categoryWiseTime.getOrDefault(activity.category, 0f) + duration
        categoryWiseEffort[activity.category] =
            categoryWiseEffort.getOrDefault(activity.category, 0f) + scaledEffort
    }

    val progressEntry = ProgressEntry(
        date = yesterdayKey,
        completedCount = completedCount,
        notCompletedCount = notCompletedCount,
        categoryWiseTime = categoryWiseTime,
        categoryWiseEffortScaledTime = categoryWiseEffort
    )

    progressRef.document(yesterdayKey).set(progressEntry).await()

    // === 3️⃣ Clear today/ collection
    for (doc in todayDocs.documents) {
        doc.reference.delete().await()
    }

    // === 4️⃣ Populate today/ from weekday_activities/{Tomorrow}
    val weekdaySnapshot = userRef.collection("weekday_activities").document(tomorrowWeekday).get().await()
    val weekdayList = (weekdaySnapshot["activities"] as? List<Map<String, Any>>).orEmpty()

    if (weekdayList.isNotEmpty()) {
        for (activityMap in weekdayList) {
            val newDocRef = todayRef.document() // generates a new ID
            val updatedMap = activityMap.toMutableMap()
            updatedMap["id"] = newDocRef.id // assign Firestore-generated ID
            newDocRef.set(updatedMap).await()
        }
    }
}


fun mapToActivity(data: Map<String, Any>): Activity? {
    return try {
        Activity(
            id = data["id"] as? String ?: "",
            category = data["category"] as? String ?: return null,
            startTime = data["startTime"] as? String ?: return null,
            endTime = data["endTime"] as? String ?: return null,
            description = data["description"] as? String ?: "",
            score = (data["score"] as? Long)?.toInt() ?: 0,
            status = Status.valueOf(data["status"] as? String ?: "PENDING")
        )
    } catch (e: Exception) {
        null
    }
}
