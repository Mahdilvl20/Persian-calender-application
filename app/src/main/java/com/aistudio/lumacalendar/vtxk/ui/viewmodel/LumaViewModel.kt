package com.aistudio.lumacalendar.vtxk.ui.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.lumacalendar.vtxk.data.CalendarEvent
import com.aistudio.lumacalendar.vtxk.data.EventRepository
import com.aistudio.lumacalendar.vtxk.data.LumaDatabase
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

    init {
        val db = LumaDatabase.getDatabase(application, viewModelScope)
        repository = EventRepository(db.eventDao())
    }

    // Navigation & Tab State (0: Calendar, 1: Agenda, 2: Search, 3: Settings)
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun setCurrentTab(tab: Int) {
        _currentTab.value = tab
    }

    // Calendar Display State
    private val _selectedYear = MutableStateFlow(2026)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(9) // September is 9 (1-indexed)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow("2026-09-11") // Matches prompt: "Friday, September 11"
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

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _calendarPersonalVisible = MutableStateFlow(true)
    val calendarPersonalVisible: StateFlow<Boolean> = _calendarPersonalVisible.asStateFlow()

    private val _calendarWorkVisible = MutableStateFlow(true)
    val calendarWorkVisible: StateFlow<Boolean> = _calendarWorkVisible.asStateFlow()

    private val _calendarHolidaysVisible = MutableStateFlow(true)
    val calendarHolidaysVisible: StateFlow<Boolean> = _calendarHolidaysVisible.asStateFlow()

    private val _themeName = MutableStateFlow("Liquid Glass (Dark)")
    val themeName: StateFlow<String> = _themeName.asStateFlow()

    // Actions
    fun selectDate(dateStr: String) {
        _selectedDate.value = dateStr
        // Synchronize month and year in current calendar type
        val (newYear, newMonth) = CalendarConverter.getYearAndMonth(dateStr, _calendarType.value)
        _selectedYear.value = newYear
        _selectedMonth.value = newMonth
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
    }

    fun goToToday() {
        val today = DateUtils.DEFAULT_TODAY
        _selectedDate.value = today
        val (newYear, newMonth) = CalendarConverter.getYearAndMonth(today, _calendarType.value)
        _selectedYear.value = newYear
        _selectedMonth.value = newMonth
    }

    fun setCalendarType(type: CalendarType) {
        if (_calendarType.value == type) return
        _calendarType.value = type
        // Synchronize month and year to the selected date in the new calendar type!
        val (newYear, newMonth) = CalendarConverter.getYearAndMonth(_selectedDate.value, type)
        _selectedYear.value = newYear
        _selectedMonth.value = newMonth
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
            val event = CalendarEvent(
                id = id,
                title = title.ifBlank { "Untitled Event" },
                date = date,
                startTime = startTime,
                endTime = endTime,
                category = category,
                colorHex = colorHex,
                location = location,
                notes = notes,
                reminderMinutes = reminderMinutes,
                calendarType = calendarType
            )
            if (id == 0L) {
                repository.insertEvent(event)
            } else {
                repository.updateEvent(event)
            }
            closeAddEdit()
        }
    }

    fun deleteEvent(event: CalendarEvent) {
        viewModelScope.launch {
            repository.deleteEvent(event)
            closeEventDetail()
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

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
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
            repository.seedInitialData()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}
