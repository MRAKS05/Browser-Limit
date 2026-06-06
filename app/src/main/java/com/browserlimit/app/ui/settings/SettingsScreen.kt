package com.browserlimit.app.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.browserlimit.app.data.SettingsManager
import com.browserlimit.app.engine.BrowserDatabase
import com.browserlimit.app.engine.GeminiClient
import rikka.shizuku.Shizuku
import kotlinx.coroutines.launch
import com.browserlimit.app.BuildConfig
import com.browserlimit.app.ui.components.ParentalUnlockDialog

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

    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    
    fun executeAction(action: () -> Unit) {
        if (settings.isParentalLockEnabled) {
            pendingAction = action
        } else {
            action()
        }
    }

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
        Text(
            text = "Need an API key? Follow these steps:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        Text(
            text = "1. Click the button below to get your free key.\n2. Sign in with Google.\n3. Create a project and generate the key.\n4. Paste the key above to enable AI detection.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Button(
            onClick = {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://aistudio.google.com/app/apikey"))
                context.startActivity(intent)
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Filled.Info, contentDescription = "Get Key", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Get Gemini API Key")
        }
        
        SettingToggle("Use Gemini AI detection", useGemini) { isChecked -> executeAction { settings.setUseGemini(isChecked) } }
        
        SettingToggle("Remove Chrome (even if system app)", removeSystemChrome) { isChecked -> executeAction { settings.setRemoveSystemChrome(isChecked) } }
        
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
                            executeAction {
                                settings.isParentalLockEnabled = false
                                isParentalLockEnabled = false
                            }
                        }
                    }
                    if (isParentalLockEnabled) {
                        Text("Settings are restricted. Require PIN to change.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        SettingToggle("Enable Waiting Mode", waitingModeEnabled) { isChecked -> executeAction { settings.setWaitingModeEnabled(isChecked) } }
                        if (waitingModeEnabled) {
                            Text("Wait Time: ${parentalLockWaitTime.coerceAtLeast(10)} seconds")
                            Slider(
                                value = parentalLockWaitTime.coerceIn(10, 300).toFloat(),
                                onValueChange = { settings.setParentalLockWaitTime(it.toInt()) },
                                valueRange = 10f..300f,
                                steps = 28
                            )
                        }
                    }
                }
            }
        }
        
        if (!isParentalLockEnabled) {
            SettingToggle("Show confirmation overlay", showOverlay) { isChecked ->
                executeAction { settings.setShowOverlay(isChecked) }
            }
            SettingToggle("Auto-remove (No countdown)", autoRemove) { isChecked ->
                executeAction { settings.setAutoRemove(isChecked) }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Countdown duration: $countdownDuration seconds")
            Slider(
                value = countdownDuration.toFloat(),
                onValueChange = { settings.setCountdownDuration(it.toInt()) },
                valueRange = 5f..30f,
                steps = 25
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        SettingToggle("Run on device startup", runOnStartup) { isChecked -> executeAction { settings.setRunOnStartup(isChecked) } }
        
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

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Version: ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
    
    if (showParentalLockDialog) {
        AlertDialog(
            onDismissRequest = { showParentalLockDialog = false },
            title = { Text("Set Parental Lock PIN") },
            text = { 
                OutlinedTextField(
                    value = parentalLockPin, 
                    onValueChange = { parentalLockPin = it.filter { char -> char.isDigit() } },
                    label = { Text("PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = { 
                    if (parentalLockPin.isNotBlank()) {
                        settings.setParentalPin(parentalLockPin)
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

    if (pendingAction != null) {
        ParentalUnlockDialog(
            settings = settings,
            onSuccess = {
                pendingAction?.invoke()
                pendingAction = null
            },
            onCancel = {
                pendingAction = null
            }
        )
    }
    
    if (showLocalListDialog) {
        AlertDialog(
            onDismissRequest = { showLocalListDialog = false },
            title = { Text("Local Browser List") },
            text = { 
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(BrowserDatabase.KNOWN_BROWSERS.joinToString("\n"))
                }
            },
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
