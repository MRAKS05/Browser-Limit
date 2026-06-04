package com.browserlimit.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.browserlimit.app.data.SettingsManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            
            val settings = SettingsManager(context)
            if (settings.runOnStartup.value) {
                // Determine foreground service type automatically via ContextCompat in MainActivity if we want
                // For Android 8+:
                val serviceIntent = Intent(context, com.browserlimit.app.service.GuardService::class.java)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}
