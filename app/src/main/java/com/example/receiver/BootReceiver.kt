package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.scheduler.DailyCleanupScheduler
import com.example.util.PreferencesManager

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val preferences = PreferencesManager(context)
            if (preferences.isDailyCleanupEnabled) {
                DailyCleanupScheduler(context).scheduleDailyCleanup()
            }
        }
    }
}
