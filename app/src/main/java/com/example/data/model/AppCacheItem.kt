package com.example.data.model

import android.graphics.drawable.Drawable

data class AppCacheItem(
    val packageName: String,
    val appName: String,
    val icon: Drawable? = null,
    val cacheSizeBytes: Long = 0L,
    val dataSizeBytes: Long = 0L,
    val apkSizeBytes: Long = 0L,
    val totalSizeBytes: Long = 0L,
    val isSystemApp: Boolean = false,
    val isWhiteListed: Boolean = false
)
