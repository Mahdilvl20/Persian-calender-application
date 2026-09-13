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
    private val initialYearMonth = CalendarConverter.getYearAndMonth(initialDeviceDate, CalendarType.GREGORIAN)

    private val _selectedYear = MutableStateFlow(initialYearMonth.first)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(initialYearMonth.second)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow(initialDeviceDate)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _calendarViewMode = MutableStateFlow("Month") // "Month", "Week", "Day"
    val calendarViewMode: StateFlow<String> = _calendarViewMode.asStateFlow()

    // Calendar Type: شمسی (Jalali), میلادی (Gregorian), قمری (Hijri)
    private val _calendarType = MutableStateFlow(CalendarType.GREGORIAN)
    val calendarType: StateFlow<CalendarType> = _calendarType.asStateFlow()

    // Events flow from Room Database
    val allEvents: StateFlow<List<CalendarEvent>> = repository.allEvents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Events for the selected date
    val selectedDateEvents: StateFlow<List<CalendarEvent>> = combine(allEvents, _selectedDate) { events, selDate ->
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

    // Settings State
    private val _accentColorIndex = MutableStateFlow(0)
    val accentColorIndex: StateFlow<Int> = _accentColorIndex.asStateFlow()

    private val _firstDayMonday = MutableStateFlow(false) // Prompt: Sun, Mon, Tue...
    val firstDayMonday: StateFlow<Boolean> = _firstDayMonday.asStateFlow()

    private val _showWeekNumbers = MutableStateFlow(false)
    val showWeekNumbers: StateFlow<Boolean> = _showWeekNumbers.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(
        NotificationPreferences.isEnabled(application) &&
            EventNotificationScheduler.hasNotificationPermission(application)
    )
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()
    private var notificationSyncJob: Job? = null

    private val _calendarPersonalVisible = MutableStateFlow(true)
    val calendarPersonalVisible: StateFlow<Boolean> = _calendarPersonalVisible.asStateFlow()

    private val _calendarWorkVisible = MutableStateFlow(true)
    val calendarWorkVisible: StateFlow<Boolean> = _calendarWorkVisible.asStateFlow()

    private val _calendarHolidaysVisible = MutableStateFlow(true)
    val calendarHolidaysVisible: StateFlow<Boolean> = _calendarHolidaysVisible.asStateFlow()

    private val _themeName = MutableStateFlow("Liquid Glass (Dark)")
    val themeName: StateFlow<String> = _themeName.asStateFlow()

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
                title = title.ifBlank { "Untitled Event" },
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
            closeAddEdit()
        }
    }

    fun deleteEvent(event: CalendarEvent) {
        viewModelScope.launch {
            EventNotificationScheduler.cancel(getApplication(), event.id)
            repository.deleteEvent(event)
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
        _notificationsEnabled.value = NotificationPreferences.isEnabled(getApplication()) &&
            EventNotificationScheduler.hasNotificationPermission(getApplication())
    }

    fun enableNotifications() {
        NotificationPreferences.setEnabled(getApplication(), true)
        refreshNotificationState()
        if (!_notificationsEnabled.value) return
        EventNotificationScheduler.createChannel(getApplication())
        val previousJob = notificationSyncJob
        notificationSyncJob = viewModelScope.launch(Dispatchers.IO) {
            previousJob?.cancelAndJoin()
            repository.getAllEventsSnapshot().forEach {
                if (NotificationPreferences.isEnabled(getApplication())) {
                    EventNotificationScheduler.schedule(getApplication(), it)
                }
            }
        }
    }

    fun disableNotifications() {
        NotificationPreferences.setEnabled(getApplication(), false)
        _notificationsEnabled.value = false
        val previousJob = notificationSyncJob
        notificationSyncJob = viewModelScope.launch(Dispatchers.IO) {
            previousJob?.cancelAndJoin()
            EventNotificationScheduler.cancelAll(getApplication(), repository.getAllEventsSnapshot())
        }
    }

    // Settings mutators
    fun setAccentColorIndex(index: Int) {
        _accentColorIndex.value = index
    }

    fun setFirstDayMonday(isMonday: Boolean) {
        _firstDayMonday.value = isMonday
    }

    fun setShowWeekNumbers(show: Boolean) {
        _showWeekNumbers.value = show
    }

    fun toggleCalendarPersonal() {
        _calendarPersonalVisible.value = !_calendarPersonalVisible.value
    }

    fun toggleCalendarWork() {
        _calendarWorkVisible.value = !_calendarWorkVisible.value
    }

    fun toggleCalendarHolidays() {
        _calendarHolidaysVisible.value = !_calendarHolidaysVisible.value
    }

    fun setThemeName(name: String) {
        _themeName.value = name
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
