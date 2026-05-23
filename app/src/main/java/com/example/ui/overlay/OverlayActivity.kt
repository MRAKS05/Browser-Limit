package com.example.ui.overlay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ExceptionsManager
import com.example.data.LogDatabase
import com.example.data.LogEntry
import com.example.data.SettingsManager
import com.example.engine.ShizukuUninstaller
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OverlayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val packageName = intent.getStringExtra("PACKAGE_NAME") ?: return finish()
        val method = intent.getStringExtra("METHOD") ?: "Unknown"
        val reason = intent.getStringExtra("REASON") ?: ""
        
        val appName = try {
            val pm = packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) { packageName }
        
        setContent {
            MaterialTheme {
                OverlayScreen(
                    packageName = packageName,
                    appName = appName,
                    method = method,
                    reason = reason,
                    onDismiss = { finish() }
                )
            }
        }
    }
}

@Composable
fun OverlayScreen(
    packageName: String,
    appName: String,
    method: String,
    reason: String,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val exceptionsManager = remember { ExceptionsManager(context) }
    val timeLimit = settingsManager.countdownDuration.collectAsState().value
    var timeLeft by remember { mutableStateOf(timeLimit) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        // Auto remove when timer reaches 0
        performUninstall(context, packageName, method, reason)
        onDismiss()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Browser Detected",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(text = appName, style = MaterialTheme.typography.titleLarge)
                Text(text = packageName, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Reason: $method - $reason")
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Auto-removing in $timeLeft seconds...",
                    color = MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                exceptionsManager.addException(packageName)
                                logResult(context, packageName, appName, "Excepted", method, reason)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Keep & Add Exception")
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                performUninstall(context, packageName, method, reason)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Remove Now")
                    }
                }
            }
        }
    }
}

private suspend fun performUninstall(context: android.content.Context, packageName: String, method: String, reason: String) {
    val uninstaller = ShizukuUninstaller()
    val success = uninstaller.uninstallPackage(packageName)
    val appName = try {
        val pm = context.packageManager
        val info = pm.getApplicationInfo(packageName, 0)
        pm.getApplicationLabel(info).toString()
    } catch (e: Exception) { packageName }
    
    logResult(context, packageName, appName, if (success) "Removed" else "Error", method, reason + (if (success) "" else " (Uninstall Fail)"))
}

private suspend fun logResult(context: android.content.Context, packageName: String, appName: String, decision: String, method: String, reason: String) {
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
