package com.example.aijournalcompanion.network

import retrofit2.http.Body
import retrofit2.http.POST

/** JSON body sent to the FastAPI backend: { "text": "..." } */
data class AnalyzeRequest(val text: String)

/**
 * JSON returned by the backend: { "emotion": "JOY", "advice": "..." }
 * Fields are nullable because the AI model may occasionally leave one out;
 * the ViewModel replaces missing values with safe defaults.
 */
data class AnalyzeResponse(val emotion: String?, val advice: String?)

/** Retrofit definition of the REST API (inter-process communication). */
interface ApiService {

    /** POST /analyze - send a journal entry and receive emotion + advice. */
    @POST("analyze")
    suspend fun analyzeEntry(
        @Body request: AnalyzeRequest
    ): AnalyzeResponse
}
