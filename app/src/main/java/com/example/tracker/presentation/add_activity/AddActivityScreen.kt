package com.example.tracker.presentation.add_activity

// File: AddActivityScreen.kt
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tracker.domain.model.ScheduledActivity
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityScreen(
    navController: NavController,
    onSave: (ScheduledActivity) -> Unit
) {
    val context = LocalContext.current
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    var startTime by remember { mutableStateOf(LocalTime.now()) }
    var endTime by remember { mutableStateOf(LocalTime.now().plusMinutes(30)) }

    val activityTypes = remember { mutableStateListOf("Work", "Study", "Break", "Exercise") }
    var selectedType by remember { mutableStateOf("") }
    var showAddTypeDialog by remember { mutableStateOf(false) }

    var description by remember { mutableStateOf("") }
    var youtubeLink by remember { mutableStateOf("") }
    var spotifyLink by remember { mutableStateOf("") }
    var pdfLink by remember { mutableStateOf("") }

    val showTimePicker: (LocalTime, (LocalTime) -> Unit) -> Unit = { time, onTimeSelected ->
        val picker = TimePickerDialog(
            context,
            { _, hour, minute -> onTimeSelected(LocalTime.of(hour, minute)) },
            time.hour,
            time.minute,
            false
        )
        picker.show()
    }

    if (showAddTypeDialog) {
        var newType by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddTypeDialog = false },
            title = { Text("Add New Activity Type") },
            text = {
                OutlinedTextField(
                    value = newType,
                    onValueChange = { newType = it },
                    label = { Text("New Type") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newType.isNotBlank()) {
                        activityTypes.add(newType)
                        selectedType = newType
                        showAddTypeDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTypeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Activity") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (selectedType.isNotBlank()) {
                    onSave(
                        ScheduledActivity(
                            id = "",
                            startTime = startTime.format(timeFormatter),
                            endTime = endTime.format(timeFormatter),
                            type = selectedType,
                            description = description,
                            youtubeLink = youtubeLink,
                            spotifyLink = spotifyLink,
                            pdfLink = pdfLink
                        )
                    )
                    navController.popBackStack()
                }
            }) {
                Icon(Icons.Default.Check, contentDescription = "Save")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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

            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Activity Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor()
                )

                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    activityTypes.forEach { type ->
                        DropdownMenuItem(text = { Text(type) }, onClick = {
                            selectedType = type
                            expanded = false
                        })
                    }
                    Divider()
                    DropdownMenuItem(text = { Text("➕ Add New") }, onClick = {
                        expanded = false
                        showAddTypeDialog = true
                    })
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = youtubeLink,
                onValueChange = { youtubeLink = it },
                label = { Text("YouTube Link") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = spotifyLink,
                onValueChange = { spotifyLink = it },
                label = { Text("Spotify Link") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = pdfLink,
                onValueChange = { pdfLink = it },
                label = { Text("PDF Link") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddActivityScreenPreview() {
    AddActivityScreen(
        navController = NavController(LocalContext.current),
        onSave = {}
    )
}


