package com.example.engine

import com.example.BuildConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

@Serializable
data class GenerateContentRequest(
    val systemInstruction: Content? = null,
    val contents: List<Content>,
    val tools: List<JsonObject>? = null,
    val generationConfig: GenerationConfig? = null
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null
)

@Serializable
data class GenerationConfig(
    val temperature: Float? = null
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@Serializable
data class Candidate(
    val content: Content? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-flash-lite-latest:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

class GeminiClient(private val context: android.content.Context) {
    suspend fun isBrowser(packageName: String): String = withContext(Dispatchers.IO) {
        val settings = com.example.data.SettingsManager(context)
        val apiKey = settings.geminiApiKey.value.takeIf { it.isNotBlank() } ?: "AIzaSyAWkVO_Q9PHEv-F1pYUQhELfQbHc6rskfs"
        val prompt = "Analyze the Android app with package name '$packageName'. Does it function as a web browser or contain an unrestricted web-view that allows users to search the web or freely browse different URLs? Respond with ONLY the word YES or NO. Do not include any explanations, reasoning, or additional text whatsoever."
        val request = GenerateContentRequest(
            systemInstruction = Content(parts = listOf(Part(text = "You are a specialized classifier API. You must evaluate the provided Android app package name and determine if it functions as a web browser, or contains an unrestricted web-view that lets a user bypass filters or freely search the web. You must respond with EXACTLY and ONLY the word 'YES' or 'NO'. No explanations, no preamble, no markdown, no punctuation. ONLY YES or NO."))),
            contents = listOf(Content(
                parts = listOf(Part(text = prompt))
            )),
            tools = null,
            generationConfig = GenerationConfig(temperature = 0.0f)
        )
        var attempt = 0
        val maxAttempts = 3
        var lastErrorUrl: String? = null
        
        while (attempt < maxAttempts) {
            try {
                val response = RetrofitClient.service.generateContent(apiKey, request)
                return@withContext response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()?.uppercase() ?: "ERROR: Empty Response"
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 429) {
                    attempt++
                    if (attempt < maxAttempts) {
                        kotlinx.coroutines.delay(1000L * attempt)
                        continue
                    }
                }
                return@withContext "ERROR: HTTP ${e.code()} ${e.message()}"
            } catch (e: Exception) {
                return@withContext "ERROR: ${e.message}"
            }
        }
        return@withContext "ERROR: HTTP 429 Too Many Requests"
    }
}
