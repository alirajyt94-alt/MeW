package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CleanupLogDao {

    @Query("SELECT * FROM cleanup_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<CleanupLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CleanupLogEntity): Long

    @Query("SELECT SUM(clearedJunkBytes) FROM cleanup_logs")
    fun getTotalJunkCleared(): Flow<Long?>

    @Query("SELECT SUM(freedRamBytes) FROM cleanup_logs")
    fun getTotalRamFreed(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM cleanup_logs")
    fun getTotalCleanupsCount(): Flow<Int>

    @Query("DELETE FROM cleanup_logs")
    suspend fun clearAllLogs()
}
