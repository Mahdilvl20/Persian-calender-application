package com.aistudio.lumacalendar.vtxk

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aistudio.lumacalendar.vtxk.notification.EventNotificationScheduler
import com.aistudio.lumacalendar.vtxk.notification.LumaNotificationManager
import com.aistudio.lumacalendar.vtxk.notification.NotificationPreferences
import com.aistudio.lumacalendar.vtxk.util.DateUtils
import com.aistudio.lumacalendar.vtxk.ui.components.AddEditEventSheet
import com.aistudio.lumacalendar.vtxk.ui.components.AmbientBackground
import com.aistudio.lumacalendar.vtxk.ui.components.EventDetailSheet
import com.aistudio.lumacalendar.vtxk.ui.components.GlassTabBar
import com.aistudio.lumacalendar.vtxk.ui.screens.CalendarScreen
import com.aistudio.lumacalendar.vtxk.ui.screens.SearchScreen
import com.aistudio.lumacalendar.vtxk.ui.screens.SettingsScreen
import com.aistudio.lumacalendar.vtxk.ui.theme.LumaCalendarTheme
import com.aistudio.lumacalendar.vtxk.ui.viewmodel.AccentPresets
import com.aistudio.lumacalendar.vtxk.ui.viewmodel.LumaViewModel
import com.aistudio.lumacalendar.vtxk.util.DynamicIconManager
import com.aistudio.lumacalendar.vtxk.util.LocalAppStrings
import com.aistudio.lumacalendar.vtxk.util.LocalCalendarType
import com.aistudio.lumacalendar.vtxk.util.LocalizationManager

class MainActivity : ComponentActivity() {
    private val viewModel: LumaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        EventNotificationScheduler.createChannel(applicationContext)
        LumaNotificationManager.createChannels(applicationContext)
        LumaNotificationManager.updateNotificationAsync(applicationContext)
        // Ensure MainActivity component state is enabled and sync dynamic date safely
        DynamicIconManager.ensureMainActivityEnabled(applicationContext)
        DynamicIconManager.syncIfDateChanged(applicationContext)
        setContent {
            LumaCalendarTheme {
                LumaApp(viewModel = viewModel)
            }
        }
        handleNotificationIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        LumaNotificationManager.updateNotificationAsync(applicationContext)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val extraAction = intent.getStringExtra("EXTRA_ACTION")

        when {
            action == LumaNotificationManager.ACTION_TODAY || extraAction == "TODAY" || action == "${packageName}.OPEN_TODAY" -> {
                val today = DateUtils.getRealDeviceDate()
                viewModel.setCurrentTab(0)
                viewModel.selectDate(today)
                intent.removeExtra("EXTRA_ACTION")
            }
            action == LumaNotificationManager.ACTION_NEW_EVENT || extraAction == "NEW_EVENT" -> {
                val today = DateUtils.getRealDeviceDate()
                viewModel.setCurrentTab(0)
                viewModel.selectDate(today)
                viewModel.openAddEvent(today)
                intent.removeExtra("EXTRA_ACTION")
            }
            else -> {
                val eventId = intent.getLongExtra(EventNotificationScheduler.EXTRA_EVENT_ID, -1L)
                if (eventId > 0) {
                    viewModel.openEventFromNotification(eventId)
                    intent.removeExtra(EventNotificationScheduler.EXTRA_EVENT_ID)
                }
            }
        }
    }
}

@Composable
fun LumaApp(viewModel: LumaViewModel) {
    val context = LocalContext.current
    val activity = context as Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    var enableAfterSettings by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        NotificationPreferences.markPermissionRequested(context)
        if (granted) viewModel.enableNotifications() else viewModel.disableNotifications()
    }

    fun requestNotificationAccess() {
        if (EventNotificationScheduler.hasNotificationPermission(context)) {
            viewModel.enableNotifications()
        } else if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            NotificationPreferences.wasPermissionRequested(context) &&
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
        ) {
            enableAfterSettings = true
            val settingsIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            } else {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(android.net.Uri.parse("package:${context.packageName}"))
            }
            context.startActivity(settingsIntent)
        } else {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(Unit) {
        if (!NotificationPreferences.wasPermissionRequested(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                NotificationPreferences.markPermissionRequested(context)
                viewModel.enableNotifications()
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (enableAfterSettings && EventNotificationScheduler.hasNotificationPermission(context)) {
                    enableAfterSettings = false
                    viewModel.enableNotifications()
                } else {
                    viewModel.refreshNotificationState()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val currentTab by viewModel.currentTab.collectAsState()
    val year by viewModel.selectedYear.collectAsState()
    val month by viewModel.selectedMonth.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val calendarViewMode by viewModel.calendarViewMode.collectAsState()
    val calendarType by viewModel.calendarType.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()

    val selectedDayEvents by viewModel.selectedDateEvents.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchCategory by viewModel.selectedCategoryFilter.collectAsState()
    val searchResults by viewModel.filteredSearchResults.collectAsState()

    // Modals state
    val isAddEditOpen by viewModel.isAddEditOpen.collectAsState()
    val editingEvent by viewModel.editingEvent.collectAsState()
    val isDetailOpen by viewModel.isDetailOpen.collectAsState()
    val viewingEvent by viewModel.viewingEvent.collectAsState()

    // Settings state
    val accentIndex by viewModel.accentColorIndex.collectAsState()
    val firstDayMonday by viewModel.firstDayMonday.collectAsState()
    val showWeekNumbers by viewModel.showWeekNumbers.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val personalVisible by viewModel.calendarPersonalVisible.collectAsState()
    val workVisible by viewModel.calendarWorkVisible.collectAsState()
    val holidaysVisible by viewModel.calendarHolidaysVisible.collectAsState()
    val themeName by viewModel.themeName.collectAsState()
    val persianDaysMap by viewModel.persianDaysMap.collectAsState()
    val isPersianLoading by viewModel.isPersianLoading.collectAsState()

    val currentAccent = AccentPresets.getOrElse(accentIndex) { AccentPresets[0] }.primary

    val layoutDirection = LocalizationManager.getLayoutDirection(calendarType)
    val appStrings = LocalizationManager.getStrings(calendarType)

    val tabItems = listOf(
        appStrings.tabCalendar to Icons.Outlined.CalendarMonth,
        appStrings.tabSearch to Icons.Default.Search,
        appStrings.tabSettings to Icons.Outlined.Settings
    )

    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
        LocalAppStrings provides appStrings,
        LocalCalendarType provides calendarType
    ) {
        val isOledTheme = themeName.contains("OLED", ignoreCase = true)
        AmbientBackground(accentGlow = currentAccent, isOled = isOledTheme) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = topInset)
            ) {
                val isTabletOrLandscape = maxWidth >= 640.dp
                val contentModifier = if (isTabletOrLandscape) {
                    Modifier
                        .widthIn(max = 840.dp)
                        .align(Alignment.TopCenter)
                        .fillMaxSize()
                } else {
                    Modifier
                        .fillMaxSize()
                }

                // Main Content Area with adaptive bounds
                Box(modifier = contentModifier) {
                    Crossfade(
                        targetState = currentTab,
                        animationSpec = tween(220),
                        label = "tab_crossfade"
                    ) { tab ->
                        when (tab) {
                            0 -> CalendarScreen(
                                year = year,
                                month = month,
                                selectedDate = selectedDate,
                                calendarViewMode = calendarViewMode,
                                calendarType = calendarType,
                                events = allEvents,
                                selectedDayEvents = selectedDayEvents,
                                firstDayMonday = firstDayMonday,
                                showWeekNumbers = showWeekNumbers,
                                onDateSelect = { viewModel.selectDate(it) },
                                onPrevMonth = { viewModel.changeMonth(-1) },
                                onNextMonth = { viewModel.changeMonth(1) },
                                onTodayClick = { viewModel.goToToday() },
                                onViewModeChange = { viewModel.setCalendarViewMode(it) },
                                onCalendarTypeChange = { viewModel.setCalendarType(it) },
                                onEventClick = { viewModel.openEventDetail(it) },
                                onAddEventClick = { viewModel.openAddEvent(it) },
                                persianDaysMap = persianDaysMap,
                                isPersianLoading = isPersianLoading
                            )
                            1 -> SearchScreen(
                                searchQuery = searchQuery,
                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                selectedCategory = searchCategory,
                                onCategoryChange = { viewModel.setCategoryFilter(it) },
                                searchResults = searchResults,
                                calendarType = calendarType,
                                onEventClick = { viewModel.openEventDetail(it) }
                            )
                            2 -> SettingsScreen(
                                accentColorIndex = accentIndex,
                                onAccentColorSelect = { viewModel.setAccentColorIndex(it) },
                                firstDayMonday = firstDayMonday,
                                onFirstDayMondayChange = { viewModel.setFirstDayMonday(it) },
                                showWeekNumbers = showWeekNumbers,
                                onShowWeekNumbersChange = { viewModel.setShowWeekNumbers(it) },
                                notificationsEnabled = notificationsEnabled,
                                onNotificationsChange = { enabled ->
                                    if (enabled) requestNotificationAccess() else viewModel.disableNotifications()
                                },
                                personalVisible = personalVisible,
                                onTogglePersonal = { viewModel.toggleCalendarPersonal() },
                                workVisible = workVisible,
                                onToggleWork = { viewModel.toggleCalendarWork() },
                                holidaysVisible = holidaysVisible,
                                onToggleHolidays = { viewModel.toggleCalendarHolidays() },
                                themeName = themeName,
                                onThemeSelect = { viewModel.setThemeName(it) },
                                onResetSampleData = { viewModel.resetToSampleData() },
                                onClearAllData = { viewModel.clearAllData() }
                            )
                        }
                    }

                    // Translucent Liquid Glass Tab Bar positioned responsively above Navigation Bar
                    GlassTabBar(
                        selectedTabIndex = currentTab,
                        onTabSelected = { viewModel.setCurrentTab(it) },
                        items = tabItems,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp + bottomInset)
                    )
                }
            }

            // Modals & Sheets
            AddEditEventSheet(
                isOpen = isAddEditOpen,
                event = editingEvent,
                defaultDate = selectedDate,
                activeCalendarType = calendarType,
                onDismiss = { viewModel.closeAddEdit() },
                onSave = { id, title, date, time, cat, hex, loc, notes, reminder, calType ->
                    viewModel.saveEvent(
                        id = id,
                        title = title,
                        date = date,
                        time = time,
                        category = cat,
                        colorHex = hex,
                        location = loc,
                        notes = notes,
                        reminderMinutes = reminder,
                        calendarType = calType
                    )
                }
            )

            EventDetailSheet(
                isOpen = isDetailOpen,
                event = viewingEvent,
                calendarType = calendarType,
                onDismiss = { viewModel.closeEventDetail() },
                onEdit = { viewModel.openEditEvent(it) },
                onDelete = { viewModel.deleteEvent(it) }
            )
        }
    }
}
