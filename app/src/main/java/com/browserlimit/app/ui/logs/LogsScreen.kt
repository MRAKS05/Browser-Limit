package com.browserlimit.app.ui.logs

import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.browserlimit.app.data.LogDatabase
import kotlinx.coroutines.launch
import java.io.File

import androidx.compose.foundation.clickable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.browserlimit.app.data.LogEntry
import com.browserlimit.app.engine.BrowserDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LogsScreen() {
    val context = LocalContext.current
    val db = remember { LogDatabase.getDatabase(context).logDao() }
    val logs by db.getAllLogs().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val browserDetector = remember { BrowserDetector(context) }
    
    var recheckingPackage by remember { mutableStateOf<String?>(null) }
    var selectedLog by remember { mutableStateOf<LogEntry?>(null) }
    
    if (selectedLog != null) {
        LogDetailsDialog(log = selectedLog!!, onDismiss = { selectedLog = null })
    }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Activity Log", style = MaterialTheme.typography.headlineSmall)
            Row {
                TextButton(onClick = { 
                    scope.launch { 
                        db.clearAll() 
                    }
                }) {
                    Text("Clear")
                }
                TextButton(onClick = {
                    try {
                        val logsText = logs.joinToString("\n") { "${it.timestamp} | ${it.appName} (${it.packageName}) | ${it.decision} | ${it.detectionMethod} - ${it.geminiResponse}" }
                        try {
                            val exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            val file = File(exportDir, "browserlimit_logs_${System.currentTimeMillis()}.txt")
                            file.writeText(logsText)
                            Toast.makeText(context, "Exported to Downloads folder", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            val backupDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            val file2 = File(backupDir, "browserlimit_logs_${System.currentTimeMillis()}.txt")
                            file2.writeText(logsText)
                            Toast.makeText(context, "Exported to App Data / Downloads", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Export failed.", Toast.LENGTH_LONG).show()
                    }
                }) {
                    Text("Export")
                }
            }
        }
        
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(logs) { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { selectedLog = log }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(log.appName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            val decisionColor = when (log.decision) {
                                "Removed" -> MaterialTheme.colorScheme.error
                                "Kept" -> Color(0xFF4CAF50)
                                "Excepted" -> Color(0xFFFFC107)
                                else -> Color.Gray
                            }
                            Text(log.decision, style = MaterialTheme.typography.labelLarge, color = decisionColor)
                        }
                        Text(log.packageName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Method: ${log.detectionMethod}")
                        if (log.geminiResponse.isNotEmpty()) {
                            Text("Detail: ${log.geminiResponse}", style = MaterialTheme.typography.bodySmall)
                        }
                        
                        OutlinedButton(
                            onClick = {
                                if (recheckingPackage == null) {
                                    recheckingPackage = log.packageName
                                    scope.launch {
                                        val settings = com.browserlimit.app.data.SettingsManager(context)
                                        settings.removeConfirmedCache(log.packageName)
                                        val result = browserDetector.checkPackage(log.packageName, forceRecheck = true)
                                        
                                        db.insertLog(
                                            LogEntry(
                                                timestamp = System.currentTimeMillis(),
                                                appName = log.appName,
                                                packageName = log.packageName,
                                                detectionMethod = result.method + " (Recheck)",
                                                decision = if (result.isBrowser) "Removed" else "Kept",
                                                geminiResponse = result.reason
                                            )
                                        )
                                        db.trimLogs()
                                        
                                        if (result.isBrowser) {
                                            val uninstaller = com.browserlimit.app.engine.ShizukuUninstaller()
                                            uninstaller.uninstallPackage(log.packageName)
                                        }

                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Recheck result: ${if (result.isBrowser) "Browser (Removed)" else "Not Browser (Allowed)"} (${result.method})", Toast.LENGTH_LONG).show()
                                            recheckingPackage = null
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                            enabled = recheckingPackage != log.packageName
                        ) {
                            if (recheckingPackage == log.packageName) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Rechecking...")
                            } else {
                                Text("Recheck with Gemini")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogDetailsDialog(log: LogEntry, onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = log.appName, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    val sdf = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
                    val timeStr = sdf.format(Date(log.timestamp))
                    Text("Time: $timeStr", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Package: ${log.packageName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Decision: ${log.decision}", style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text("Method: ${log.detectionMethod}", style = MaterialTheme.typography.bodyMedium)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Reason:", style = MaterialTheme.typography.titleSmall)
                    Text(log.geminiResponse.ifEmpty { "N/A" }, style = MaterialTheme.typography.bodySmall)
                }
                
                if (log.detectionMethod.startsWith("Gemini")) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Gemini API Details:", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text("Prompt Used:", style = MaterialTheme.typography.titleSmall)
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()) {
                            Text("You are a strict App Capability Evaluator. Your job is to determine if the Android app with package name '${log.packageName}' is a DEDICATED WEB BROWSER or an app designed to allow UNRESTRICTED open internet browsing...", 
                                style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp))
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Output Got:", style = MaterialTheme.typography.titleSmall)
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()) {
                            Text(log.geminiResponse, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp))
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Uninstallation Method / Execution:", style = MaterialTheme.typography.titleSmall)
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()) {
                            val executionText = if (log.decision == "Removed") {
                                "Method: Shizuku (ADB Wrapper)\nCommand: pm uninstall -k --user 0 ${log.packageName}\nOutput: Success"
                            } else if (log.decision == "Error") {
                                "Method: Shizuku (ADB Wrapper)\nCommand: pm uninstall -k --user 0 ${log.packageName}\nOutput: Failed (Permission or System Error)"
                            } else {
                                "Action: Skipped (App kept based on response or exception list limits)"
                            }
                            Text(
                                executionText,
                                style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                } else if (log.detectionMethod.contains("Local Cache") || log.detectionMethod.contains("Fallback") || log.detectionMethod.contains("Settings")) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Local Evaluation Only.", style = MaterialTheme.typography.bodyMedium, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Uninstallation Method / Execution:", style = MaterialTheme.typography.titleSmall)
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()) {
                            val executionText = if (log.decision == "Removed") {
                                "Method: Shizuku (ADB Wrapper)\nCommand: pm uninstall -k --user 0 ${log.packageName}\nOutput: Success"
                            } else if (log.decision == "Error") {
                                "Method: Shizuku (ADB Wrapper)\nCommand: pm uninstall -k --user 0 ${log.packageName}\nOutput: Failed (Permission or System Error)"
                            } else {
                                "Action: Skipped (App kept based on local evaluation)"
                            }
                            Text(
                                executionText,
                                style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        dismissButton = {
            TextButton(onClick = {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val timeStr = sdf.format(Date(log.timestamp))
                val exportText = """
                    |App Name: ${log.appName}
                    |Package Name: ${log.packageName}
                    |Time: $timeStr
                    |Decision: ${log.decision}
                    |Method: ${log.detectionMethod}
                    |Reason: ${log.geminiResponse}
                """.trimMargin()
                try {
                    val exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(exportDir, "browserlimit_log_${log.packageName}_${log.timestamp}.txt")
                    file.writeText(exportText)
                    Toast.makeText(context, "Exported individual log to Downloads", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    try {
                        val backupDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                        val file2 = File(backupDir, "browserlimit_log_${log.packageName}_${log.timestamp}.txt")
                        file2.writeText(exportText)
                        Toast.makeText(context, "Exported individual log to App Data / Downloads", Toast.LENGTH_LONG).show()
                    } catch (e2: Exception) {
                        Toast.makeText(context, "Export failed.", Toast.LENGTH_SHORT).show()
                    }
                }
            }) { Text("Export Log") }
        }
    )
}
