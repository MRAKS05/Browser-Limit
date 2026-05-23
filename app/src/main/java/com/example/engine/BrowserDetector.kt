package com.example.engine

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.data.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BrowserDetector(private val context: Context) {
    private val geminiClient = GeminiClient()
    private val settingsManager = SettingsManager(context)

    suspend fun checkPackage(packageName: String): DetectionResult = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val appInfo: ApplicationInfo? = try {
            pm.getApplicationInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }

        // Never uninstall system apps
        if (appInfo != null && (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0) {
            return@withContext DetectionResult(false, "System App", "Ignored: System App")
        }

        val appName = if (appInfo != null) pm.getApplicationLabel(appInfo).toString() else packageName
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())

        // Primary Mode: Gemini API (if active and limit not reached)
        if (settingsManager.useGemini.value && settingsManager.canUseGeminiApi(todayStr)) {
            settingsManager.incrementGeminiApiCount()
            val response = geminiClient.isBrowser(packageName)
            if (response == "YES") {
                return@withContext DetectionResult(true, "Gemini", "Gemini returned YES")
            } else if (response == "NO") {
                return@withContext DetectionResult(false, "Gemini", "Gemini returned NO")
            } else {
                // Fallback Mode if unexpected response
                return@withContext checkLocalList(packageName, "Fallback (Error: $response)")
            }
        } else {
            // Fallback Mode: Local List
            return@withContext checkLocalList(packageName, "Local")
        }
    }

    private fun checkLocalList(packageName: String, method: String): DetectionResult {
        if (BrowserDatabase.KNOWN_BROWSERS.contains(packageName)) {
            return DetectionResult(true, method, "Found in local KNOWN_BROWSERS list")
        }
        return DetectionResult(false, method, "Not found in local list")
    }
}

data class DetectionResult(
    val isBrowser: Boolean,
    val method: String,
    val reason: String
)
