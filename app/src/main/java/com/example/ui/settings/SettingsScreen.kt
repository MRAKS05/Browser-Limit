package com.example.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.SettingsManager
import com.example.engine.BrowserDatabase
import com.example.engine.GeminiClient
import rikka.shizuku.Shizuku
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val settings = remember { SettingsManager(context) }
    
    val useGemini by settings.useGemini.collectAsState()
    val showOverlay by settings.showOverlay.collectAsState()
    val autoRemove by settings.autoRemove.collectAsState()
    val countdownDuration by settings.countdownDuration.collectAsState()
    val runOnStartup by settings.runOnStartup.collectAsState()
    val showAnimation by settings.showAnimation.collectAsState()
    
    val scope = rememberCoroutineScope()
    var showLocalListDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))
        
        SettingToggle("Use Gemini AI detection", useGemini) { settings.setUseGemini(it) }
        SettingToggle("Show confirmation overlay", showOverlay) { settings.setShowOverlay(it) }
        
        if (!settings.isParentalLockEnabled) {
            SettingToggle("Auto-remove (No countdown)", autoRemove) { settings.setAutoRemove(it) }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Countdown duration: $countdownDuration seconds")
        Slider(
            value = countdownDuration.toFloat(),
            onValueChange = { settings.setCountdownDuration(it.toInt()) },
            valueRange = 5f..30f,
            steps = 25
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        SettingToggle("Run on device startup", runOnStartup) { settings.setRunOnStartup(it) }
        SettingToggle("Show scanning animation", showAnimation) { settings.setShowAnimation(it) }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = {
            scope.launch {
                val client = GeminiClient()
                val result = client.isBrowser("com.android.chrome")
                Toast.makeText(context, "Test Chrome: $result", Toast.LENGTH_LONG).show()
            }
        }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            Text("Test Gemini Connection")
        }
        
        Button(onClick = {
            val result = if (Shizuku.pingBinder()) "Shizuku is running!" else "Shizuku is NOT running."
            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
        }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            Text("Test Shizuku Connection")
        }
        
        Button(onClick = { showLocalListDialog = true }, modifier = Modifier.fillMaxWidth()) {
            Text("View local browser list")
        }
    }
    
    if (showLocalListDialog) {
        AlertDialog(
            onDismissRequest = { showLocalListDialog = false },
            title = { Text("Local Browser List") },
            text = { Text(BrowserDatabase.KNOWN_BROWSERS.joinToString("\n")) },
            confirmButton = {
                TextButton(onClick = { showLocalListDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
fun SettingToggle(title: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, modifier = Modifier.weight(1f))
        Switch(checked = isChecked, onCheckedChange = onCheckedChange)
    }
}
