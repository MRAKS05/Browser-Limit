package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.SettingsManager
import com.example.service.GuardService
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.exceptions.ExceptionsScreen
import com.example.ui.logs.LogsScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import rikka.shizuku.Shizuku

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Start service automatically if it's active
        val settings = SettingsManager(this)
        if (settings.isActive.value) {
            val intent = Intent(this, GuardService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
        
        setContent {
            MyApplicationTheme {
                val isUnlocked = remember { mutableStateOf(!settings.isParentalLockEnabled) }
                
                if (isUnlocked.value) {
                    MainScreen()
                } else {
                    LockScreen(
                        settings = settings,
                        onUnlocked = { isUnlocked.value = true }
                    )
                }
            }
        }
    }
}

@Composable
fun LockScreen(settings: SettingsManager, onUnlocked: () -> Unit) {
    val waitTime = settings.parentalLockWaitTime.collectAsState().value
    val isWaitingMode = settings.waitingModeEnabled.collectAsState().value
    var timeLeft by remember { mutableStateOf(if (isWaitingMode) waitTime else 0) }
    var pinValue by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.Lock, contentDescription = "Lock", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("App Locked", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(32.dp))

            if (timeLeft > 0) {
                Text("Please wait $timeLeft seconds before unlocking.", color = MaterialTheme.colorScheme.error)
            } else {
                OutlinedTextField(
                    value = pinValue,
                    onValueChange = { 
                        pinValue = it
                        showError = false
                    },
                    label = { Text("Enter PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = showError
                )
                if (showError) {
                    Text("Incorrect PIN", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (pinValue == settings.parentalPinHash) {
                            onUnlocked()
                        } else {
                            showError = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Unlock")
                }
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Onboarding : Screen("onboarding", "Setup", Icons.Filled.Info)
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Info)
    object Exceptions : Screen("exceptions", "Exceptions", Icons.Filled.Warning)
    object Logs : Screen("logs", "Logs", Icons.AutoMirrored.Filled.List)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val settings = remember { SettingsManager(context) }
    val isSetupComplete = remember { mutableStateOf(settings.hasCompletedSetup) }

    val items = listOf(
        Screen.Dashboard,
        Screen.Exceptions,
        Screen.Logs,
        Screen.Settings
    )
    
    Scaffold(
        bottomBar = {
            if (isSetupComplete.value) {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController, 
            startDestination = if (isSetupComplete.value) Screen.Dashboard.route else Screen.Onboarding.route, 
            Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) { 
                com.example.ui.onboarding.OnboardingScreen(onComplete = {
                    isSetupComplete.value = true
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }) 
            }
            composable(Screen.Dashboard.route) { DashboardScreen() }
            composable(Screen.Exceptions.route) { ExceptionsScreen() }
            composable(Screen.Logs.route) { LogsScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
