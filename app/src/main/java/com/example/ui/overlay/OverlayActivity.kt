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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.ExceptionsManager
import com.example.data.LogDatabase
import com.example.data.LogEntry
import com.example.data.SettingsManager
import com.example.engine.ShizukuUninstaller
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning

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
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "Warning",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Browser Removed", // Changed to removed because it is about to be removed
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(text = appName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = packageName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Reason: $method\n$reason",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Auto-removing in $timeLeft seconds...",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                performUninstall(context, packageName, method, reason)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Remove Now")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                exceptionsManager.addException(packageName)
                                logResult(context, packageName, appName, "Excepted", method, reason)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Keep & Add Exception")
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
