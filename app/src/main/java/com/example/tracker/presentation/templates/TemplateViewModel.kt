package com.example.tracker.presentation.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tracker.data.repository.TemplateRepository
import com.example.tracker.domain.model.DayTemplate
import com.example.tracker.domain.model.WeekTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class TemplateMode { DAY, WEEK }

class TemplateViewModel(
    private val userId: String,
    private val repository: TemplateRepository = TemplateRepository()
) : ViewModel() {

    private val _templateMode = MutableStateFlow(TemplateMode.DAY)
    val templateMode: StateFlow<TemplateMode> = _templateMode

    private val _dayTemplates = MutableStateFlow<List<DayTemplate>>(emptyList())
    private val _weekTemplates = MutableStateFlow<List<WeekTemplate>>(emptyList())
    val templates: StateFlow<List<Any>> = MutableStateFlow(emptyList())

    val dayTemplates: StateFlow<List<DayTemplate>> get() = _dayTemplates
    val weekTemplates: StateFlow<List<WeekTemplate>> get() = _weekTemplates

    private val _selectedTemplate = MutableStateFlow<Any?>(null)
    val selectedTemplate: StateFlow<Any?> get() = _selectedTemplate

    init {
        loadTemplates()
    }

    fun onModeChange(mode: TemplateMode) {
        _templateMode.value = mode
        loadTemplates()
    }

    private fun loadTemplates() {
        viewModelScope.launch {
            if (_templateMode.value == TemplateMode.DAY) {
                val dayTemplates = repository.getDayTemplates(userId)
                _dayTemplates.value = dayTemplates
                (templates as MutableStateFlow).value = dayTemplates
            } else {
                val weekTemplates = repository.getWeekTemplates(userId)
                _weekTemplates.value = weekTemplates
                (templates as MutableStateFlow).value = weekTemplates
            }
        }
    }

    fun onDeleteTemplate(template: Any) {
        viewModelScope.launch {
            when (template) {
                is DayTemplate -> {
                    repository.deleteDayTemplate(userId, template.templateId)
                    loadTemplates()
                }
                is WeekTemplate -> {
                    repository.deleteWeekTemplate(userId, template.templateId)
                    loadTemplates()
                }
            }
        }
    }

    fun applyDayTemplate(template: DayTemplate, days: List<String>) {
        viewModelScope.launch {
            repository.applyDayTemplateToDays(userId, template, days)
        }
    }

    fun applyWeekTemplate(template: WeekTemplate) {
        viewModelScope.launch {
            repository.applyWeekTemplate(userId, template)
        }
    }


    fun updateTemplate(template: Any) {
        viewModelScope.launch {
            when (template) {
                is DayTemplate -> {
                    if (template.templateId.isBlank()) {
                        repository.addDayTemplate(userId, template)
                    } else {
                        repository.updateDayTemplate(userId, template)
                    }
                }

                is WeekTemplate -> {
                    if (template.templateId.isBlank()) {
                        repository.addWeekTemplate(userId, template)
                    } else {
                        repository.updateWeekTemplate(userId, template)
                    }
                }
            }
            loadTemplates()
        }
    }

    fun loadTemplateById(templateId: String) {
        viewModelScope.launch {
            val day = repository.getDayTemplateById(userId, templateId)
            val week = if (day == null) repository.getWeekTemplateById(userId, templateId) else null
            _selectedTemplate.value = day ?: week
        }
    }
}

class TemplateViewModelFactory(
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TemplateViewModel(userId) as T
    }
}

