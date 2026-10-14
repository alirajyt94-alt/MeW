package com.example.data.model

data class JunkCategory(
    val id: String,
    val title: String,
    val description: String,
    val sizeBytes: Long,
    val isSelected: Boolean = true,
    val iconName: String = "clean"
)

data class JunkScanOverview(
    val totalJunkBytes: Long = 0L,
    val categories: List<JunkCategory> = emptyList(),
    val scannedAppsCount: Int = 0,
    val isScanning: Boolean = false
)
