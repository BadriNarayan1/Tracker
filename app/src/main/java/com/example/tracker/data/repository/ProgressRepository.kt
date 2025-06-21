package com.example.tracker.data.repository

import com.example.tracker.domain.model.ProgressEntry
import com.example.tracker.presentation.progress.TimeRange
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ProgressRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun getProgressEntriesForRange(range: TimeRange): List<ProgressEntry> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        val progressRef = firestore.collection("users").document(userId).collection("progress")

        val formatter = DateTimeFormatter.ISO_DATE
        val today = LocalDate.now()
        val startDate = when (range) {
            TimeRange.WEEK -> today.minusDays(6)
            TimeRange.MONTH -> today.minusDays(29)
            TimeRange.YEAR -> today.minusDays(364)
        }

        val snapshots = progressRef.get().await()
        return snapshots.documents.mapNotNull { it.toObject(ProgressEntry::class.java) }
            .filter {
                LocalDate.parse(it.date, formatter) in startDate..today
            }
            .sortedBy { it.date }
    }

    suspend fun loadActivityTypes(): List<String> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("activity_types")
            .get().await()

        return snapshot.documents.map { it.id }
    }
}