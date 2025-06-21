package com.example.tracker.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiRepository(apiKey: String) {
    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )

    suspend fun askGemini(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val response = model.generateContent(prompt)
            response.text ?: "No response"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
