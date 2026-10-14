package com.example.data.model

data class MemoryStats(
    val totalRamBytes: Long = 0L,
    val availableRamBytes: Long = 0L,
    val usedRamBytes: Long = 0L,
    val ramUsagePercent: Float = 0f,
    val isLowMemory: Boolean = false,
    val thresholdBytes: Long = 0L,
    val jvmAllocatedBytes: Long = 0L,
    val jvmMaxBytes: Long = 0L,
    val totalStorageBytes: Long = 0L,
    val availableStorageBytes: Long = 0L,
    val usedStorageBytes: Long = 0L,
    val storageUsagePercent: Float = 0f
)
