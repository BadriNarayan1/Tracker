package com.example.tracker.presentation.templates

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tracker.data.repository.TemplateRepository
import com.example.tracker.domain.model.Activity
import com.example.tracker.domain.model.DayTemplate
import com.example.tracker.domain.model.WeekTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TemplateInputViewModel(
    private val userId: String,
    private val repository: TemplateRepository = TemplateRepository()
) : ViewModel() {

    private var lastLoadedId: String? = null
    private var templateId: String = ""
    val weekdays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    var selectedDayIndex by mutableStateOf(0)
    var showDialog by mutableStateOf(false)

    private val _templateType = MutableStateFlow("Day")
    val templateType: StateFlow<String> = _templateType

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _dayActivities = MutableStateFlow<List<Activity>>(emptyList())
    val dayActivities: StateFlow<List<Activity>> = _dayActivities

    private val _weekMap = MutableStateFlow<Map<String, List<Activity>>>(emptyMap())
    val weekMap: StateFlow<Map<String, List<Activity>>> = _weekMap

    fun loadTemplateIfNeeded(templateId: String?) {
        if (templateId == null || templateId == lastLoadedId) return

        viewModelScope.launch {
            val day = repository.getDayTemplateById(userId, templateId)
            if (day != null) {
                this@TemplateInputViewModel.templateId = day.templateId
                _templateType.value = "Day"
                _name.value = day.name
                _dayActivities.value = day.activities
            } else {
                val week = repository.getWeekTemplateById(userId, templateId)
                if (week != null) {
                    this@TemplateInputViewModel.templateId = week.templateId
                    _templateType.value = "Week"
                    _name.value = week.name
                    _weekMap.value = week.weekMap
                }
            }
            lastLoadedId = templateId
        }
    }

    fun setTemplateType(type: String) { _templateType.value = type }
    fun setName(newName: String) { _name.value = newName }

    fun addDayActivity(activity: Activity) {
        _dayActivities.value += activity
    }

    fun removeDayActivity(activity: Activity) {
        _dayActivities.value -= activity
    }

    fun addWeekActivity(day: String, activity: Activity) {
        _weekMap.value = _weekMap.value.toMutableMap().apply {
            put(day, (this[day] ?: emptyList()) + activity)
        }
    }

    fun removeWeekActivity(day: String, activity: Activity) {
        _weekMap.value = _weekMap.value.toMutableMap().apply {
            put(day, (this[day] ?: emptyList()) - activity)
        }
    }

    fun buildDayTemplate(): DayTemplate = DayTemplate(
        templateId = templateId,
        name = _name.value,
        activities = _dayActivities.value
    )

    fun buildWeekTemplate(): WeekTemplate = WeekTemplate(
        templateId = templateId,
        name = _name.value,
        weekMap = _weekMap.value
    )
}



