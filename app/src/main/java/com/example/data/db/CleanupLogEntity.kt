package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cleanup_logs")
data class CleanupLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val freedRamBytes: Long,
    val clearedJunkBytes: Long,
    val processesKilledCount: Int,
    val cleanupType: String, // "MANUAL_BOOST", "DAILY_SCHEDULED", "QUICK_CLEAN"
    val notes: String = ""
)
