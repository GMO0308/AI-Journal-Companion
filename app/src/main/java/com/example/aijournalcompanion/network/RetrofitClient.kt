package com.example.aijournalcompanion.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton that creates the Retrofit client used to talk to the backend.
 */
object RetrofitClient {
    // 10.0.2.2 is the special IP address that refers to your PC's localhost
    // from inside the Android Emulator.
    private const val BASE_URL = "http://10.0.2.2:8000/"

    // gemma:2b can take longer than OkHttp's default 10 seconds to reply,
    // especially on the first request while the model loads, so the
    // read timeout is raised to avoid false "timeout" errors.
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
