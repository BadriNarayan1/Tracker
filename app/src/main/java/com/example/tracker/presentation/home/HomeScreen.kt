package com.example.tracker.presentation.home


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.tracker.Screen
import com.example.tracker.domain.model.ScheduledActivity
import com.example.tracker.presentation.sign_in.UserData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userData: UserData?,
    onSignOut: () -> Unit,
    navController: NavHostController,
    activities: List<ScheduledActivity>
) {
    var showMenu by remember { mutableStateOf(false) }

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
                navController.navigate(Screen.AddActivity.route)
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Activity")
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(activities) { activity ->
                ActivityItem(activity = activity)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ActivityItem(activity: ScheduledActivity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "${activity.startTime} - ${activity.endTime}", style = MaterialTheme.typography.labelMedium)
            Text(text = activity.type, style = MaterialTheme.typography.titleMedium)
            Text(text = activity.description, style = MaterialTheme.typography.bodyMedium)
            if (!activity.youtubeLink.isNullOrBlank()) {
                Text(text = "🎥 YouTube: ${activity.youtubeLink}", style = MaterialTheme.typography.bodySmall)
            }
            if (!activity.spotifyLink.isNullOrBlank()) {
                Text(text = "🎵 Spotify: ${activity.spotifyLink}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// File: DummyData.kt (or within HomeScreen.kt for preview/testing)

val dummyActivities = listOf(
    ScheduledActivity(
        id = "1",
        startTime = "08:00 AM",
        endTime = "09:00 AM",
        type = "Workout",
        description = "Morning strength training session",
        spotifyLink = "https://open.spotify.com/track/example1"
    ),
    ScheduledActivity(
        id = "2",
        startTime = "09:30 AM",
        endTime = "10:30 AM",
        type = "Study",
        description = "Read Data Structures chapter 4",
        youtubeLink = "https://youtube.com/watch?v=example2"
    ),
    ScheduledActivity(
        id = "3",
        startTime = "11:00 AM",
        endTime = "12:00 PM",
        type = "Break",
        description = "Watch a fun YouTube video",
        youtubeLink = "https://youtube.com/watch?v=example3",
        spotifyLink = "https://open.spotify.com/track/example3"
    )
)


//@Preview(showBackground = true)
//@Composable
//fun HomeScreenPreview() {
//    HomeScreen(
//        navController = rememberNavController(),
//        activities = dummyActivities
//    )
//}

