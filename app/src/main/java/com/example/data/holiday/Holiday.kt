package com.example.data.holiday

import com.example.util.CalendarType

enum class HolidayType {
    OFFICIAL_HOLIDAY, // Official public holiday / day off (displayed in RED)
    OBSERVANCE        // Cultural or optional observance
}

data class Holiday(
    val id: String,
    val name: String, // Localized display name, e.g. "نوروز (آغاز سال نو)" or "Independence Day"
    val dateString: String, // Canonical Gregorian date "YYYY-MM-DD" e.g., "2026-03-21"
    val isOfficialHoliday: Boolean = true,
    val type: HolidayType = HolidayType.OFFICIAL_HOLIDAY,
    val calendarType: CalendarType, // Originating calendar system
    val localizedDayDisplay: String = "", // e.g. "۱ فروردین" or "July 4"
    val description: String = ""
)
