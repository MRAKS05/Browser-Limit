package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.SettingsManager
import com.example.receiver.AppInstallReceiver

class GuardService : Service() {
    private lateinit var installReceiver: AppInstallReceiver
    private lateinit var settingsManager: SettingsManager
    
    private val CHANNEL_ID = "GuardServiceChannel"

    override fun onCreate() {
        super.onCreate()
        settingsManager = SettingsManager(this)
        installReceiver = AppInstallReceiver()
        
        val filter = IntentFilter(Intent.ACTION_PACKAGE_ADDED).apply {
            addDataScheme("package")
        }
        registerReceiver(installReceiver, filter)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "PAUSE_RESUME") {
            val isActive = settingsManager.isActive.value
            settingsManager.setActive(!isActive)
        }
        
        startForeground(1, buildNotification())
        return START_STICKY
    }

    private fun buildNotification(): android.app.Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val actionIntent = Intent(this, GuardService::class.java).apply {
            action = "PAUSE_RESUME"
        }
        val actionPendingIntent = PendingIntent.getService(
            this, 1, actionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val title = if (settingsManager.isActive.value) "Browser Limit is active" else "Browser Limit is paused"
        val actionTitle = if (settingsManager.isActive.value) "Pause" else "Resume"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("Monitoring new app installations")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: use actual icon
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_pause, actionTitle, actionPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Browser Limit Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(installReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
