package com.aistudio.lumacalendar.vtxk.util

import com.aistudio.lumacalendar.vtxk.data.holiday.HolidayService
import com.aistudio.lumacalendar.vtxk.data.model.PersianCalendarDay
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class CalendarDay(
    val dateString: String, // "YYYY-MM-DD"
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean,
    val displayNumber: String = dayOfMonth.toString(),
    val isHoliday: Boolean = false,
    val holidayName: String? = null
)

data class WeekDayInfo(
    val dateString: String,
    val dayOfWeekName: String, // "Mon", "Tue", "ش", "ی"
    val dayOfMonth: Int,
    val isToday: Boolean,
    val isSelected: Boolean,
    val displayNumber: String = dayOfMonth.toString(),
    val isHoliday: Boolean = false,
    val holidayName: String? = null
)

object DateUtils {
    /**
     * Resolves the device's local timezone reliably.
     */
    fun getDeviceZoneId(): ZoneId {
        return try {
            TimeZone.getDefault().toZoneId()
        } catch (_: Exception) {
            ZoneId.systemDefault()
        }
    }

    /**
     * Obtains today's date in the device's actual local timezone.
     * Never relies on UTC to determine 'today'.
     */
    fun getRealDeviceLocalDate(): LocalDate {
        return try {
            LocalDate.now(getDeviceZoneId())
        } catch (_: Exception) {
            val cal = Calendar.getInstance(TimeZone.getDefault())
            LocalDate.of(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
        }
    }

    /**
     * Returns today's date in canonical ISO format "YYYY-MM-DD" based on local device time.
     */
    fun getRealDeviceDate(): String {
        return try {
            val now = getRealDeviceLocalDate()
            String.format(Locale.US, "%04d-%02d-%02d", now.year, now.monthValue, now.dayOfMonth)
        } catch (_: Exception) {
            val cal = Calendar.getInstance(TimeZone.getDefault())
            String.format(
                Locale.US,
                "%04d-%02d-%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
        }
    }

    /** Dynamic real device date */
    val DEFAULT_TODAY: String
        get() = getRealDeviceDate()

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
        todayStr: String = getRealDeviceDate(),
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
     * Returns list of CalendarDay items with accurate day numbers, localized numerals, current month flags and holiday info.
     */
    fun getMonthDays(
        year: Int,
        month: Int,
        selectedDate: String,
        todayDate: String = getRealDeviceDate(),
        calendarType: CalendarType = CalendarType.GREGORIAN,
        firstDaySunday: Boolean = true,
        holidayService: HolidayService? = HolidayService.default,
        persianDaysMap: Map<String, PersianCalendarDay>? = null
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
            val isJalali = calendarType == CalendarType.JALALI
            val pDay = if (isJalali) persianDaysMap?.get(it.dateString) else null
            val holiday = holidayService?.getHoliday(it.dateString, calendarType)
            val isHoliday = if (isJalali && pDay != null) {
                pDay.isHoliday
            } else {
                holiday?.isOfficialHoliday == true
            }
            val holidayName = if (isJalali && pDay != null) {
                pDay.holidayDescription ?: holiday?.name
            } else {
                holiday?.name
            }

            CalendarDay(
                dateString = it.dateString,
                dayOfMonth = it.dayNumber,
                isCurrentMonth = it.isCurrentMonth,
                isToday = it.isToday,
                isSelected = it.isSelected,
                displayNumber = it.displayNumber,
                isHoliday = isHoliday,
                holidayName = holidayName
            )
        }
    }

    /**
     * Generates 7 days of the week containing the given date.
     */
    fun getWeekDays(
        targetDateStr: String,
        selectedDate: String,
        todayDate: String = getRealDeviceDate(),
        calendarType: CalendarType = CalendarType.GREGORIAN,
        firstDaySunday: Boolean = false,
        holidayService: HolidayService? = HolidayService.default,
        persianDaysMap: Map<String, PersianCalendarDay>? = null
    ): List<WeekDayInfo> {
        val weekData = CalendarConverter.getWeekDays(
            selectedDate = targetDateStr,
            todayDate = todayDate,
            calendarType = calendarType,
            firstDayMonday = !firstDaySunday
        )
        return weekData.map {
            val isJalali = calendarType == CalendarType.JALALI
            val pDay = if (isJalali) persianDaysMap?.get(it.dateString) else null
            val holiday = holidayService?.getHoliday(it.dateString, calendarType)
            val isHoliday = if (isJalali && pDay != null) {
                pDay.isHoliday
            } else {
                holiday?.isOfficialHoliday == true
            }
            val holidayName = if (isJalali && pDay != null) {
                pDay.holidayDescription ?: holiday?.name
            } else {
                holiday?.name
            }

            WeekDayInfo(
                dateString = it.dateString,
                dayOfWeekName = it.dayOfWeekName,
                dayOfMonth = it.dayNumberString.toIntOrNull() ?: 1,
                isToday = it.isToday,
                isSelected = it.isSelected,
                displayNumber = it.dayNumberString,
                isHoliday = isHoliday,
                holidayName = holidayName
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

    fun getWeekOfYear(dateStr: String, firstDayMonday: Boolean): Int {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = if (firstDayMonday) Calendar.MONDAY else Calendar.SUNDAY
        cal.minimalDaysInFirstWeek = 4
        cal.time = parseDate(dateStr)
        return cal.get(Calendar.WEEK_OF_YEAR)
    }
}
