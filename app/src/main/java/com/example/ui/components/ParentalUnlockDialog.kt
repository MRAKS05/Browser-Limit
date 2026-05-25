package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.SettingsManager
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
        title = { Text(if (showPinInput) "Enter PIN to Continue" else "Waiting...") },
        text = { 
            if (showPinInput) {
                OutlinedTextField(
                    value = parentalUnlockPin, 
                    onValueChange = { parentalUnlockPin = it.filter { char -> char.isDigit() } },
                    label = { Text("PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true
                )
            } else {
                Text("Please keep this screen open for $waitTimeLeft seconds. Do not leave the app or the timer will reset.", modifier = Modifier.padding(16.dp))
            }
        },
        confirmButton = {
            if (showPinInput) {
                TextButton(onClick = { 
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
