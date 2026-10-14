package com.example

import android.app.Application
import com.example.scheduler.DailyCleanupScheduler
import com.example.util.NotificationHelper
import com.example.util.PreferencesManager

class OptiCleanApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize notification channel
        NotificationHelper(this)

        // Ensure daily cleanup is scheduled if user has it enabled
        val preferences = PreferencesManager(this)
        if (preferences.isDailyCleanupEnabled) {
            DailyCleanupScheduler(this).scheduleDailyCleanup()
        }
    }
}
