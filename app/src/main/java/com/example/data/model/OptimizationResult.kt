package com.example.data.model

data class OptimizationResult(
    val freedRamBytes: Long = 0L,
    val clearedJunkBytes: Long = 0L,
    val processesKilledCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuccess: Boolean = true,
    val summaryMessage: String = ""
)
