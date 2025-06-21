package com.example.tracker.presentation.templates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tracker.domain.model.DayTemplate
import com.example.tracker.domain.model.WeekTemplate
import com.example.tracker.presentation.templates.TemplateMode
import com.example.tracker.presentation.templates.TemplateViewModel
import com.example.tracker.presentation.templates.TemplateViewModelFactory

@Composable
fun TemplateScreen(
    userId: String,
    onAddTemplate: () -> Unit,
    onEditTemplate: (templateId: String) -> Unit,
    viewModel: TemplateViewModel
) {
    val mode by viewModel.templateMode.collectAsState()
    val dayTemplates by viewModel.dayTemplates.collectAsState()
    val weekTemplates by viewModel.weekTemplates.collectAsState()
    val templates = if (mode == TemplateMode.DAY) dayTemplates else weekTemplates

    var showDayDialog by remember { mutableStateOf(false) }
    var selectedDayTemplate by remember { mutableStateOf<DayTemplate?>(null) }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Templates",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ModeToggle(mode = mode, onModeChange = viewModel::onModeChange)

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(templates) { template ->
                    when (mode) {
                        TemplateMode.DAY -> {
                            val dayTemplate = template
                            if (dayTemplate is DayTemplate) {
                                TemplateCard(
                                    name = dayTemplate.name,
                                    onEdit = { onEditTemplate(dayTemplate.templateId) },
                                    onDelete = { viewModel.onDeleteTemplate(dayTemplate) },
                                    onApply = {
                                        selectedDayTemplate = dayTemplate
                                        showDayDialog = true
                                    }
                                )
                            }
                        }

                        TemplateMode.WEEK -> {
                            val weekTemplate = template
                            if (weekTemplate is WeekTemplate) {
                                TemplateCard(
                                    name = weekTemplate.name,
                                    onEdit = { onEditTemplate(weekTemplate.templateId) },
                                    onDelete = { viewModel.onDeleteTemplate(weekTemplate) },
                                    onApply = { viewModel.applyWeekTemplate(weekTemplate) }
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { onAddTemplate() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Template")
        }

        if (showDayDialog && selectedDayTemplate != null) {
            WeekdayPickerDialog(
                onDismiss = { showDayDialog = false },
                onConfirm = { selectedDays ->
                    selectedDayTemplate?.let {
                        viewModel.applyDayTemplate(it, selectedDays)
                    }
                    showDayDialog = false
                }
            )
        }
    }
}

@Composable
fun TemplateCard(
    name: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onApply: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onApply) {
                Text("Apply")
            }
        }
    }
}

@Composable
fun ModeToggle(mode: TemplateMode, onModeChange: (TemplateMode) -> Unit) {
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        TemplateMode.values().forEach {
            Button(
                onClick = { onModeChange(it) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (it == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(it.name)
            }
        }
    }
}

@Composable
fun WeekdayPickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val weekdays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val selectedDays = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedDays.toList()) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Select Days") },
        text = {
            Column {
                weekdays.forEach { day ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = day in selectedDays,
                            onCheckedChange = {
                                if (it) selectedDays.add(day) else selectedDays.remove(day)
                            }
                        )
                        Text(day)
                    }
                }
            }
        }
    )
}
