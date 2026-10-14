package com.example.util

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.example.data.model.MemoryStats
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

class MemoryMonitor(private val context: Context) {

    private val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    fun getMemoryStats(): MemoryStats {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val totalRam = memInfo.totalMem
        val availRam = memInfo.availMem
        val usedRam = (totalRam - availRam).coerceAtLeast(0L)
        val ramPercent = if (totalRam > 0) {
            ((usedRam.toDouble() / totalRam.toDouble()) * 100.0).toFloat().coerceIn(0f, 100f)
        } else 0f

        val runtime = Runtime.getRuntime()
        val jvmAllocated = runtime.totalMemory() - runtime.freeMemory()
        val jvmMax = runtime.maxMemory()

        var totalStorage = 0L
        var availStorage = 0L
        var usedStorage = 0L
        var storagePercent = 0f

        try {
            val statFs = StatFs(Environment.getDataDirectory().path)
            val blockSize = statFs.blockSizeLong
            val totalBlocks = statFs.blockCountLong
            val availableBlocks = statFs.availableBlocksLong

            totalStorage = totalBlocks * blockSize
            availStorage = availableBlocks * blockSize
            usedStorage = (totalStorage - availStorage).coerceAtLeast(0L)
            if (totalStorage > 0) {
                storagePercent = ((usedStorage.toDouble() / totalStorage.toDouble()) * 100.0).toFloat()
                    .coerceIn(0f, 100f)
            }
        } catch (_: Exception) {
            // Fallback for restricted storage environments
        }

        return MemoryStats(
            totalRamBytes = totalRam,
            availableRamBytes = availRam,
            usedRamBytes = usedRam,
            ramUsagePercent = ramPercent,
            isLowMemory = memInfo.lowMemory,
            thresholdBytes = memInfo.threshold,
            jvmAllocatedBytes = jvmAllocated,
            jvmMaxBytes = jvmMax,
            totalStorageBytes = totalStorage,
            availableStorageBytes = availStorage,
            usedStorageBytes = usedStorage,
            storageUsagePercent = storagePercent
        )
    }

    /**
     * Emits live memory telemetry every [intervalMs] milliseconds.
     */
    fun memoryTelemetryFlow(intervalMs: Long = 1500L): Flow<MemoryStats> = flow {
        while (currentCoroutineContext().isActive) {
            emit(getMemoryStats())
            delay(intervalMs)
        }
    }
}
