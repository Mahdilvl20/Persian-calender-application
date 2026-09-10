package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CalendarDay(
    val dateString: String, // "YYYY-MM-DD"
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean,
    val displayNumber: String = dayOfMonth.toString()
)

data class WeekDayInfo(
    val dateString: String,
    val dayOfWeekName: String, // "Mon", "Tue", "ش", "ی"
    val dayOfMonth: Int,
    val isToday: Boolean,
    val isSelected: Boolean,
    val displayNumber: String = dayOfMonth.toString()
)

object DateUtils {
    // Current simulated base date matching user prompt & context (September 11, 2026 / Sep 10, 2026)
    val DEFAULT_TODAY: String = "2026-09-11"

    private val ymdFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
    private val monthOnlyFormat = SimpleDateFormat("MMMM", Locale.US)
    private val yearOnlyFormat = SimpleDateFormat("yyyy", Locale.US)
    private val fullDateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.US)
    private val shortDayFormat = SimpleDateFormat("EEE", Locale.US)
    private val dayOfWeekFormat = SimpleDateFormat("EEEE", Locale.US)
    private val monthDayFormat = SimpleDateFormat("MMMM d", Locale.US)

    fun parseDate(dateStr: String): Date {
        return try {
            ymdFormat.parse(dateStr) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }

    fun formatDate(date: Date): String = ymdFormat.format(date)

    fun getMonthName(year: Int, month: Int, calendarType: CalendarType = CalendarType.GREGORIAN): String {
        return CalendarConverter.getMonthName(month, calendarType)
    }

    fun getMonthSecondaryName(year: Int, month: Int, calendarType: CalendarType = CalendarType.GREGORIAN): String {
        return CalendarConverter.getMonthSecondaryName(month, calendarType)
    }

    fun getYear(year: Int, month: Int, calendarType: CalendarType = CalendarType.GREGORIAN): String {
        return CalendarConverter.formatYearString(year, calendarType)
    }

    fun formatSelectedHeader(
        dateStr: String,
        todayStr: String = DEFAULT_TODAY,
        calendarType: CalendarType = CalendarType.GREGORIAN
    ): String {
        return CalendarConverter.formatHeaderDate(dateStr, calendarType, todayStr)
    }

    fun formatDisplayDate(dateStr: String, calendarType: CalendarType = CalendarType.GREGORIAN): String {
        return CalendarConverter.formatDisplayDate(dateStr, calendarType)
    }

    fun formatMonthDay(dateStr: String, calendarType: CalendarType = CalendarType.GREGORIAN): String {
        return CalendarConverter.formatMonthDay(dateStr, calendarType)
    }

    fun getDayOfWeek(dateStr: String, calendarType: CalendarType = CalendarType.GREGORIAN): String {
        return CalendarConverter.getWeekdayName(dateStr, calendarType)
    }

    fun getWeekdayLabels(calendarType: CalendarType, firstDayMonday: Boolean): List<String> {
        return CalendarConverter.getWeekdayLabels(calendarType, firstDayMonday)
    }

    /**
     * Generates a 42-day (6x7) grid for a given year and month (1-indexed).
     * Returns list of CalendarDay items with accurate day numbers, localized numerals, current month flags.
     */
    fun getMonthDays(
        year: Int,
        month: Int,
        selectedDate: String,
        todayDate: String = DEFAULT_TODAY,
        calendarType: CalendarType = CalendarType.GREGORIAN,
        firstDaySunday: Boolean = true
    ): List<CalendarDay> {
        val daysData = CalendarConverter.getMonthDays(
            year = year,
            month = month,
            selectedDate = selectedDate,
            todayDate = todayDate,
            calendarType = calendarType,
            firstDayMonday = !firstDaySunday
        )
        return daysData.map {
            CalendarDay(
                dateString = it.dateString,
                dayOfMonth = it.dayNumber,
                isCurrentMonth = it.isCurrentMonth,
                isToday = it.isToday,
                isSelected = it.isSelected,
                displayNumber = it.displayNumber
            )
        }
    }

    /**
     * Generates 7 days of the week containing the given date.
     */
    fun getWeekDays(
        targetDateStr: String,
        selectedDate: String,
        todayDate: String = DEFAULT_TODAY,
        calendarType: CalendarType = CalendarType.GREGORIAN,
        firstDaySunday: Boolean = false
    ): List<WeekDayInfo> {
        val weekData = CalendarConverter.getWeekDays(
            selectedDate = targetDateStr,
            todayDate = todayDate,
            calendarType = calendarType,
            firstDayMonday = !firstDaySunday
        )
        return weekData.map {
            WeekDayInfo(
                dateString = it.dateString,
                dayOfWeekName = it.dayOfWeekName,
                dayOfMonth = it.dayNumberString.toIntOrNull() ?: 1,
                isToday = it.isToday,
                isSelected = it.isSelected,
                displayNumber = it.dayNumberString
            )
        }
    }

    fun addMonths(year: Int, month: Int, delta: Int, calendarType: CalendarType = CalendarType.GREGORIAN): Pair<Int, Int> {
        return CalendarConverter.addMonths(year, month, delta, calendarType)
    }

    fun addDays(dateStr: String, delta: Int): String {
        val cal = Calendar.getInstance()
        cal.time = parseDate(dateStr)
        cal.add(Calendar.DAY_OF_MONTH, delta)
        return ymdFormat.format(cal.time)
    }
}
