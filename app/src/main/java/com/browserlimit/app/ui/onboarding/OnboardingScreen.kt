package com.browserlimit.app.ui.onboarding

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.browserlimit.app.data.SettingsManager
import rikka.shizuku.Shizuku

data class OnboardingStep(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var currentStep by remember { mutableStateOf(0) }
    
    val steps = listOf(
        OnboardingStep(
            "Welcome to Browser Limit",
            "Monitors installed apps and removes browsers automatically using Shizuku and Gemini AI.",
            Icons.Filled.CheckCircle
        ),
        OnboardingStep(
            "Notifications",
            "We need Notifications permission to run a background service that detects app installations securely.",
            Icons.Filled.Notifications
        ),
        OnboardingStep(
            "Display Over Other Apps",
            "This overlay permission allows us to show a confirmation dialog when a browser is detected.",
            Icons.Filled.Info
        ),
        OnboardingStep(
            "Shizuku Setup",
            "Shizuku enables us to uninstall apps automatically. Please ensure Shizuku is set up and running.",
            Icons.Filled.Build
        ),
        OnboardingStep(
            "Gemini API Key",
            "Enter your Gemini API key to enable AI-based browser detection, or skip this step to continue without Gemini.",
            Icons.Filled.Lock
        ),
        OnboardingStep(
            "Battery Optimization",
            "To ensure the background service doesn't get killed, please disable battery optimization for Browser Limit.",
            Icons.Filled.Warning
        )
    )
    
    val context = LocalContext.current
    val settings = remember { SettingsManager(context) }
    var geminiApiKeyInput by remember { mutableStateOf(settings.geminiApiKey.value) }
    
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { currentStep++ }
    )
    
    Scaffold(
        bottomBar = {
            Column(modifier = Modifier.padding(24.dp)) {
                // Progress Indicators
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(steps.size) { index ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (index == currentStep) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentStep) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }
                
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
                                if (geminiApiKeyInput.isBlank()) {
                                    Toast.makeText(context, "Enter Gemini API key or choose Do it later", Toast.LENGTH_LONG).show()
                                } else {
                                    settings.setGeminiApiKey(geminiApiKeyInput.trim())
                                    settings.setUseGemini(true)
                                    currentStep++
                                }
                            }
                            5 -> {
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
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        if (currentStep == steps.size - 1) "Finish Setup" else "Continue",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }) + fadeIn() with
                    slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth }) + fadeOut()
                }
            ) { targetStep ->
                val step = steps[targetStep]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(120.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = step.icon,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (targetStep == 4) {
                        OutlinedTextField(
                            value = geminiApiKeyInput,
                            onValueChange = { geminiApiKeyInput = it },
                            label = { Text("Gemini API Key") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Create an API key using the button below, then paste it here to enable Gemini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/app/apikey"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Text("Get Gemini API Key")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                settings.setUseGemini(false)
                                currentStep++
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Do it later")
                        }
                    }
                }
            }
        }
    }
}
