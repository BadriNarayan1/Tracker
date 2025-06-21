package com.example.tracker.data.repository

import com.example.tracker.Injection
import com.example.tracker.domain.model.Activity
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class ActivityRepository {
    private val firestore = Injection.instance()

    suspend fun addActivity(userId: String, activity: Activity) {
        val docRef = firestore.collection("users")
            .document(userId)
            .collection("today")
            .document()

        docRef.set(activity.copy(id = docRef.id)).await()
    }

    suspend fun getTodayActivities(userId: String): List<Activity> {
        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("today")
            .get()
            .await()

        return snapshot.documents.mapNotNull { it.toObject<Activity>() }
    }

    suspend fun updateActivityStatus(userId: String, activity: Activity) {
        firestore.collection("users")
            .document(userId)
            .collection("today")
            .document(activity.id)
            .set(activity)
            .await()
    }

    suspend fun deleteActivity(userId: String, activityId: String) {
        firestore.collection("users")
            .document(userId)
            .collection("today")
            .document(activityId)
            .delete()
            .await()
    }
}
