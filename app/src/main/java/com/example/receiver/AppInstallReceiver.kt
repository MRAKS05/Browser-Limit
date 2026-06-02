package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.ExceptionsManager
import com.example.data.LogDatabase
import com.example.data.LogEntry
import com.example.data.SettingsManager
import com.example.engine.BrowserDetector
import com.example.engine.ShizukuUninstaller
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppInstallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_PACKAGE_ADDED) {
            val packageName = intent.data?.schemeSpecificPart ?: return
            
            val settingsManager = SettingsManager(context)
            if (!settingsManager.isActive.value) return
            
            CoroutineScope(Dispatchers.IO).launch {
                val exceptionsManager = ExceptionsManager(context)
                if (exceptionsManager.isExcepted(packageName)) {
                    logResult(context, packageName, "Excepted", "Settings", "Found in exceptions")
                    return@launch
                }
                
                val detector = BrowserDetector(context)
                val result = detector.checkPackage(packageName)
                
                if (result.isBrowser) {
                    if (settingsManager.showOverlay.value) {
                        launchOverlayActivity(context, packageName, result.method, result.reason)
                    } else {
                        // Auto uninstall
                        val uninstaller = ShizukuUninstaller()
                        val success = uninstaller.uninstallPackage(packageName)
                        if (success) {
                            val appName = try {
                                val pm = context.packageManager
                                val info = pm.getApplicationInfo(packageName, 0)
                                pm.getApplicationLabel(info).toString()
                            } catch (e: Exception) { packageName }
                            com.example.engine.NotificationHelper.sendUninstallNotification(context, appName, packageName, result.reason)
                        }
                        logResult(context, packageName, if (success) "Removed" else "Error", result.method, result.reason + (if (success) "" else " (Uninstall Fail)"))
                    }
                } else {
                    logResult(context, packageName, "Kept", result.method, result.reason)
                }
            }
        }
    }
    
    private fun launchOverlayActivity(context: Context, packageName: String, method: String, reason: String) {
        val intent = Intent(context, com.example.ui.overlay.OverlayActivity::class.java).apply {
            putExtra("PACKAGE_NAME", packageName)
            putExtra("METHOD", method)
            putExtra("REASON", reason)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(intent)
    }
    
    private suspend fun logResult(context: Context, packageName: String, decision: String, method: String, reason: String) {
        val appName = try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) { packageName }
        
        val db = LogDatabase.getDatabase(context).logDao()
        db.insertLog(
            LogEntry(
                timestamp = System.currentTimeMillis(),
                appName = appName,
                packageName = packageName,
                detectionMethod = method,
                decision = decision,
                geminiResponse = reason
            )
        )
        db.trimLogs()
    }
}
