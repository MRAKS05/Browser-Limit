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
        val apiKey = settings.geminiApiKey.value.takeIf { it.isNotBlank() } ?: "***REMOVED***"
        
        var playStoreInfo = ""
        try {
            val url = java.net.URL("https://play.google.com/store/apps/details?id=$packageName&hl=en")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "GET"
            // Set User-Agent so we don't get blocked
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            if (conn.responseCode == 200) {
                val html = conn.inputStream.bufferedReader().use { it.readText() }
                val titleRegex = """<title>(.*?)</title>""".toRegex()
                val metaRegex = """<meta name="description" content="([^"]+)">""".toRegex()
                val titleMatch = titleRegex.find(html)
                val descMatch = metaRegex.find(html)
                
                val title = titleMatch?.groupValues?.getOrNull(1) ?: packageName
                val desc = descMatch?.groupValues?.getOrNull(1) ?: ""
                if (desc.isNotEmpty()) {
                    playStoreInfo = "\nApp Name: $title\nPlay Store Description:\n\"\"\"\n$desc\n\"\"\""
                }
            }
        } catch (e: Exception) {
            // Ignore network errors, fall back to packageName alone
        }

        val prompt = """
            You are an app capability detector. Your job is to determine if the Android app with package name '$packageName' can be used to browse the internet freely.$playStoreInfo
            
            Analyze the app's known features and answer YES if the app has ANY of these:
            1. Can open and render arbitrary web URLs
            2. Contains a built-in WebView or browser component
            3. User can type/paste a URL and visit any website
            4. File manager, downloader, or media app with web browsing built in
            5. Social or utility app with an in-app browser
            6. App description mentions: "browser", "browse", "webview", "open URL", "visit websites", "web experience", "built-in browser"
            
            Answer NO only if NONE of the above are true.
            Reply with only one word: YES or NO.
        """.trimIndent()
        val request = GenerateContentRequest(
            systemInstruction = Content(parts = listOf(Part(text = "You are an app capability detector. Respond with ONLY the word 'YES' or 'NO'. No explanations, no preamble, no markdown, no punctuation. ONLY YES or NO."))),
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
