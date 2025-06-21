package com.example.tracker.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.tracker.Injection
import com.example.tracker.Screen
import com.example.tracker.domain.model.Activity
import com.example.tracker.domain.model.Status
import com.example.tracker.presentation.add_activity.ActivityInputViewModel
import com.example.tracker.presentation.add_activity.ActivityInputViewModelFactory
import com.example.tracker.presentation.add_activity.AddOrUpdateActivityDialog
import com.example.tracker.presentation.sign_in.UserData
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userData: UserData?,
    onSignOut: () -> Unit,
    navController: NavHostController,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(userData?.userId ?: ""))
) {
    val factory = remember { userData?.let {
        ActivityInputViewModelFactory(
            Injection.instance(),
            it.userId
        )
    } }
    val activityViewModel: ActivityInputViewModel = viewModel(factory = factory)
    var showMenu by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedActivity by remember { mutableStateOf<Activity?>(null) }

    val activities by viewModel.activities.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val sortedActivities = activities.sortedBy {
        LocalTime.parse(it.startTime, DateTimeFormatter.ofPattern("hh:mm a"))
    }
    LaunchedEffect(Unit) {
        viewModel.loadActivities()
    }

    if (showAddDialog) {
        AddOrUpdateActivityDialog(
            viewModel = activityViewModel,
            initialActivity = selectedActivity,
            onDismiss = { showAddDialog = false },
            onSave = {
                if (selectedActivity != null) {
                    viewModel.updateActivity(it) //  update
                } else {
                    viewModel.addActivity(it)    //  new
                }
                showAddDialog = false
                selectedActivity = null
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Today's Schedule") },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile"
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sign Out") },
                                onClick = {
                                    showMenu = false
                                    onSignOut()
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedActivity = null
                showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Activity")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (error != null) {
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(sortedActivities) { activity ->
                    ActivityItem(
                        activity = activity,
                        onStatusChange = { viewModel.updateActivity(it) },
                        onDelete = { viewModel.deleteActivity(it.id) },
                        onClick = { //  New: open dialog with existing activity
                            selectedActivity = activity
                            showAddDialog = true
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun ActivityItem(
    activity: Activity,
    onStatusChange: (Activity) -> Unit,
    onDelete: (Activity) -> Unit,
    onClick: (Activity) -> Unit,
    modifier: Modifier = Modifier
) {
    var localStatus by remember { mutableStateOf(activity.status) }
    var score by remember { mutableStateOf(activity.score) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(activity) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${activity.startTime} - ${activity.endTime}", style = MaterialTheme.typography.labelMedium)
                IconButton(onClick = { onDelete(activity) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Activity")
                }
            }

            Text(activity.category, style = MaterialTheme.typography.titleMedium)
            Text(activity.description, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))

            // Status Toggle Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
//                Text("Status: ")

                // Completed button
                Button(
                    onClick = {
                        localStatus = Status.COMPLETED
                        onStatusChange(activity.copy(status = localStatus))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (localStatus == Status.COMPLETED) Color(0xFF4CAF50) else Color.LightGray,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20)
                ) {
                    Text("Completed")
                }

                // Not Completed button
                Button(
                    onClick = {
                        localStatus = Status.NOT_COMPLETED
                        onStatusChange(activity.copy(status = localStatus))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (localStatus == Status.NOT_COMPLETED) Color(0xFFF44336) else Color.LightGray,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20)
                ) {
                    Text("Not Completed")
                }
            }

            if (localStatus == Status.COMPLETED) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Rate Activity")
                Slider(
                    value = score.toFloat(),
                    onValueChange = {
                        score = it.toInt()
                        onStatusChange(activity.copy(score = score))
                    },
                    valueRange = 1f..10f,
                    steps = 8
                )
                Text("Score: $score")
            }
        }
    }
}



class HomeViewModelFactory(private val userId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(userId) as T
    }
}



// File: DummyData.kt (or within HomeScreen.kt for preview/testing)

//val dummyActivities = listOf(
//    ScheduledActivity(
//        id = "1",
//        startTime = "08:00 AM",
//        endTime = "09:00 AM",
//        type = "Workout",
//        description = "Morning strength training session",
//        spotifyLink = "https://open.spotify.com/track/example1"
//    ),
//    ScheduledActivity(
//        id = "2",
//        startTime = "09:30 AM",
//        endTime = "10:30 AM",
//        type = "Study",
//        description = "Read Data Structures chapter 4",
//        youtubeLink = "https://youtube.com/watch?v=example2"
//    ),
//    ScheduledActivity(
//        id = "3",
//        startTime = "11:00 AM",
//        endTime = "12:00 PM",
//        type = "Break",
//        description = "Watch a fun YouTube video",
//        youtubeLink = "https://youtube.com/watch?v=example3",
//        spotifyLink = "https://open.spotify.com/track/example3"
//    )
//)


//@Preview(showBackground = true)
//@Composable
//fun HomeScreenPreview() {
//    HomeScreen(
//        navController = rememberNavController(),
//        activities = dummyActivities
//    )
//}

