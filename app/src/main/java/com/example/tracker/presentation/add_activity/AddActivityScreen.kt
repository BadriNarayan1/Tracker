package com.example.tracker.presentation.add_activity

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tracker.domain.model.Activity
import com.example.tracker.domain.model.Status
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrUpdateActivityDialog(
    viewModel: ActivityInputViewModel,
    initialActivity: Activity? = null,
    onDismiss: () -> Unit,
    onSave: (Activity) -> Unit
) {
    val context = LocalContext.current
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    var startTime by remember {
        mutableStateOf(
            initialActivity?.startTime?.let { LocalTime.parse(it, timeFormatter) }
                ?: LocalTime.now()
        )
    }
    var endTime by remember {
        mutableStateOf(
            initialActivity?.endTime?.let { LocalTime.parse(it, timeFormatter) }
                ?: LocalTime.now().plusMinutes(30)
        )
    }

    val activityCategories by viewModel.activityTypes.collectAsState()

    var selectedCategory by remember { mutableStateOf(initialActivity?.category ?: "") }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    var description by remember { mutableStateOf(initialActivity?.description ?: "") }

    // These will be hidden when adding
    var score by remember { mutableStateOf(initialActivity?.score ?: 0) }
    var status by remember { mutableStateOf(initialActivity?.status ?: Status.PENDING) }

    val showTimePicker: (LocalTime, (LocalTime) -> Unit) -> Unit = { time, onTimeSelected ->
        TimePickerDialog(
            context,
            { _, hour, minute -> onTimeSelected(LocalTime.of(hour, minute)) },
            time.hour,
            time.minute,
            false
        ).show()
    }

    // Add category dialog
    if (showAddCategoryDialog) {
        var newCategory by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Add New Category") },
            text = {
                OutlinedTextField(
                    value = newCategory,
                    onValueChange = { newCategory = it },
                    label = { Text("New Category") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newCategory.isNotBlank()) {
                        viewModel.addCategory(newCategory)
                        selectedCategory = newCategory
                        showAddCategoryDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialActivity == null) "Add Activity" else "Edit Activity") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Time pickers
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { showTimePicker(startTime) { startTime = it } }) {
                        Text("Start: ${startTime.format(timeFormatter)}")
                    }
                    Button(onClick = { showTimePicker(endTime) { endTime = it } }) {
                        Text("End: ${endTime.format(timeFormatter)}")
                    }
                }

                // Category Dropdown
                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )

                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        activityCategories.forEach { type ->
                            DropdownMenuItem(text = { Text(type) }, onClick = {
                                selectedCategory = type
                                expanded = false
                            })
                        }
                        Divider()
                        DropdownMenuItem(text = { Text("➕ Add New") }, onClick = {
                            expanded = false
                            showAddCategoryDialog = true
                        })
                    }
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

//                if (initialActivity != null) {
//                    // Only show when editing
//                    // Score
//                    OutlinedTextField(
//                        value = score.toString(),
//                        onValueChange = {
//                            score = it.toIntOrNull()?.coerceIn(0, 5) ?: 0
//                        },
//                        label = { Text("Score (0 to 5)") },
//                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
//                        modifier = Modifier.fillMaxWidth()
//                    )
//
//                    // Status dropdown
//                    var statusExpanded by remember { mutableStateOf(false) }
//
//                    ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = !statusExpanded }) {
//                        OutlinedTextField(
//                            value = status.name,
//                            onValueChange = {},
//                            readOnly = true,
//                            label = { Text("Status") },
//                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
//                            modifier = Modifier.menuAnchor()
//                        )
//
//                        ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
//                            Status.values().forEach {
//                                DropdownMenuItem(text = { Text(it.name) }, onClick = {
//                                    status = it
//                                    statusExpanded = false
//                                })
//                            }
//                        }
//                    }
//                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (selectedCategory.isNotBlank()) {
                    onSave(
                        Activity(
                            id = initialActivity?.id ?: "",
                            category = selectedCategory,
                            startTime = startTime.format(timeFormatter),
                            endTime = endTime.format(timeFormatter),
                            description = description,
                            score = if (initialActivity == null) 0 else score,
                            status = if (initialActivity == null) Status.PENDING else status
                        )
                    )
                    onDismiss()
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

