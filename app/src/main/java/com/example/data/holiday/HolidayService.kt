package com.example.data.holiday

import com.example.data.CalendarEvent
import com.example.util.AppStrings
import com.example.util.CalendarConverter
import com.example.util.CalendarType

/**
 * High-level service providing holiday inquiries and conversions for the application UI.
 */
class HolidayService(private val repository: HolidayRepository) {

    fun getHoliday(dateStr: String, calendarType: CalendarType): Holiday? {
        return repository.getHolidayForDate(dateStr, calendarType)
    }

    fun isHoliday(dateStr: String, calendarType: CalendarType): Boolean {
        return repository.isOfficialHoliday(dateStr, calendarType)
    }

    fun getHolidaysForDate(dateStr: String, calendarType: CalendarType): List<Holiday> {
        val h = repository.getHolidayForDate(dateStr, calendarType)
        return if (h != null) listOf(h) else emptyList()
    }

    fun getHolidaysForYear(year: Int, calendarType: CalendarType): List<Holiday> {
        return repository.getHolidaysForYear(year, calendarType)
    }

    fun getHolidaysForMonth(year: Int, month: Int, calendarType: CalendarType): List<Holiday> {
        val yearHolidays = repository.getHolidaysForYear(year, calendarType)
        return yearHolidays.filter { h ->
            val (y, m) = CalendarConverter.getYearAndMonth(h.dateString, calendarType)
            y == year && m == month
        }
    }

    /**
     * Converts a Holiday into a CalendarEvent representation for seamless display
     * in the Agenda view, Search results, and day event lists.
     */
    fun toCalendarEvent(holiday: Holiday, strings: AppStrings): CalendarEvent {
        return CalendarEvent(
            id = -kotlin.math.abs(holiday.id.hashCode().toLong()), // Negative ID indicates system holiday
            title = holiday.name,
            date = holiday.dateString,
            startTime = "00:00",
            endTime = "23:59",
            category = "Special",
            colorHex = "#FF453A", // Apple/Liquid Glass vibrant holiday red
            location = if (holiday.calendarType == CalendarType.GREGORIAN) "United States" else "ایران",
            notes = holiday.description.ifEmpty {
                if (holiday.calendarType == CalendarType.GREGORIAN) "Official US Federal Holiday" else "تعطیل رسمی"
            },
            reminderMinutes = 0,
            calendarType = "Holidays"
        )
    }

    companion object {
        val default: HolidayService by lazy {
            val iranProvider = IranHolidayProvider()
            val usProvider = USFederalHolidayProvider()
            val repo = HolidayRepositoryImpl(iranProvider, usProvider)
            HolidayService(repo)
        }
    }
}
