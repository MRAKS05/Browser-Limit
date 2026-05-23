package com.example.ui.onboarding

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.SettingsManager
import rikka.shizuku.Shizuku

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var currentStep by remember { mutableStateOf(0) }
    
    val steps = listOf(
        "Welcome" to "BrowserGuard monitors installed apps and removes browsers automatically using Shizuku and Gemini AI.",
        "Notifications" to "We need Notifications permission to run a background service that detects app installations.",
        "Overlay" to "Overlay permission allows us to show a confirmation dialog when a browser is detected.",
        "Shizuku" to "Shizuku enables us to uninstall apps automatically. Please ensure Shizuku is set up and running.",
        "Battery Optimization" to "To ensure the background service doesn't get killed, please disable battery optimization for BrowserGuard."
    )
    
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val (title, desc) = steps[currentStep]
        
        Text(title, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(desc, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        val context = LocalContext.current
        val settings = remember { SettingsManager(context) }
        
        val notificationLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { currentStep++ }
        )
        
        Button(
            onClick = {
                when (currentStep) {
                    0 -> currentStep++
                    1 -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                currentStep++
                            }
                        } else {
                            currentStep++
                        }
                    }
                    2 -> {
                        if (!Settings.canDrawOverlays(context)) {
                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                            context.startActivity(intent)
                        }
                        currentStep++
                    }
                    3 -> {
                        try {
                            if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                                Shizuku.requestPermission(0)
                            }
                        } catch (e: Exception) {}
                        currentStep++
                    }
                    4 -> {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                        intent.data = Uri.parse("package:${context.packageName}")
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        }
                        settings.hasCompletedSetup = true
                        onComplete()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (currentStep == steps.size - 1) "Finish Setup" else "Grant & Continue")
        }
    }
}
