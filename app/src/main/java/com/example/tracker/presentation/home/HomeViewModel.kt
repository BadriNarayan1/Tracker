package com.example.tracker.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracker.data.repository.ActivityRepository
import com.example.tracker.domain.model.Activity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val userId: String) : ViewModel() {
    private val repository = ActivityRepository()

    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadActivities() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _activities.value = repository.getTodayActivities(userId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load activities: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateActivity(activity: Activity) {
        viewModelScope.launch {
            try {
                repository.updateActivityStatus(userId, activity)
                loadActivities()
            } catch (e: Exception) {
                _error.value = "Failed to update activity: ${e.message}"
            }
        }
    }

    fun addActivity(activity: Activity) {
        viewModelScope.launch {
            try {
                repository.addActivity(userId, activity)
                loadActivities()
            } catch (e: Exception) {
                _error.value = "Failed to add activity: ${e.message}"
            }
        }
    }

    fun deleteActivity(activityId: String) {
        viewModelScope.launch {
            repository.deleteActivity(userId, activityId)
            loadActivities()
        }
    }
}
