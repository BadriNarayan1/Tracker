package com.example.tracker.presentation.add_activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tracker.Injection
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ActivityInputViewModel(
    private val firestore: FirebaseFirestore,
    private val  userId: String,
) : ViewModel() {

    private val _activityTypes = MutableStateFlow<List<String>>(emptyList())
    val activityTypes: StateFlow<List<String>> = _activityTypes

    init {
        fetchActivityTypes()
    }

    private fun fetchActivityTypes() {

        val userRef = firestore.collection("users").document(userId).collection("activity_types")
        val defaultRef = firestore.collection("defaults").document("activity_types").collection("activity_types")

        // Fetch both and merge
        viewModelScope.launch {
            try {
                val userSnapshot = userRef.get().await()
                val defaultSnapshot = defaultRef.get().await()

                val allTypes = (userSnapshot.documents + defaultSnapshot.documents)
                    .mapNotNull { it.getString("name") }
                    .distinct()

                _activityTypes.value = allTypes
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun addCategory(name: String) {
        val userCategoryRef = firestore
            .collection("users")
            .document(userId)
            .collection("activity_types")
            .document(name) // Using the name as doc ID for uniqueness

        viewModelScope.launch {
            try {
                userCategoryRef.set(mapOf("name" to name)).await()
                // Optionally: refresh the list
                fetchActivityTypes()
            } catch (e: Exception) {
                // Handle error (e.g. show a snackbar or toast via state)
            }
        }
    }

}

class ActivityInputViewModelFactory(
    private val firestore: FirebaseFirestore,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ActivityInputViewModel(firestore, userId) as T
    }
}

