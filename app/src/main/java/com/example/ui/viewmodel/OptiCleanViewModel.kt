package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.CleanupLogEntity
import com.example.data.model.AppCacheItem
import com.example.data.model.JunkScanOverview
import com.example.data.model.MemoryStats
import com.example.data.model.OptimizationResult
import com.example.scheduler.DailyCleanupScheduler
import com.example.util.CacheManager
import com.example.util.MemoryMonitor
import com.example.util.NotificationHelper
import com.example.util.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OptiCleanViewModel(application: Application) : AndroidViewModel(application) {

    private val cacheManager = CacheManager(application)
    private val memoryMonitor = MemoryMonitor(application)
    private val preferences = PreferencesManager(application)
    private val scheduler = DailyCleanupScheduler(application)
    private val notificationHelper = NotificationHelper(application)
    private val database = AppDatabase.getDatabase(application)
    private val logDao = database.cleanupLogDao()

    // Real-time Memory & Storage Stats
    private val _memoryStats = MutableStateFlow(memoryMonitor.getMemoryStats())
    val memoryStats: StateFlow<MemoryStats> = _memoryStats.asStateFlow()

    // Junk Scan
    private val _junkOverview = MutableStateFlow(JunkScanOverview(isScanning = true))
    val junkOverview: StateFlow<JunkScanOverview> = _junkOverview.asStateFlow()

    // Installed Apps Cache breakdown
    private val _installedApps = MutableStateFlow<List<AppCacheItem>>(emptyList())
    val installedApps: StateFlow<List<AppCacheItem>> = _installedApps.asStateFlow()

    private val _isScanningApps = MutableStateFlow(false)
    val isScanningApps: StateFlow<Boolean> = _isScanningApps.asStateFlow()

    // Permissions
    private val _hasUsageStatsPermission = MutableStateFlow(cacheManager.hasUsageStatsPermission())
    val hasUsageStatsPermission: StateFlow<Boolean> = _hasUsageStatsPermission.asStateFlow()

    // Scheduler settings
    private val _isDailyCleanupEnabled = MutableStateFlow(preferences.isDailyCleanupEnabled)
    val isDailyCleanupEnabled: StateFlow<Boolean> = _isDailyCleanupEnabled.asStateFlow()

    private val _dailyCleanupHour = MutableStateFlow(preferences.dailyCleanupHour)
    val dailyCleanupHour: StateFlow<Int> = _dailyCleanupHour.asStateFlow()

    private val _dailyCleanupMinute = MutableStateFlow(preferences.dailyCleanupMinute)
    val dailyCleanupMinute: StateFlow<Int> = _dailyCleanupMinute.asStateFlow()

    private val _isNotifyOnCleanup = MutableStateFlow(preferences.isNotifyOnCleanup)
    val isNotifyOnCleanup: StateFlow<Boolean> = _isNotifyOnCleanup.asStateFlow()

    private val _nextScheduledTime = MutableStateFlow(scheduler.getNextRunTimeMillis())
    val nextScheduledTime: StateFlow<Long> = _nextScheduledTime.asStateFlow()

    // Optimization State & Dialog
    private val _isOptimizing = MutableStateFlow(false)
    val isOptimizing: StateFlow<Boolean> = _isOptimizing.asStateFlow()

    private val _optimizationPhase = MutableStateFlow("")
    val optimizationPhase: StateFlow<String> = _optimizationPhase.asStateFlow()

    private val _lastResult = MutableStateFlow<OptimizationResult?>(null)
    val lastResult: StateFlow<OptimizationResult?> = _lastResult.asStateFlow()

    private val _showResultDialog = MutableStateFlow(false)
    val showResultDialog: StateFlow<Boolean> = _showResultDialog.asStateFlow()

    // Database History Flows
    val cleanupLogs: StateFlow<List<CleanupLogEntity>> = logDao.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalJunkCleared: StateFlow<Long> = logDao.getTotalJunkCleared()
        .map { it ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalRamFreed: StateFlow<Long> = logDao.getTotalRamFreed()
        .map { it ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalCleanupsCount: StateFlow<Int> = logDao.getTotalCleanupsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        // Start live memory telemetry polling loop
        viewModelScope.launch(Dispatchers.Default) {
            memoryMonitor.memoryTelemetryFlow(1500L).collect { stats ->
                _memoryStats.value = stats
            }
        }

        // Initial scan of installed apps & junk
        loadAppsAndScanJunk()
    }

    fun checkUsagePermission() {
        _hasUsageStatsPermission.value = cacheManager.hasUsageStatsPermission()
        if (_hasUsageStatsPermission.value) {
            loadAppsAndScanJunk()
        }
    }

    fun loadAppsAndScanJunk() {
        viewModelScope.launch(Dispatchers.IO) {
            _isScanningApps.value = true
            _junkOverview.value = _junkOverview.value.copy(isScanning = true)

            val apps = cacheManager.scanInstalledApps(preferences.whitelistedPackages)
            _installedApps.value = apps

            val junk = cacheManager.scanJunkOverview(apps)
            _junkOverview.value = junk
            _isScanningApps.value = false
        }
    }

    fun startOptimization(cleanupType: String = "MANUAL_BOOST") {
        if (_isOptimizing.value) return

        viewModelScope.launch(Dispatchers.IO) {
            _isOptimizing.value = true

            // Phase 1: Scanning memory & processes
            _optimizationPhase.value = "Analyzing active memory & background processes..."
            delay(600)

            // Phase 2: Purging cache directories
            _optimizationPhase.value = "Purging app cache & temporary file buffers..."
            delay(700)

            // Phase 3: Terminating cached background tasks & GC
            _optimizationPhase.value = "Reclaiming RAM & triggering system garbage collection..."
            val result = cacheManager.performOptimization(preferences.whitelistedPackages)
            delay(500)

            // Phase 4: Recording log
            logDao.insertLog(
                CleanupLogEntity(
                    timestamp = System.currentTimeMillis(),
                    freedRamBytes = result.freedRamBytes,
                    clearedJunkBytes = result.clearedJunkBytes,
                    processesKilledCount = result.processesKilledCount,
                    cleanupType = cleanupType,
                    notes = "Boosted system performance & reclaimed storage"
                )
            )

            preferences.lastCleanupTimestamp = System.currentTimeMillis()

            // Update stats
            _memoryStats.value = memoryMonitor.getMemoryStats()
            _lastResult.value = result
            _isOptimizing.value = false
            _showResultDialog.value = true

            // Refresh installed apps cache and junk list
            val apps = cacheManager.scanInstalledApps(preferences.whitelistedPackages)
            _installedApps.value = apps
            _junkOverview.value = cacheManager.scanJunkOverview(apps)
        }
    }

    fun testDailyCleanupNow() {
        startOptimization("DAILY_SCHEDULED")
    }

    fun dismissResultDialog() {
        _showResultDialog.value = false
    }

    fun toggleDailyCleanup(enabled: Boolean) {
        preferences.isDailyCleanupEnabled = enabled
        _isDailyCleanupEnabled.value = enabled
        if (enabled) {
            scheduler.scheduleDailyCleanup()
            _nextScheduledTime.value = scheduler.getNextRunTimeMillis()
        } else {
            scheduler.cancelDailyCleanup()
            _nextScheduledTime.value = 0L
        }
    }

    fun updateDailyCleanupTime(hour: Int, minute: Int) {
        preferences.dailyCleanupHour = hour
        preferences.dailyCleanupMinute = minute
        _dailyCleanupHour.value = hour
        _dailyCleanupMinute.value = minute

        if (preferences.isDailyCleanupEnabled) {
            scheduler.scheduleDailyCleanup()
            _nextScheduledTime.value = scheduler.getNextRunTimeMillis()
        }
    }

    fun toggleNotification(enabled: Boolean) {
        preferences.isNotifyOnCleanup = enabled
        _isNotifyOnCleanup.value = enabled
    }

    fun toggleAppWhitelist(packageName: String) {
        preferences.toggleWhitelist(packageName)
        val currentWhitelist = preferences.whitelistedPackages
        _installedApps.value = _installedApps.value.map { item ->
            if (item.packageName == packageName) {
                item.copy(isWhiteListed = currentWhitelist.contains(packageName))
            } else item
        }
    }

    fun clearOptimizationHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            logDao.clearAllLogs()
        }
    }

    fun getUsageAccessIntent() = cacheManager.getUsageAccessIntent()
    fun getAppDetailsIntent(packageName: String) = cacheManager.getAppDetailsIntent(packageName)
    fun getStorageSettingsIntent() = cacheManager.getStorageSettingsIntent()
}
