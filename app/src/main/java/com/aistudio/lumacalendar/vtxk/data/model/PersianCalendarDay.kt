package com.aistudio.lumacalendar.vtxk.data.model

/**
 * Domain model representing a single day in the Persian calendar.
 * Completely decouples the UI layer from Retrofit / Moshi DTOs.
 */
data class PersianCalendarDay(
    val date: String, // Gregorian date "YYYY-MM-DD" e.g., "2026-03-21"
    val shamsiDate: String, // Shamsi date "YYYY/MM/DD" e.g., "1405/01/01"
    val isHoliday: Boolean,
    val holidayDescription: String?
)
