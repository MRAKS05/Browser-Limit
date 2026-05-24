package com.example.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
    val geminiApiKey by settings.geminiApiKey.collectAsState()
    val removeSystemChrome by settings.removeSystemChrome.collectAsState()
    val waitingModeEnabled by settings.waitingModeEnabled.collectAsState()
    val parentalLockWaitTime by settings.parentalLockWaitTime.collectAsState()
    
    val scope = rememberCoroutineScope()
    var showLocalListDialog by remember { mutableStateOf(false) }
    var showParentalLockDialog by remember { mutableStateOf(false) }
    var showParentalUnlockDialog by remember { mutableStateOf(false) }
    var parentalLockPin by remember { mutableStateOf("") }
    var parentalUnlockPin by remember { mutableStateOf("") }
    var isParentalLockEnabled by remember { mutableStateOf(settings.isParentalLockEnabled) }

    var securityExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))
        
        OutlinedTextField(
            value = geminiApiKey,
            onValueChange = { settings.setGeminiApiKey(it) },
            label = { Text("Gemini API Key") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        
        SettingToggle("Use Gemini AI detection", useGemini) { settings.setUseGemini(it) }
        
        SettingToggle("Remove Chrome (even if system app)", removeSystemChrome) { settings.setRemoveSystemChrome(it) }
        
        // Security Settings Section
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Security Settings", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { securityExpanded = !securityExpanded }) {
                        Icon(Icons.Filled.ArrowDropDown, "Toggle", modifier = Modifier.rotate(if (securityExpanded) 180f else 0f))
                    }
                }
                
                if (securityExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingToggle("Enable Parental Lock", isParentalLockEnabled) { enabled ->
                        if (enabled) {
                            showParentalLockDialog = true
                        } else {
                            showParentalUnlockDialog = true
                        }
                    }
                    if (isParentalLockEnabled) {
                        Text("Settings are restricted. Overlay disabled, Auto-remove enabled.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    
                    SettingToggle("Enable Waiting Mode", waitingModeEnabled) { settings.setWaitingModeEnabled(it) }
                    if (waitingModeEnabled) {
                        Text("Wait Time: $parentalLockWaitTime seconds")
                        Slider(
                            value = parentalLockWaitTime.toFloat(),
                            onValueChange = { settings.setParentalLockWaitTime(it.toInt()) },
                            valueRange = 0f..300f,
                            steps = 29
                        )
                    }
                }
            }
        }
        
        if (!isParentalLockEnabled) {
            SettingToggle("Show confirmation overlay", showOverlay) { 
                settings.setShowOverlay(it)
                if (it) settings.setAutoRemove(false)
            }
            SettingToggle("Auto-remove (No countdown)", autoRemove) { 
                settings.setAutoRemove(it)
                if (it) settings.setShowOverlay(false)
            }
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
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = {
            scope.launch {
                val client = GeminiClient(context)
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
    
    if (showParentalLockDialog) {
        AlertDialog(
            onDismissRequest = { showParentalLockDialog = false },
            title = { Text("Set Parental Lock PIN") },
            text = { 
                OutlinedTextField(
                    value = parentalLockPin, 
                    onValueChange = { parentalLockPin = it },
                    label = { Text("PIN") },
                    visualTransformation = PasswordVisualTransformation()
                )
            },
            confirmButton = {
                TextButton(onClick = { 
                    if (parentalLockPin.isNotBlank()) {
                        settings.parentalPinHash = parentalLockPin // Note: We should hash this in a real app
                        settings.isParentalLockEnabled = true
                        settings.setShowOverlay(false)
                        settings.setAutoRemove(true)
                        isParentalLockEnabled = true
                        showParentalLockDialog = false
                    }
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showParentalLockDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showParentalUnlockDialog) {
        var waitTimeLeft by remember { mutableStateOf(if (waitingModeEnabled) parentalLockWaitTime else 0) }

        val isWindowFocused = androidx.compose.ui.platform.LocalWindowInfo.current.isWindowFocused

        LaunchedEffect(isWindowFocused) {
            if (!isWindowFocused) {
                showParentalUnlockDialog = false
                parentalUnlockPin = ""
            }
        }

        LaunchedEffect(waitingModeEnabled, parentalLockWaitTime) {
            while (waitTimeLeft > 0) {
                kotlinx.coroutines.delay(1000)
                waitTimeLeft--
            }
        }

        val showPinInput = waitTimeLeft == 0

        AlertDialog(
            onDismissRequest = { 
                showParentalUnlockDialog = false 
                parentalUnlockPin = ""
            },
            title = { Text(if (showPinInput) "Enter PIN to Disable" else "Waiting to Unlock...") },
            text = { 
                if (showPinInput) {
                    OutlinedTextField(
                        value = parentalUnlockPin, 
                        onValueChange = { parentalUnlockPin = it },
                        label = { Text("PIN") },
                        visualTransformation = PasswordVisualTransformation()
                    )
                } else {
                    Text("Please keep this screen open for $waitTimeLeft seconds. Do not leave the app or the timer will reset.", modifier = Modifier.padding(16.dp))
                }
            },
            confirmButton = {
                if (showPinInput) {
                    TextButton(onClick = { 
                        if (parentalUnlockPin == settings.parentalPinHash) {
                            settings.isParentalLockEnabled = false
                            isParentalLockEnabled = false
                            showParentalUnlockDialog = false
                            parentalUnlockPin = ""
                        } else {
                            Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                        }
                    }) { Text("Confirm") }
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showParentalUnlockDialog = false 
                    parentalUnlockPin = ""
                }) { Text("Cancel") }
            }
        )
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
