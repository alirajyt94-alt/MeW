package com.example.util

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("opticlean_preferences", Context.MODE_PRIVATE)

    var isDailyCleanupEnabled: Boolean
        get() = prefs.getBoolean(KEY_DAILY_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_DAILY_ENABLED, value).apply()

    var dailyCleanupHour: Int
        get() = prefs.getInt(KEY_DAILY_HOUR, 3) // Default 3 AM
        set(value) = prefs.edit().putInt(KEY_DAILY_HOUR, value).apply()

    var dailyCleanupMinute: Int
        get() = prefs.getInt(KEY_DAILY_MINUTE, 0)
        set(value) = prefs.edit().putInt(KEY_DAILY_MINUTE, value).apply()

    var isNotifyOnCleanup: Boolean
        get() = prefs.getBoolean(KEY_NOTIFY_CLEANUP, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFY_CLEANUP, value).apply()

    var isAggressiveBoostEnabled: Boolean
        get() = prefs.getBoolean(KEY_AGGRESSIVE_BOOST, false)
        set(value) = prefs.edit().putBoolean(KEY_AGGRESSIVE_BOOST, value).apply()

    var lastCleanupTimestamp: Long
        get() = prefs.getLong(KEY_LAST_CLEANUP_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_CLEANUP_TIME, value).apply()

    var whitelistedPackages: Set<String>
        get() = prefs.getStringSet(KEY_WHITELIST, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_WHITELIST, value).apply()

    fun toggleWhitelist(packageName: String) {
        val current = whitelistedPackages.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        whitelistedPackages = current
    }

    companion object {
        private const val KEY_DAILY_ENABLED = "key_daily_enabled"
        private const val KEY_DAILY_HOUR = "key_daily_hour"
        private const val KEY_DAILY_MINUTE = "key_daily_minute"
        private const val KEY_NOTIFY_CLEANUP = "key_notify_cleanup"
        private const val KEY_AGGRESSIVE_BOOST = "key_aggressive_boost"
        private const val KEY_LAST_CLEANUP_TIME = "key_last_cleanup_time"
        private const val KEY_WHITELIST = "key_whitelist"
    }
}
