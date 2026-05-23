package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logs")
data class LogEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val appName: String,
    val packageName: String,
    val detectionMethod: String,
    val decision: String,
    val geminiResponse: String = "",
    val shizukuResult: String = ""
)
