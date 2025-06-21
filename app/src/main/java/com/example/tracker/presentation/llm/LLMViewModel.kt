import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tracker.data.repository.GeminiRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LLMViewModel(
    private val repo: GeminiRepository,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _response = MutableStateFlow("")
    val response = _response.asStateFlow()

    private var activityText: String? = null

    fun loadAllActivities(uid: String) {
        viewModelScope.launch {
            try {
                val activitiesRef = firestore.collection("users").document(uid).collection("activities")
                val allDocs = activitiesRef.get().await()

                activityText = buildString {
                    for (doc in allDocs.documents) {
                        val date = doc.id
                        val list = doc.get("list") as? List<Map<String, Any>> ?: continue

                        append("📅 $date\n")
                        list.forEach { act ->
                            val category = act["category"] ?: ""
                            val desc = act["description"] ?: ""
                            val start = act["startTime"] ?: ""
                            val end = act["endTime"] ?: ""
                            val score = act["score"] ?: 0
                            append("- [$category] $desc from $start to $end, score: $score\n")
                        }
                        append("\n")
                    }
                }

            } catch (e: Exception) {
                _response.value = "Error loading activity data: ${e.message}"
            }
        }
    }

    fun askAboutAllActivities(userQuestion: String) {
        viewModelScope.launch {
            val activityTextCopy = activityText

            if (activityTextCopy.isNullOrBlank()) {
                _response.value = "Please load activities first."
                return@launch
            }

            val prompt = """
            Below is the list of my past activities:

            $activityTextCopy

            Question: $userQuestion
        """.trimIndent()

            try {
                val result = repo.askGemini(prompt)
                _response.value = result
            } catch (e: Exception) {
                _response.value = "Error querying Gemini: ${e.message}"
            }
        }
    }

}

class LLMViewModelFactory(
    private val repo: GeminiRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LLMViewModel::class.java)) {
            return LLMViewModel(repo, FirebaseFirestore.getInstance()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


