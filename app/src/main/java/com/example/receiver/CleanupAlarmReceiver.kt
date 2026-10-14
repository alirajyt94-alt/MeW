package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.db.AppDatabase
import com.example.data.db.CleanupLogEntity
import com.example.scheduler.DailyCleanupScheduler
import com.example.util.CacheManager
import com.example.util.NotificationHelper
import com.example.util.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CleanupAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val preferences = PreferencesManager(context)
                val cacheManager = CacheManager(context)
                val notificationHelper = NotificationHelper(context)
                val database = AppDatabase.getDatabase(context)

                // Perform automated scheduled optimization
                val result = cacheManager.performOptimization(preferences.whitelistedPackages)

                // Record in history
                database.cleanupLogDao().insertLog(
                    CleanupLogEntity(
                        timestamp = System.currentTimeMillis(),
                        freedRamBytes = result.freedRamBytes,
                        clearedJunkBytes = result.clearedJunkBytes,
                        processesKilledCount = result.processesKilledCount,
                        cleanupType = "DAILY_SCHEDULED",
                        notes = "Automated daily scheduled optimization"
                    )
                )

                preferences.lastCleanupTimestamp = System.currentTimeMillis()

                // Notify user if enabled
                if (preferences.isNotifyOnCleanup) {
                    notificationHelper.sendCleanupNotification(
                        freedRamBytes = result.freedRamBytes,
                        clearedJunkBytes = result.clearedJunkBytes
                    )
                }

                // Reschedule for next day
                DailyCleanupScheduler(context).scheduleDailyCleanup()
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_DAILY_CLEANUP = "com.example.opticlean.ACTION_DAILY_CLEANUP"
    }
}
