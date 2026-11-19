package com.example.aijournalcompanion.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aijournalcompanion.JournalEntry
import com.example.aijournalcompanion.JournalHistory
import com.example.aijournalcompanion.network.AnalyzeRequest
import com.example.aijournalcompanion.network.RetrofitClient
import kotlinx.coroutines.launch

/**
 * Holds the state of the Journal screen and sends entries to the backend.
 */
class JournalViewModel : ViewModel() {

    var journalText by mutableStateOf("")
    var isAnalyzing by mutableStateOf(false)
    var analysisResult by mutableStateOf<JournalEntry?>(null)
    var errorMessage by mutableStateOf<String?>(null)

    /** Emotions the backend is asked to return. Anything else becomes NEUTRAL. */
    private val validEmotions = setOf("JOY", "SADNESS", "ANGER", "FEAR", "SURPRISE")

    /** Sends the journal text to FastAPI (POST /analyze) and stores the result. */
    fun analyzeJournal() {
        if (journalText.isBlank() || isAnalyzing) return

        isAnalyzing = true
        errorMessage = null
        analysisResult = null

        viewModelScope.launch {
            try {
                val text = journalText.trim()
                val response = RetrofitClient.instance.analyzeEntry(AnalyzeRequest(text))

                // Normalise the AI output so sorting, searching and the chart stay consistent
                val emotion = response.emotion?.trim()?.uppercase()
                    ?.takeIf { it in validEmotions } ?: "NEUTRAL"
                val advice = response.advice?.trim()?.takeIf { it.isNotEmpty() }
                    ?: "Take a moment for yourself today."

                val newEntry = JournalEntry(text = text, emotion = emotion, advice = advice)
                analysisResult = newEntry

                // Save to the in-memory history
                JournalHistory.addEntry(newEntry)

            } catch (e: Exception) {
                errorMessage = "Couldn't reach the AI. Check that the backend (port 8000) " +
                        "and Ollama are running.\n\nDetails: ${e.message}"
            } finally {
                isAnalyzing = false
            }
        }
    }

    /** Clears only the text box (Clear button). */
    fun clearText() {
        journalText = ""
        errorMessage = null
    }

    /** Clears everything so the user can write a new entry. */
    fun reset() {
        journalText = ""
        analysisResult = null
        errorMessage = null
    }
}
