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
        val apiKey = settings.geminiApiKey.value.takeIf { it.isNotBlank() }
        if (apiKey == null) {
            return@withContext "ERROR: Gemini API key is not configured"
        }
        
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
            You are a strict App Capability Evaluator. Your job is to determine if the Android app with package name '$packageName' is a DEDICATED WEB BROWSER or an app designed to allow UNRESTRICTED open internet browsing.$playStoreInfo
            
            Do NOT flag an app as YES just because it contains a WebView, opens help pages, or has a basic in-app browser for clicking links (e.g., standard social media apps like Facebook/Telegram, messaging apps, or utility apps).
            
            Answer YES ONLY if:
            1. The app is a dedicated web browser (like Chrome, Firefox, Brave).
            2. The app's primary purpose is proxying or unblocking websites.
            3. The app is a video downloader or utility that explicitly features an unrestricted built-in browser with address bar for general web surfing.
            
            Answer NO for anything else, including regular apps, games, standard communication tools, or apps that only open specific links or safe in-app content.
            
            Reply with EXACTLY one word: YES or NO.
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
                val status = e.code()
                val message = e.message() ?: "HTTP error"
                if (status == 429) {
                    attempt++
                    if (attempt < maxAttempts) {
                        kotlinx.coroutines.delay(1000L * attempt)
                        continue
                    }
                    return@withContext "ERROR: HTTP 429 Too Many Requests"
                }
                val detail = when (status) {
                    401 -> "Unauthorized - check your Gemini API key"
                    403 -> "Forbidden - invalid API key or access denied"
                    404 -> "Not Found - invalid endpoint"
                    else -> message
                }
                return@withContext "ERROR: HTTP $status $detail"
            } catch (e: java.net.SocketTimeoutException) {
                return@withContext "ERROR: Gemini not responding (timeout)"
            } catch (e: java.net.ConnectException) {
                return@withContext "ERROR: Connection refused"
            } catch (e: java.net.UnknownHostException) {
                return@withContext "ERROR: Network failure - unable to resolve Gemini host"
            } catch (e: Exception) {
                return@withContext "ERROR: ${e.message ?: "Unexpected error"}"
            }
        }
        return@withContext "ERROR: HTTP 429 Too Many Requests"
    }
}
