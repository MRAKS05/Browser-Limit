package com.browserlimit.app.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.browserlimit.app.data.SettingsManager
import kotlinx.coroutines.delay

@Composable
fun ParentalUnlockDialog(
    settings: SettingsManager,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var parentalUnlockPin by remember { mutableStateOf("") }
    
    val waitingModeEnabled by settings.waitingModeEnabled.collectAsState()
    val parentalLockWaitTime by settings.parentalLockWaitTime.collectAsState()
    
    var waitTimeLeft by remember { mutableStateOf(if (waitingModeEnabled) parentalLockWaitTime.coerceAtLeast(10) else 0) }

    LaunchedEffect(waitingModeEnabled, parentalLockWaitTime) {
        while (waitTimeLeft > 0) {
            delay(1000)
            waitTimeLeft--
        }
    }

    val showPinInput = waitTimeLeft == 0

    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Security Unlock",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = if (showPinInput) "Security Unlock" else "Waiting to Unlock",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = { 
            if (showPinInput) {
                OutlinedTextField(
                    value = parentalUnlockPin, 
                    onValueChange = { parentalUnlockPin = it.filter { char -> char.isDigit() } },
                    label = { Text("PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp
                    )
                    Text(
                        text = "$waitTimeLeft seconds remaining",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "To prevent impulsive changes, please wait. Do not leave the app or the timer will reset.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        confirmButton = {
            if (showPinInput) {
                Button(onClick = { 
                    if (parentalUnlockPin == settings.parentalPinHash) {
                        onSuccess()
                    } else {
                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Confirm") }
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    )
}
