package com.example.tracker.data.repository

import com.example.tracker.domain.model.Activity
import com.example.tracker.domain.model.DayTemplate
import com.example.tracker.domain.model.WeekTemplate
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TemplateRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getDayTemplates(userId: String): List<DayTemplate> {
        return try {
            db.collection("users").document(userId)
                .collection("day_templates")
                .get().await()
                .map { it.toObject(DayTemplate::class.java).copy(templateId = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getWeekTemplates(userId: String): List<WeekTemplate> {
        return try {
            db.collection("users").document(userId)
                .collection("week_templates")
                .get().await()
                .map { it.toObject(WeekTemplate::class.java).copy(templateId = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDayTemplateById(userId: String, templateId: String): DayTemplate? {
        return try {
            val snapshot = db.collection("users").document(userId)
                .collection("day_templates")
                .document(templateId)
                .get().await()

            snapshot.toObject(DayTemplate::class.java)?.copy(templateId = snapshot.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getWeekTemplateById(userId: String, templateId: String): WeekTemplate? {
        return try {
            val snapshot = db.collection("users").document(userId)
                .collection("week_templates")
                .document(templateId)
                .get().await()

            snapshot.toObject(WeekTemplate::class.java)?.copy(templateId = snapshot.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteDayTemplate(userId: String, templateId: String) {
        db.collection("users").document(userId)
            .collection("day_templates")
            .document(templateId).delete().await()
    }

    suspend fun deleteWeekTemplate(userId: String, templateId: String) {
        db.collection("users").document(userId)
            .collection("week_templates")
            .document(templateId).delete().await()
    }

    suspend fun applyDayTemplateToDays(userId: String, template: DayTemplate, days: List<String>) {
        val userRef = db.collection("users").document(userId)
        val todayName = getTodayName()

        for (day in days) {
            // Update weekday_activities
            val weekdayRef = userRef.collection("weekday_activities").document(day)
            weekdayRef.set(mapOf("activities" to template.activities)).await()

            // If today, update 'today' collection
            if (day.equals(todayName, ignoreCase = true)) {
                val todayCollection = userRef.collection("today")

                // Clear old activities
                val oldDocs = todayCollection.get().await()
                for (doc in oldDocs.documents) {
                    doc.reference.delete().await()
                }

                // Add new activities with docRef.id as the activity.id
                for (activity in template.activities) {
                    val docRef = todayCollection.document() // auto-generated ID
                    val activityWithId = activity.copy(id = docRef.id)
                    docRef.set(activityWithId).await()
                }
            }
        }
    }


    suspend fun applyWeekTemplate(userId: String, template: WeekTemplate) {
        val userRef = db.collection("users").document(userId)
        val todayName = getTodayName()

        for ((day, activities) in template.weekMap) {
            // Update weekday_activities
            val weekdayRef = userRef.collection("weekday_activities").document(day)
            weekdayRef.set(mapOf("activities" to activities)).await()

            // If today, update the 'today' collection
            if (day.equals(todayName, ignoreCase = true)) {
                val todayCollection = userRef.collection("today")

                // Clear old activities
                val oldDocs = todayCollection.get().await()
                for (doc in oldDocs.documents) {
                    doc.reference.delete().await()
                }

                // Add new activities with docRef.id as the activity.id
                for (activity in activities) {
                    val docRef = todayCollection.document()
                    val activityWithId = activity.copy(id = docRef.id)
                    docRef.set(activityWithId).await()
                }
            }
        }
    }

    private fun getTodayName(): String {
        return java.time.LocalDate.now()
            .dayOfWeek
            .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH)
    }

    suspend fun updateDayTemplate(userId: String, template: DayTemplate) {
        db.collection("users").document(userId)
            .collection("day_templates")
            .document(template.templateId)
            .set(template).await()
    }

    suspend fun updateWeekTemplate(userId: String, template: WeekTemplate) {
        db.collection("users").document(userId)
            .collection("week_templates")
            .document(template.templateId)
            .set(template).await()
    }

    suspend fun addDayTemplate(userId: String, template: DayTemplate): DayTemplate {
        val docRef = db.collection("users").document(userId)
            .collection("day_templates").document()
        val templateWithId = template.copy(templateId = docRef.id)
        docRef.set(templateWithId).await()
        return templateWithId
    }

    suspend fun addWeekTemplate(userId: String, template: WeekTemplate): WeekTemplate {
        val docRef = db.collection("users").document(userId)
            .collection("week_templates").document()
        val templateWithId = template.copy(templateId = docRef.id)
        docRef.set(templateWithId).await()
        return templateWithId
    }

}
