package com.aistudio.lumacalendar.vtxk.ui.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.lumacalendar.vtxk.data.CalendarEvent
import com.aistudio.lumacalendar.vtxk.data.EventRepository
import com.aistudio.lumacalendar.vtxk.data.LumaDatabase
import com.aistudio.lumacalendar.vtxk.data.holiday.HolidayRepositoryImpl
import com.aistudio.lumacalendar.vtxk.data.holiday.HolidayService
import com.aistudio.lumacalendar.vtxk.data.model.PersianCalendarDay
import com.aistudio.lumacalendar.vtxk.data.repository.PersianCalendarRepository
import com.aistudio.lumacalendar.vtxk.data.repository.PersianCalendarRepositoryImpl
import com.aistudio.lumacalendar.vtxk.notification.EventNotificationScheduler
import com.aistudio.lumacalendar.vtxk.notification.LumaNotificationManager
import com.aistudio.lumacalendar.vtxk.notification.NotificationPreferences
import com.aistudio.lumacalendar.vtxk.ui.theme.AccentCyan
import com.aistudio.lumacalendar.vtxk.ui.theme.AccentDeepViolet
import com.aistudio.lumacalendar.vtxk.ui.theme.AccentElectricBlue
import com.aistudio.lumacalendar.vtxk.ui.theme.AccentRoyalViolet
import com.aistudio.lumacalendar.vtxk.ui.theme.CategoryFinance
import com.aistudio.lumacalendar.vtxk.ui.theme.CategoryHealth
import com.aistudio.lumacalendar.vtxk.ui.theme.CategoryPersonal
import com.aistudio.lumacalendar.vtxk.ui.theme.CategorySocial
import com.aistudio.lumacalendar.vtxk.ui.theme.CategorySpecial
import com.aistudio.lumacalendar.vtxk.ui.theme.CategoryWork
import com.aistudio.lumacalendar.vtxk.util.CalendarConverter
import com.aistudio.lumacalendar.vtxk.util.CalendarType
import com.aistudio.lumacalendar.vtxk.util.DateUtils
import com.aistudio.lumacalendar.vtxk.util.TimeValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import java.util.Calendar

data class CategoryMeta(
    val name: String,
    val color: Color,
    val hex: String
)

val AvailableCategories = listOf(
    CategoryMeta("Work", CategoryWork, "#6366F1"),
    CategoryMeta("Personal", CategoryPersonal, "#38BDF8"),
    CategoryMeta("Health", CategoryHealth, "#10B981"),
    CategoryMeta("Social", CategorySocial, "#A855F7"),
    CategoryMeta("Finance", CategoryFinance, "#F59E0B"),
    CategoryMeta("Special", CategorySpecial, "#FB7185")
)

data class AccentPreset(
    val name: String,
    val primary: Color,
    val secondary: Color
)

val AccentPresets = listOf(
    AccentPreset("Electric Blue", AccentElectricBlue, AccentRoyalViolet),
    AccentPreset("Royal Violet", AccentRoyalViolet, AccentDeepViolet),
    AccentPreset("Neon Cyan", AccentCyan, AccentElectricBlue),
    AccentPreset("Emerald Mint", CategoryHealth, AccentCyan),
    AccentPreset("Radiant Coral", CategorySpecial, AccentRoyalViolet)
)

/**
 * Applies the three category-visibility toggles to an event list.
 * An event whose `calendarType` is outside the known three stays visible so
 * nothing silently vanishes (FR-003).
 */
internal fun filterVisibleEvents(
    events: List<CalendarEvent>,
    personalVisible: Boolean,
    workVisible: Boolean,
    holidaysVisible: Boolean
): List<CalendarEvent> = events.filter { event ->
    when (event.calendarType) {
        "Personal" -> personalVisible
        "Work" -> workVisible
        "Holidays" -> holidaysVisible
        else -> true
    }
}

class LumaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EventRepository
    val persianCalendarRepository: PersianCalendarRepository = PersianCalendarRepositoryImpl(application)

    // Live Persian Calendar Days state flow: Map of Gregorian date ("YYYY-MM-DD") and Shamsi date ("YYYY/MM/DD") to PersianCalendarDay
    private val _persianDaysMap = MutableStateFlow<Map<String, PersianCalendarDay>>(emptyMap())
    val persianDaysMap: StateFlow<Map<String, PersianCalendarDay>> = _persianDaysMap.asStateFlow()

    private val _isPersianLoading = MutableStateFlow(false)
    val isPersianLoading: StateFlow<Boolean> = _isPersianLoading.asStateFlow()

    private val _persianApiError = MutableStateFlow<String?>(null)
    val persianApiError: StateFlow<String?> = _persianApiError.asStateFlow()

    init {
        val db = LumaDatabase.getDatabase(application, viewModelScope)
        repository = EventRepository(db.eventDao())
        HolidayService.default.updatePersianCalendarRepository(persianCalendarRepository)
        // Pre-populate memory cache from repository for instant display
        persianCalendarRepository.getCachedDaysForYear(1404)?.let { updatePersianDaysMap(it) }
        persianCalendarRepository.getCachedDaysForYear(1405)?.let { updatePersianDaysMap(it) }
    }

    // Navigation & Tab State (0: Calendar, 1: Search, 2: Settings)
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun setCurrentTab(tab: Int) {
        _currentTab.value = tab
    }

    // Calendar Display State (Initialized to actual device local date)
    private val initialDeviceDate = DateUtils.getRealDeviceDate()
    private val initialCalendarType = NotificationPreferences.getCalendarType(application)
    private val initialYearMonth = CalendarConverter.getYearAndMonth(initialDeviceDate, initialCalendarType)

    private val _selectedYear = MutableStateFlow(initialYearMonth.first)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(initialYearMonth.second)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow(initialDeviceDate)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _calendarViewMode = MutableStateFlow("Month") // "Month", "Week", "Day"
    val calendarViewMode: StateFlow<String> = _calendarViewMode.asStateFlow()

    // Calendar Type: شمسی (Jalali), میلادی (Gregorian), قمری (Hijri)
    private val _calendarType = MutableStateFlow(initialCalendarType)
    val calendarType: StateFlow<CalendarType> = _calendarType.asStateFlow()

    // Events flow from Room Database
    val allEvents: StateFlow<List<CalendarEvent>> = repository.allEvents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Category visibility (Settings toggles), restored from preferences on startup
    private val _calendarPersonalVisible = MutableStateFlow(
        NotificationPreferences.getCategoryPersonalVisible(application)
    )
    val calendarPersonalVisible: StateFlow<Boolean> = _calendarPersonalVisible.asStateFlow()

    private val _calendarWorkVisible = MutableStateFlow(
        NotificationPreferences.getCategoryWorkVisible(application)
    )
    val calendarWorkVisible: StateFlow<Boolean> = _calendarWorkVisible.asStateFlow()

    private val _calendarHolidaysVisible = MutableStateFlow(
        NotificationPreferences.getCategoryHolidaysVisible(application)
    )
    val calendarHolidaysVisible: StateFlow<Boolean> = _calendarHolidaysVisible.asStateFlow()

    // Calendar views honour the toggles; Search keeps using unfiltered allEvents (FR-002a)
    val visibleEvents: StateFlow<List<CalendarEvent>> = combine(
        allEvents,
        _calendarPersonalVisible,
        _calendarWorkVisible,
        _calendarHolidaysVisible
    ) { events, personal, work, holidays ->
        filterVisibleEvents(events, personal, work, holidays)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Events for the selected date, respecting category visibility
    val selectedDateEvents: StateFlow<List<CalendarEvent>> =
        combine(visibleEvents, _selectedDate) { events, selDate ->
            events.filter { it.date == selDate }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Search & Filter State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    val filteredSearchResults: StateFlow<List<CalendarEvent>> = combine(
        allEvents,
        _searchQuery,
        _selectedCategoryFilter
    ) { events, query, category ->
        events.filter { event ->
            val matchesQuery = query.isBlank() ||
                event.title.contains(query, ignoreCase = true) ||
                event.notes.contains(query, ignoreCase = true) ||
                event.location.contains(query, ignoreCase = true) ||
                event.category.contains(query, ignoreCase = true)

            val matchesCategory = category == "All" || event.category.equals(category, ignoreCase = true)

            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Modals & Dialogs State
    private val _isAddEditOpen = MutableStateFlow(false)
    val isAddEditOpen: StateFlow<Boolean> = _isAddEditOpen.asStateFlow()

    private val _editingEvent = MutableStateFlow<CalendarEvent?>(null)
    val editingEvent: StateFlow<CalendarEvent?> = _editingEvent.asStateFlow()

    private val _isDetailOpen = MutableStateFlow(false)
    val isDetailOpen: StateFlow<Boolean> = _isDetailOpen.asStateFlow()

    private val _viewingEvent = MutableStateFlow<CalendarEvent?>(null)
    val viewingEvent: StateFlow<CalendarEvent?> = _viewingEvent.asStateFlow()

    // Settings State (restored from preferences on startup, SP-003)
    private val _accentColorIndex = MutableStateFlow(
        NotificationPreferences.getAccentColorIndex(application)
            .coerceIn(0, AccentPresets.lastIndex)
    )
    val accentColorIndex: StateFlow<Int> = _accentColorIndex.asStateFlow()

    private val _firstDayMonday = MutableStateFlow(
        NotificationPreferences.getFirstDayMonday(application)
    ) // Prompt: Sun, Mon, Tue...
    val firstDayMonday: StateFlow<Boolean> = _firstDayMonday.asStateFlow()

    private val _showWeekNumbers = MutableStateFlow(
        NotificationPreferences.getShowWeekNumbers(application)
    )
    val showWeekNumbers: StateFlow<Boolean> = _showWeekNumbers.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(
        NotificationPreferences.isEnabled(application) &&
            EventNotificationScheduler.hasNotificationPermission(application)
    )
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()
    private var notificationSyncJob: Job? = null

    private val _snoozeMinutes = MutableStateFlow(
        NotificationPreferences.getSnoozeMinutes(application)
    )
    val snoozeMinutes: StateFlow<Int> = _snoozeMinutes.asStateFlow()

    private val _themeName = MutableStateFlow(
        sanitizeThemeName(NotificationPreferences.getThemeName(application))
    )
    val themeName: StateFlow<String> = _themeName.asStateFlow()

    /**
     * VR-002: a stored theme that matches neither offered option falls back to the default,
     * so a restart never applies an unknown theme.
     */
    private fun sanitizeThemeName(name: String): String =
        if (name.contains("Liquid Glass", ignoreCase = true) || name.contains("OLED", ignoreCase = true)) {
            name
        } else {
            NotificationPreferences.DEFAULT_THEME_NAME
        }

    // Actions
    fun loadPersianYear(year: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val cached = persianCalendarRepository.getCachedDaysForYear(year)
            if (cached != null && cached.isNotEmpty()) {
                updatePersianDaysMap(cached)
                _isPersianLoading.value = false
                return@launch
            }

            _isPersianLoading.value = true
            _persianApiError.value = null
            val result = persianCalendarRepository.getDaysForYear(year)
            result.onSuccess { days ->
                updatePersianDaysMap(days)
                HolidayService.default.updatePersianCalendarRepository(persianCalendarRepository)
                _isPersianLoading.value = false
                _persianApiError.value = null
            }.onFailure { err ->
                _isPersianLoading.value = false
                _persianApiError.value = err.message
            }
        }
    }

    private fun updatePersianDaysMap(days: List<PersianCalendarDay>) {
        val map = _persianDaysMap.value.toMutableMap()
        days.forEach { day ->
            map[day.date] = day
            map[day.shamsiDate] = day
        }
        _persianDaysMap.value = map
    }

    private fun checkAndLoadPersianYearIfNeeded() {
        if (_calendarType.value == CalendarType.JALALI) {
            val jalaliYear = _selectedYear.value
            loadPersianYear(jalaliYear)
        }
    }

    fun selectDate(dateStr: String) {
        _selectedDate.value = dateStr
        // Synchronize month and year in current calendar type
        val (newYear, newMonth) = CalendarConverter.getYearAndMonth(dateStr, _calendarType.value)
        _selectedYear.value = newYear
        _selectedMonth.value = newMonth
        checkAndLoadPersianYearIfNeeded()
    }

    fun changeMonth(delta: Int) {
        val (newYear, newMonth) = DateUtils.addMonths(
            year = _selectedYear.value,
            month = _selectedMonth.value,
            delta = delta,
            calendarType = _calendarType.value
        )
        _selectedYear.value = newYear
        _selectedMonth.value = newMonth
        checkAndLoadPersianYearIfNeeded()
    }

    fun goToToday() {
        val today = DateUtils.getRealDeviceDate()
        _selectedDate.value = today
        val (newYear, newMonth) = CalendarConverter.getYearAndMonth(today, _calendarType.value)
        _selectedYear.value = newYear
        _selectedMonth.value = newMonth
        checkAndLoadPersianYearIfNeeded()
    }

    fun setCalendarType(type: CalendarType) {
        if (_calendarType.value == type) return
        _calendarType.value = type
        NotificationPreferences.setCalendarType(getApplication(), type)
        LumaNotificationManager.updateNotificationAsync(getApplication())
        // Synchronize month and year to the selected date in the new calendar type!
        val (newYear, newMonth) = CalendarConverter.getYearAndMonth(_selectedDate.value, type)
        _selectedYear.value = newYear
        _selectedMonth.value = newMonth
        checkAndLoadPersianYearIfNeeded()
    }

    fun setCalendarViewMode(mode: String) {
        _calendarViewMode.value = mode
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun openAddEvent(initialDate: String? = null) {
        if (initialDate != null) {
            _selectedDate.value = initialDate
        }
        _editingEvent.value = null
        _isAddEditOpen.value = true
    }

    fun openEditEvent(event: CalendarEvent) {
        _editingEvent.value = event
        _isDetailOpen.value = false
        _isAddEditOpen.value = true
    }

    fun closeAddEdit() {
        _isAddEditOpen.value = false
        _editingEvent.value = null
    }

    fun openEventDetail(event: CalendarEvent) {
        _viewingEvent.value = event
        _isDetailOpen.value = true
    }

    fun closeEventDetail() {
        _isDetailOpen.value = false
        _viewingEvent.value = null
    }

    fun saveEvent(
        id: Long = 0,
        title: String,
        date: String,
        time: String,
        category: String,
        colorHex: String,
        location: String,
        notes: String,
        reminderMinutes: Int,
        calendarType: String
    ) {
        val normTime = TimeValidator.normalizeTime(time, "11:00")
        saveEvent(
            id = id,
            title = title,
            date = date,
            startTime = normTime,
            endTime = normTime,
            category = category,
            colorHex = colorHex,
            location = location,
            notes = notes,
            reminderMinutes = reminderMinutes,
            calendarType = calendarType
        )
    }

    fun saveEvent(
        id: Long = 0,
        title: String,
        date: String,
        startTime: String,
        endTime: String,
        category: String,
        colorHex: String,
        location: String,
        notes: String,
        reminderMinutes: Int,
        calendarType: String
    ) {
        viewModelScope.launch {
            val normStart = TimeValidator.normalizeTime(startTime, "09:00")
            val normEnd = TimeValidator.normalizeTime(endTime, "10:00")
            val (safeStart, safeEnd) = TimeValidator.ensureValidRange(normStart, normEnd)

            val event = CalendarEvent(
                id = id,
                title = title.trim(),
                date = date,
                startTime = safeStart,
                endTime = safeEnd,
                category = category,
                colorHex = colorHex,
                location = location,
                notes = notes,
                reminderMinutes = reminderMinutes,
                calendarType = calendarType
            )
            val savedEvent = if (id == 0L) {
                event.copy(id = repository.insertEvent(event))
            } else {
                repository.updateEvent(event)
                event
            }
            EventNotificationScheduler.schedule(getApplication(), savedEvent)
            LumaNotificationManager.updateNotification(getApplication())
            closeAddEdit()
        }
    }

    fun deleteEvent(event: CalendarEvent) {
        viewModelScope.launch {
            EventNotificationScheduler.cancel(getApplication(), event.id)
            repository.deleteEvent(event)
            LumaNotificationManager.updateNotification(getApplication())
            closeEventDetail()
        }
    }

    fun openEventFromNotification(eventId: Long) {
        if (eventId <= 0) return
        viewModelScope.launch {
            val event = repository.getEventById(eventId) ?: return@launch
            _currentTab.value = 0
            selectDate(event.date)
            openEventDetail(event)
        }
    }

    fun refreshNotificationState() {
        val hasPerm = EventNotificationScheduler.hasNotificationPermission(getApplication())
        val isDaily = NotificationPreferences.isDailyNotificationEnabled(getApplication())
        val isReminders = NotificationPreferences.areEventRemindersEnabled(getApplication())
        _notificationsEnabled.value = (isDaily || isReminders) && hasPerm
    }

    fun enableNotifications() {
        NotificationPreferences.setEnabled(getApplication(), true)
        refreshNotificationState()
        EventNotificationScheduler.createChannel(getApplication())
        LumaNotificationManager.createChannels(getApplication())
        if (EventNotificationScheduler.hasNotificationPermission(getApplication())) {
            LumaNotificationManager.updateNotificationAsync(getApplication())
            LumaNotificationManager.scheduleMidnightUpdate(getApplication())
        }
        val previousJob = notificationSyncJob
        notificationSyncJob = viewModelScope.launch(Dispatchers.IO) {
            previousJob?.cancelAndJoin()
            repository.getAllEventsSnapshot().forEach {
                if (NotificationPreferences.areEventRemindersEnabled(getApplication())) {
                    EventNotificationScheduler.schedule(getApplication(), it)
                }
            }
        }
    }

    fun disableNotifications() {
        NotificationPreferences.setEnabled(getApplication(), false)
        _notificationsEnabled.value = false
        LumaNotificationManager.cancel(getApplication(), "User disabled notifications in app settings")
        val previousJob = notificationSyncJob
        notificationSyncJob = viewModelScope.launch(Dispatchers.IO) {
            previousJob?.cancelAndJoin()
            EventNotificationScheduler.cancelAll(getApplication(), repository.getAllEventsSnapshot())
        }
    }

    // Settings mutators: persist first, then update the StateFlow so a restart
    // reproduces the last in-session value.
    fun setAccentColorIndex(index: Int) {
        val safe = index.coerceIn(0, AccentPresets.lastIndex)
        NotificationPreferences.setAccentColorIndex(getApplication(), safe)
        _accentColorIndex.value = safe
    }

    fun setFirstDayMonday(isMonday: Boolean) {
        NotificationPreferences.setFirstDayMonday(getApplication(), isMonday)
        _firstDayMonday.value = isMonday
    }

    fun setShowWeekNumbers(show: Boolean) {
        NotificationPreferences.setShowWeekNumbers(getApplication(), show)
        _showWeekNumbers.value = show
    }

    fun setSnoozeMinutes(minutes: Int) {
        NotificationPreferences.setSnoozeMinutes(getApplication(), minutes)
        _snoozeMinutes.value = NotificationPreferences.getSnoozeMinutes(getApplication())
    }

    fun toggleCalendarPersonal() {
        val next = !_calendarPersonalVisible.value
        NotificationPreferences.setCategoryPersonalVisible(getApplication(), next)
        _calendarPersonalVisible.value = next
    }

    fun toggleCalendarWork() {
        val next = !_calendarWorkVisible.value
        NotificationPreferences.setCategoryWorkVisible(getApplication(), next)
        _calendarWorkVisible.value = next
    }

    fun toggleCalendarHolidays() {
        val next = !_calendarHolidaysVisible.value
        NotificationPreferences.setCategoryHolidaysVisible(getApplication(), next)
        _calendarHolidaysVisible.value = next
    }

    fun setThemeName(name: String) {
        val safe = sanitizeThemeName(name)
        NotificationPreferences.setThemeName(getApplication(), safe)
        _themeName.value = safe
    }

    fun resetToSampleData() {
        viewModelScope.launch {
            val events = repository.getAllEventsSnapshot()
            EventNotificationScheduler.cancelAll(getApplication(), events)
            repository.seedInitialData()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            val events = repository.getAllEventsSnapshot()
            EventNotificationScheduler.cancelAll(getApplication(), events)
            repository.clearAll()
        }
    }
}
