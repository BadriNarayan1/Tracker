package com.example.tracker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tracker.Injection
import com.example.tracker.data.repository.processMidnightUpdate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DailyMidnightWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val firestore = Injection.instance()
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser == null) return Result.retry()
            processMidnightUpdate(firestore, auth)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
