package com.example.ui.logs

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
import com.example.data.LogDatabase
import kotlinx.coroutines.launch
import java.io.File

import com.example.engine.BrowserDetector
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
                    val exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(exportDir, "browserlimit_logs.txt")
                    file.writeText(logs.joinToString("\n") { "${it.timestamp} | ${it.appName} (${it.packageName}) | ${it.decision} | ${it.detectionMethod} - ${it.geminiResponse}" })
                    Toast.makeText(context, "Exported to Downloads", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Export")
                }
            }
        }
        
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(logs) { log ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(log.appName, style = MaterialTheme.typography.titleMedium)
                            
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
                                        val settings = com.example.data.SettingsManager(context)
                                        settings.removeConfirmedCache(log.packageName)
                                        val result = browserDetector.checkPackage(log.packageName, forceRecheck = true)
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Recheck result: ${if (result.isBrowser) "Browser" else "Not Browser"} (${result.method})", Toast.LENGTH_LONG).show()
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
