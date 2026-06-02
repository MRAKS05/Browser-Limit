package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<LogEntry>>

    @Query("SELECT COUNT(*) FROM logs")
    suspend fun getLogCount(): Int
    
    @Query("SELECT COUNT(*) FROM logs WHERE decision = 'Removed' AND timestamp >= :todayStart")
    fun getRemovedTodayCount(todayStart: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM logs WHERE timestamp >= :todayStart")
    fun getScannedTodayCount(todayStart: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: LogEntry)

    @Query("DELETE FROM logs WHERE id NOT IN (SELECT id FROM logs ORDER BY timestamp DESC LIMIT 500)")
    suspend fun trimLogs()
    
    @Query("DELETE FROM logs")
    suspend fun clearAll()
}
