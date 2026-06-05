package com.browserlimit.app.ui.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import com.browserlimit.app.data.LogDatabase
import com.browserlimit.app.data.SettingsManager
import rikka.shizuku.Shizuku
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

import com.browserlimit.app.ui.components.ParentalUnlockDialog

@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val settings = remember { SettingsManager(context) }
    val isActive by settings.isActive.collectAsState()
    val useGemini by settings.useGemini.collectAsState()
    
    val shizukuRunning = remember { mutableStateOf(Shizuku.pingBinder()) }
    
    var missingNotifications by remember { mutableStateOf(false) }
    var missingOverlay by remember { mutableStateOf(false) }
    var missingShizuku by remember { mutableStateOf(false) }
    var missingBattery by remember { mutableStateOf(false) }

    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    
    fun executeAction(action: () -> Unit) {
        if (settings.isParentalLockEnabled) {
            pendingAction = action
        } else {
            action()
        }
    }
    
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    LaunchedEffect(lifecycleState) {
        if (lifecycleState == androidx.lifecycle.Lifecycle.State.RESUMED) {
            missingNotifications = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    
            missingOverlay = !Settings.canDrawOverlays(context)
            
            val pm = context.getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
            missingBattery = !pm.isIgnoringBatteryOptimizations(context.packageName)
            
            while (true) {
                if (!shizukuRunning.value) {
                    shizukuRunning.value = Shizuku.pingBinder()
                }
                missingShizuku = shizukuRunning.value && Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED
                kotlinx.coroutines.delay(2000)
            }
        }
    }

    DisposableEffect(Unit) {
        val listener = Shizuku.OnBinderReceivedListener {
            shizukuRunning.value = true
        }
        val deadListener = Shizuku.OnBinderDeadListener {
            shizukuRunning.value = false
        }
        Shizuku.addBinderReceivedListener(listener)
        Shizuku.addBinderDeadListener(deadListener)
        onDispose {
            Shizuku.removeBinderReceivedListener(listener)
            Shizuku.removeBinderDeadListener(deadListener)
        }
    }
    
    val db = LogDatabase.getDatabase(context).logDao()
    val todayStart = remember {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        cal.timeInMillis
    }
    
    val scannedCount by db.getScannedTodayCount(todayStart).collectAsState(initial = 0)
    val removedCount by db.getRemovedTodayCount(todayStart).collectAsState(initial = 0)
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Browser Limit Active", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            Switch(
                checked = isActive,
                onCheckedChange = { isChecked ->
                    executeAction {
                        settings.setActive(isChecked)
                        val intent = android.content.Intent(context, com.browserlimit.app.service.GuardService::class.java)
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            context.startForegroundService(intent)
                        } else {
                            context.startService(intent)
                        }
                    }
                }
            )
        }
        
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("System Status", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val color = if (shizukuRunning.value) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    Box(modifier = Modifier.size(12.dp).background(color = color, shape = MaterialTheme.shapes.small))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Shizuku: ${if (shizukuRunning.value) "Running" else "Not Running"}")
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    val todayStr = sdf.format(java.util.Date())
                    val hasLimit = settings.canUseGeminiApi(todayStr)
                    val geminiStatusColor = if (useGemini && hasLimit) Color(0xFF4CAF50) else if (!useGemini) Color.Gray else Color(0xFFFFC107)
                    Box(modifier = Modifier.size(12.dp).background(color = geminiStatusColor, shape = MaterialTheme.shapes.small))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gemini API: ${if (useGemini && hasLimit) "Ready" else if (!useGemini) "Disabled" else "Limit Reached"}")
                }
            }
        }
        
        if (missingNotifications || missingOverlay || missingShizuku || missingBattery || !shizukuRunning.value) {
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Action Required", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                    if (!shizukuRunning.value) {
                        Text("• Shizuku is Not Running", color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.titleSmall)
                        Text("This app heavily relies on Shizuku for auto-removing packages. Without it, core functionalities will not work.", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp, bottom = 4.dp))
                        Button(onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app/guide/setup/"))
                            context.startActivity(intent)
                        }) { Text("Install / Start Shizuku Guide") }
                    }
                    if (missingNotifications) {
                        Text("• Notifications Permission Missing", color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(top = 4.dp))
                        Button(onClick = {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        }, modifier = Modifier.padding(top = 4.dp)) { Text("Fix Notifications") }
                    }
                    if (missingOverlay) {
                        Text("• Overlay Permission Missing (Tap to fix)", color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(top = 4.dp))
                        Button(onClick = {
                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                            context.startActivity(intent)
                        }, modifier = Modifier.padding(top = 4.dp)) { Text("Fix Overlay") }
                    }
                    if (missingShizuku) {
                        Text("• Shizuku Permission Missing", color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(top = 4.dp))
                        Button(onClick = { Shizuku.requestPermission(0) }, modifier = Modifier.padding(top = 4.dp)) { Text("Fix Shizuku") }
                    }
                    if (missingBattery) {
                        Text("• Battery Optimization Active", color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(top = 4.dp))
                        Button(onClick = {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                            intent.data = Uri.parse("package:${context.packageName}")
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            }
                        }, modifier = Modifier.padding(top = 4.dp)) { Text("Disable Optimization") }
                    }
                }
            }
        }
        
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Scanned Today", style = MaterialTheme.typography.labelMedium)
                    Text("$scannedCount", style = MaterialTheme.typography.headlineMedium)
                }
            }
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Removed Today", style = MaterialTheme.typography.labelMedium)
                    Text("$removedCount", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
    
    pendingAction?.let { action ->
        ParentalUnlockDialog(
            settings = settings,
            onSuccess = {
                action()
                pendingAction = null
            },
            onCancel = {
                pendingAction = null
            }
        )
    }
}
