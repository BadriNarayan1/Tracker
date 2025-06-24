package com.example.tracker.presentation.templates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tracker.Injection
import com.example.tracker.domain.model.Activity
import com.example.tracker.domain.model.DayTemplate
import com.example.tracker.domain.model.WeekTemplate
import com.example.tracker.presentation.add_activity.ActivityInputViewModel
import com.example.tracker.presentation.add_activity.ActivityInputViewModelFactory
import com.example.tracker.presentation.add_activity.AddOrUpdateActivityDialog
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale


@Composable
fun TemplateInputScreen(
    userId: String,
    viewModelKey: String,
    templateId: String? = null,
    onSaveDayTemplate: (DayTemplate) -> Unit,
    onSaveWeekTemplate: (WeekTemplate) -> Unit
) {
    val viewModel: TemplateInputViewModel = viewModel(
        key = viewModelKey,
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TemplateInputViewModel(userId) as T
            }
        }
    )

    val factory = remember { ActivityInputViewModelFactory(Injection.instance(), userId) }
    val activityViewModel: ActivityInputViewModel = viewModel(factory = factory)

    LaunchedEffect(templateId) {
        viewModel.loadTemplateIfNeeded(templateId)
    }


    val templateType = viewModel.templateType.collectAsState()
    val name = viewModel.name.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(onClick = { viewModel.setTemplateType("Day") }, modifier = Modifier.padding(4.dp)) {
                Text("Day Template")
            }
            Button(onClick = { viewModel.setTemplateType("Week") }, modifier = Modifier.padding(4.dp)) {
                Text("Week Template")
            }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = name.value,
            onValueChange = { viewModel.setName(it) },
            label = { Text("Template Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
        if (templateType.value == "Day") {
            DayTemplateForm(viewModel, activityViewModel, onSaveDayTemplate)
        } else {
            WeekTemplateForm(viewModel, activityViewModel, onSaveWeekTemplate)
        }
    }
}

@Composable
fun DayTemplateForm(viewModel: TemplateInputViewModel, activityViewModel: ActivityInputViewModel, onSave: (DayTemplate) -> Unit) {
    val activities = viewModel.dayActivities.collectAsState()
    val showDialog = viewModel.showDialog

    val sortedActivities = activities.value.sortedBy {
        LocalTime.parse(it.startTime, DateTimeFormatter.ofPattern("hh:mm a"))
    }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        items(sortedActivities) { activity ->
            ActivityRow(activity) { viewModel.removeDayActivity(activity) }
        }

        item { Spacer(Modifier.height(8.dp)) }
        item {
            Button(onClick = { viewModel.showDialog = true }) {
                Text("Add Activity")
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
        item {
            Button(onClick = { onSave(viewModel.buildDayTemplate()) }) {
                Text("Save Day Template")
            }
        }
    }

        if (showDialog) {
            AddOrUpdateActivityDialog(
                viewModel = activityViewModel,
                onDismiss = { viewModel.showDialog = false },
                onSave = { viewModel.addDayActivity(it) }
            )
        }
}

@Composable
fun WeekTemplateForm(viewModel: TemplateInputViewModel, activityViewModel: ActivityInputViewModel, onSave: (WeekTemplate) -> Unit) {
    val selectedDayIndex = viewModel.selectedDayIndex
    val weekMap = viewModel.weekMap.collectAsState()
    val showDialog = viewModel.showDialog
    val days = viewModel.weekdays
    val currentDay = days.getOrNull(selectedDayIndex) ?: days.first()
    val currentActivities = weekMap.value[currentDay] ?: emptyList()

    val sortedActivities = currentActivities.sortedBy {
        LocalTime.parse(it.startTime, DateTimeFormatter.ofPattern("hh:mm a"))
    }


    Column() {
        ScrollableTabRow(selectedTabIndex = selectedDayIndex) {
            days.forEachIndexed { i, day ->
                Tab(
                    selected = selectedDayIndex == i,
                    onClick = { viewModel.selectedDayIndex = i },
                    text = { Text(day) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sortedActivities) { activity ->
                ActivityRow(activity) {
                    viewModel.removeWeekActivity(currentDay, activity)
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Button(onClick = { viewModel.showDialog = true }) {
                    Text("Add Activity to $currentDay")
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    onSave(viewModel.buildWeekTemplate())
                }) {
                    Text("Save Week Template")
                }
            }
        }

    }

    if (showDialog) {
        AddOrUpdateActivityDialog(
            viewModel = activityViewModel,
            onDismiss = { viewModel.showDialog = false },
            onSave = {
                viewModel.addWeekActivity(currentDay, it)
            }
        )
    }
}


@Composable
fun ActivityRow(activity: Activity, onDelete: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${activity.description} | ${activity.startTime} - ${activity.endTime}", modifier = Modifier.weight(1f))
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Activity")
        }
    }
}

//@Composable
//fun ActivityInputDialog(onDismiss: () -> Unit, onConfirm: (Activity) -> Unit) {
//    var description by remember { mutableStateOf("") }
//    var start by remember { mutableStateOf("") }
//    var end by remember { mutableStateOf("") }
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text("Add Activity") },
//        text = {
//            Column {
//                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
//                OutlinedTextField(value = start, onValueChange = { start = it }, label = { Text("Start Time") })
//                OutlinedTextField(value = end, onValueChange = { end = it }, label = { Text("End Time") })
//            }
//        },
//        confirmButton = {
//            Button(onClick = {
//                onConfirm(
//                    Activity(
//                        id = UUID.randomUUID().toString(),
//                        description = description,
//                        startTime = start,
//                        endTime = end,
//                        status = Status.PENDING,
//                        score = 0
//                    )
//                )
//                onDismiss()
//            }) {
//                Text("Add")
//            }
//        },
//        dismissButton = {
//            TextButton(onClick = onDismiss) { Text("Cancel") }
//        }
//    )
//}
