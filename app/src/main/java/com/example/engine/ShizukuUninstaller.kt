package com.example.engine

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

class ShizukuUninstaller {
    suspend fun uninstallPackage(packageName: String): Boolean = withContext(Dispatchers.IO) {
        if (!Shizuku.pingBinder()) {
            Log.e("ShizukuUninstaller", "Shizuku is not running")
            return@withContext false
        }
        return@withContext ShizukuHelper.uninstall(packageName)
    }
}
