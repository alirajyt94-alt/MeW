package com.example.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.receiver.CleanupAlarmReceiver
import com.example.util.PreferencesManager
import java.util.Calendar

class DailyCleanupScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val preferences = PreferencesManager(context)

    fun scheduleDailyCleanup() {
        if (!preferences.isDailyCleanupEnabled) {
            cancelDailyCleanup()
            return
        }

        val hour = preferences.dailyCleanupHour
        val minute = preferences.dailyCleanupMinute

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If time is already past today, set for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val pendingIntent = getPendingIntent()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // Inexact fallback
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelDailyCleanup() {
        alarmManager.cancel(getPendingIntent())
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(context, CleanupAlarmReceiver::class.java).apply {
            action = CleanupAlarmReceiver.ACTION_DAILY_CLEANUP
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, REQUEST_CODE_DAILY, intent, flags)
    }

    fun getNextRunTimeMillis(): Long {
        if (!preferences.isDailyCleanupEnabled) return 0L
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, preferences.dailyCleanupHour)
            set(Calendar.MINUTE, preferences.dailyCleanupMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return calendar.timeInMillis
    }

    companion object {
        const val REQUEST_CODE_DAILY = 4201
    }
}
