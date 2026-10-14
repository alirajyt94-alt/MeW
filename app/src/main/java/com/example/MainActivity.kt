package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.OptimizationInProgressDialog
import com.example.ui.components.OptimizationSuccessDialog
import com.example.ui.screens.AppCacheScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SchedulerScreen
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.OptiCleanTheme
import com.example.ui.viewmodel.OptiCleanViewModel

enum class NavigationTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Speed),
    APPS("App Cache", Icons.Default.Apps),
    SCHEDULER("Schedule", Icons.Default.Schedule),
    HISTORY("History", Icons.Default.History)
}

class MainActivity : ComponentActivity() {

    private val viewModel: OptiCleanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OptiCleanTheme {
                val context = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current

                // Check permissions on resume
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            viewModel.checkUsagePermission()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                // Notification Permission Request for Android 13+
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { /* handle permission response */ }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                // Observe States
                val memoryStats by viewModel.memoryStats.collectAsStateWithLifecycle()
                val junkOverview by viewModel.junkOverview.collectAsStateWithLifecycle()
                val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
                val isScanningApps by viewModel.isScanningApps.collectAsStateWithLifecycle()
                val hasUsagePermission by viewModel.hasUsageStatsPermission.collectAsStateWithLifecycle()

                val isDailyEnabled by viewModel.isDailyCleanupEnabled.collectAsStateWithLifecycle()
                val scheduledHour by viewModel.dailyCleanupHour.collectAsStateWithLifecycle()
                val scheduledMinute by viewModel.dailyCleanupMinute.collectAsStateWithLifecycle()
                val isNotifyEnabled by viewModel.isNotifyOnCleanup.collectAsStateWithLifecycle()
                val nextScheduledTime by viewModel.nextScheduledTime.collectAsStateWithLifecycle()

                val isOptimizing by viewModel.isOptimizing.collectAsStateWithLifecycle()
                val optimizationPhase by viewModel.optimizationPhase.collectAsStateWithLifecycle()
                val lastResult by viewModel.lastResult.collectAsStateWithLifecycle()
                val showResultDialog by viewModel.showResultDialog.collectAsStateWithLifecycle()

                val cleanupLogs by viewModel.cleanupLogs.collectAsStateWithLifecycle()
                val totalJunkCleared by viewModel.totalJunkCleared.collectAsStateWithLifecycle()
                val totalRamFreed by viewModel.totalRamFreed.collectAsStateWithLifecycle()
                val totalCleanupsCount by viewModel.totalCleanupsCount.collectAsStateWithLifecycle()

                var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
                val snackbarHostState = remember { SnackbarHostState() }

                // BackHandler: if not on Dashboard, navigate back to Dashboard
                BackHandler(enabled = currentTab != NavigationTab.DASHBOARD) {
                    currentTab = NavigationTab.DASHBOARD
                }

                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        NavigationBar(
                            containerColor = DarkSurface,
                            tonalElevation = 8.dp,
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            NavigationTab.values().forEach { tab ->
                                val selected = currentTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.title,
                                            tint = if (selected) EmeraldNeon else Color(0xFF94A3B8)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            color = if (selected) EmeraldNeon else Color(0xFF94A3B8)
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = Color(0xFF063A29)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            NavigationTab.DASHBOARD -> DashboardScreen(
                                memoryStats = memoryStats,
                                junkOverview = junkOverview,
                                isOptimizing = isOptimizing,
                                onBoostClicked = { viewModel.startOptimization("MANUAL_BOOST") },
                                onNavigateToApps = { currentTab = NavigationTab.APPS },
                                onNavigateToScheduler = { currentTab = NavigationTab.SCHEDULER },
                                onOpenStorageSettings = {
                                    context.startActivity(viewModel.getStorageSettingsIntent())
                                },
                                onRefreshTelemetry = {
                                    viewModel.loadAppsAndScanJunk()
                                }
                            )

                            NavigationTab.APPS -> AppCacheScreen(
                                apps = installedApps,
                                isScanning = isScanningApps,
                                hasUsageStatsPermission = hasUsagePermission,
                                onRequestUsageAccess = {
                                    context.startActivity(viewModel.getUsageAccessIntent())
                                },
                                onOpenAppDetails = { pkg ->
                                    context.startActivity(viewModel.getAppDetailsIntent(pkg))
                                },
                                onOpenStorageSettings = {
                                    context.startActivity(viewModel.getStorageSettingsIntent())
                                },
                                onToggleWhitelist = { pkg ->
                                    viewModel.toggleAppWhitelist(pkg)
                                },
                                onRefresh = { viewModel.loadAppsAndScanJunk() }
                            )

                            NavigationTab.SCHEDULER -> SchedulerScreen(
                                isDailyEnabled = isDailyEnabled,
                                scheduledHour = scheduledHour,
                                scheduledMinute = scheduledMinute,
                                isNotifyEnabled = isNotifyEnabled,
                                nextScheduledTime = nextScheduledTime,
                                onToggleDaily = { viewModel.toggleDailyCleanup(it) },
                                onUpdateTime = { h, m -> viewModel.updateDailyCleanupTime(h, m) },
                                onToggleNotify = { viewModel.toggleNotification(it) },
                                onTestCleanupNow = { viewModel.testDailyCleanupNow() }
                            )

                            NavigationTab.HISTORY -> HistoryScreen(
                                logs = cleanupLogs,
                                totalJunkCleared = totalJunkCleared,
                                totalRamFreed = totalRamFreed,
                                totalCleanupsCount = totalCleanupsCount,
                                onClearHistory = { viewModel.clearOptimizationHistory() }
                            )
                        }

                        // Optimization In-Progress Dialog
                        if (isOptimizing) {
                            OptimizationInProgressDialog(phaseText = optimizationPhase)
                        }

                        // Optimization Success Dialog
                        if (showResultDialog && lastResult != null) {
                            OptimizationSuccessDialog(
                                result = lastResult!!,
                                onDismiss = { viewModel.dismissResultDialog() }
                            )
                        }
                    }
                }
            }
        }
    }
}
