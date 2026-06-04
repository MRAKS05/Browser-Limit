package com.browserlimit.app.engine

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.browserlimit.app.data.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BrowserDetector(private val context: Context) {
    private val geminiClient = GeminiClient(context)
    private val settingsManager = SettingsManager(context)

    suspend fun checkPackage(packageName: String, forceRecheck: Boolean = false): DetectionResult = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val appInfo: ApplicationInfo? = try {
            pm.getApplicationInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }

        // Never uninstall system apps
        if (appInfo != null && (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0) {
            val isChrome = packageName == "com.android.chrome"
            if (!(isChrome && settingsManager.removeSystemChrome.value)) {
                return@withContext DetectionResult(false, "System App", "Ignored: System App")
            }
        }

        val appName = if (appInfo != null) pm.getApplicationLabel(appInfo).toString() else packageName
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())

        // Check Local Check first (unless forcing a recheck)
        if (!forceRecheck) {
            if (settingsManager.geminiConfirmedNonBrowsers.contains(packageName)) {
                return@withContext DetectionResult(false, "Local Cache", "User overrides or Gemini previously confirmed as NO")
            }
            if (BrowserDatabase.KNOWN_BROWSERS.contains(packageName) || settingsManager.geminiConfirmedBrowsers.contains(packageName)) {
                return@withContext DetectionResult(true, "Local Cache", "Found in local known browsers list")
            }
        }

        // Primary Mode: Gemini API (if active and limit not reached, or if forced)
        if (settingsManager.useGemini.value && settingsManager.canUseGeminiApi(todayStr)) {
            val response = geminiClient.isBrowser(packageName)
            if (response.contains("YES") && !response.contains("NO")) {
                settingsManager.incrementGeminiApiCount()
                settingsManager.addConfirmedBrowser(packageName)
                return@withContext DetectionResult(true, "Gemini", "Gemini returned YES")
            } else if (response.contains("NO") && !response.contains("YES")) {
                settingsManager.incrementGeminiApiCount()
                settingsManager.addConfirmedNonBrowser(packageName)
                return@withContext DetectionResult(false, "Gemini", "Gemini returned NO")
            } else if (response.contains("YES") || response.startsWith("YES") || response.endsWith("YES.") || response.endsWith("YES")) {
                settingsManager.incrementGeminiApiCount()
                settingsManager.addConfirmedBrowser(packageName)
                return@withContext DetectionResult(true, "Gemini", "Gemini returned YES (parsed)")
            } else if (response.contains("NO") || response.startsWith("NO") || response.endsWith("NO.") || response.endsWith("NO")) {
                settingsManager.incrementGeminiApiCount()
                settingsManager.addConfirmedNonBrowser(packageName)
                return@withContext DetectionResult(false, "Gemini", "Gemini returned NO (parsed)")
            } else {
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
