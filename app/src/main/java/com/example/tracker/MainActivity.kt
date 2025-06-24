package com.example.tracker

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.tracker.presentation.profile.ProfileScreen
import com.example.tracker.presentation.sign_in.GoogleAuthUiClient
import com.example.tracker.presentation.sign_in.SignInScreen
import com.example.tracker.presentation.sign_in.SignInViewModel
import com.example.tracker.ui.theme.TrackerTheme
import com.example.tracker.worker.DailyMidnightWorker
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    private val googleAuthClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
//        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
//        if (currentUserId != null) {
//            seed60ActivityDocuments(Firebase.firestore, currentUserId)
//        }
//        if (currentUserId != null) {
////            seedFakeProgressData(Injection.instance(), currentUserId)
//            val firestore = FirebaseFirestore.getInstance()
//
//            val collectionRef = firestore
//                .collection("users")
//                .document(currentUserId)
//                .collection("progress")
//
//            weeklyProgress.forEach { entry ->
//                collectionRef.add(entry)
//            }
//        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        scheduleMidnightWorker(applicationContext)
//        val request = OneTimeWorkRequestBuilder<DailyMidnightWorker>().build()
//        WorkManager.getInstance(this).enqueue(request)
        setContent {
            TrackerTheme {
                MainView(googleAuthClient)
            }
        }
    }
}

private fun scheduleMidnightWorker(context: Context) {
    val midnight = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_YEAR, 1)
    }

    val delay = midnight.timeInMillis - System.currentTimeMillis()

    val request = PeriodicWorkRequestBuilder<DailyMidnightWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "dailyMidnightJob",
        ExistingPeriodicWorkPolicy.UPDATE, // replaces if already exists
        request
    )
}



fun seed60ActivityDocuments(firestore: FirebaseFirestore, userId: String) {
    val categories = listOf("Study", "Work", "Exercise", "Reading", "Leisure", "Coding")
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    val today = LocalDate.now()

    val userActivitiesRef = firestore
        .collection("users")
        .document(userId)
        .collection("activities")

    for (i in 0 until 60) {
        val date = today.minusDays(i.toLong()).toString() // e.g., "2025-06-20"
        val activityList = mutableListOf<Map<String, Any>>()

        var currentTime = LocalTime.of(7, 0) // Start day at 7:00 AM
        val activitiesToday = Random.nextInt(3, 6)

        repeat(activitiesToday) {
            val category = categories.random()
            val duration = listOf(30, 45, 60, 75, 90).random() // minutes
            val endTime = currentTime.plusMinutes(duration.toLong())
            val score = Random.nextInt(5, 11)

            val activity = mapOf(
                "id" to "${System.currentTimeMillis()}-${Random.nextInt()}",
                "category" to category,
                "description" to "Seeded activity",
                "startTime" to currentTime.format(timeFormatter),
                "endTime" to endTime.format(timeFormatter),
                "score" to score,
                "status" to "COMPLETED"
            )

            activityList.add(activity)
            currentTime = endTime.plusMinutes(15)
        }

        val docRef = userActivitiesRef.document(date)
        docRef.set(mapOf("list" to activityList))
    }
}


fun seedFakeProgressData(firestore: FirebaseFirestore, userId: String) {
    val collectionRef = firestore
        .collection("users")
        .document(userId)
        .collection("progress")

    val categories = listOf("Work", "Study", "Exercise", "Leisure", "Reading", "Coding")
    val today = LocalDate.now()

    for (i in 0 until 60) {
        val date = today.minusDays(i.toLong()).toString()

        val categoryWiseTime = categories.associateWith {
            // Hours between 0.5 and 3.0
            (Random.nextDouble(0.5, 3.0)).toFloat()
        }

        val categoryWiseEffort = categories.associateWith {
            // Effort between 1 and 10
            Random.nextInt(1, 11).toFloat()
        }

        val categoryWiseEffortScaledTime = categories.associateWith { category ->
            val hours = categoryWiseTime[category] ?: 0f
            val effort = categoryWiseEffort[category] ?: 1f
            (hours * effort / 10f).coerceAtLeast(0f)
        }

        val entry = hashMapOf(
            "date" to date,
            "categoryWiseTime" to categoryWiseTime,
            "categoryWiseEffortScaledTime" to categoryWiseEffortScaledTime
        )

        collectionRef.add(entry)
    }
}

val weeklyProgress = listOf(
    mapOf(
        "date" to "2025-06-19",
        "categoryWiseTime" to mapOf(
            "Work" to 2.0f,
            "Study" to 1.5f,
            "Exercise" to 1.0f
        ),
        "categoryWiseEffortScaledTime" to mapOf(
            "Work" to 2.0f * 8 / 10f,     // 1.6
            "Study" to 1.5f * 6 / 10f,    // 0.9
            "Exercise" to 1.0f * 7 / 10f  // 0.7
        )
    ),
    mapOf(
        "date" to "2025-06-18",
        "categoryWiseTime" to mapOf(
            "Work" to 2.5f,
            "Reading" to 1.0f
        ),
        "categoryWiseEffortScaledTime" to mapOf(
            "Work" to 2.5f * 9 / 10f,     // 2.25
            "Reading" to 1.0f * 5 / 10f   // 0.5
        )
    ),
    mapOf(
        "date" to "2025-06-17",
        "categoryWiseTime" to mapOf(
            "Leisure" to 1.5f,
            "Coding" to 2.0f
        ),
        "categoryWiseEffortScaledTime" to mapOf(
            "Leisure" to 1.5f * 4 / 10f,  // 0.6
            "Coding" to 2.0f * 8 / 10f    // 1.6
        )
    ),
    mapOf(
        "date" to "2025-06-16",
        "categoryWiseTime" to mapOf(
            "Study" to 2.0f,
            "Coding" to 2.5f
        ),
        "categoryWiseEffortScaledTime" to mapOf(
            "Study" to 2.0f * 6 / 10f,    // 1.2
            "Coding" to 2.5f * 9 / 10f    // 2.25
        )
    ),
    mapOf(
        "date" to "2025-06-15",
        "categoryWiseTime" to mapOf(
            "Work" to 3.0f,
            "Exercise" to 1.0f
        ),
        "categoryWiseEffortScaledTime" to mapOf(
            "Work" to 3.0f * 7 / 10f,     // 2.1
            "Exercise" to 1.0f * 6 / 10f  // 0.6
        )
    ),
    mapOf(
        "date" to "2025-06-14",
        "categoryWiseTime" to mapOf(
            "Reading" to 1.0f,
            "Exercise" to 1.5f
        ),
        "categoryWiseEffortScaledTime" to mapOf(
            "Reading" to 1.0f * 5 / 10f,  // 0.5
            "Exercise" to 1.5f * 8 / 10f  // 1.2
        )
    ),
    mapOf(
        "date" to "2025-06-13",
        "categoryWiseTime" to mapOf(
            "Study" to 2.0f,
            "Leisure" to 1.0f
        ),
        "categoryWiseEffortScaledTime" to mapOf(
            "Study" to 2.0f * 7 / 10f,    // 1.4
            "Leisure" to 1.0f * 4 / 10f   // 0.4
        )
    )
)




@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TrackerTheme {
        Greeting("Android")
    }
}