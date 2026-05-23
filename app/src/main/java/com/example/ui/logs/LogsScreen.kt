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

@Composable
fun LogsScreen() {
    val context = LocalContext.current
    val db = remember { LogDatabase.getDatabase(context).logDao() }
    val logs by db.getAllLogs().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    
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
                    }
                }
            }
        }
    }
}
