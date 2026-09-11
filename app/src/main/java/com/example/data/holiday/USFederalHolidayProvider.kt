package com.example.data.holiday

import com.example.util.CalendarType
import java.util.Calendar
import java.util.Locale

/**
 * Provider for official United States Federal Holidays (5 U.S.C. 6103).
 * Provides exact 2026 dates and algorithmic calculation for any Gregorian year.
 */
class USFederalHolidayProvider {

    /**
     * Calculates the Gregorian date string ("YYYY-MM-DD") for the n-th occurrence
     * of a given day of the week in a month (1-indexed month).
     * e.g., 3rd Monday in January.
     */
    private fun getNthWeekdayOfMonth(year: Int, month: Int, dayOfWeek: Int, n: Int): String {
        val cal = Calendar.getInstance(Locale.US)
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        var count = 0
        var targetDay = 1
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (d in 1..maxDays) {
            cal.set(Calendar.DAY_OF_MONTH, d)
            if (cal.get(Calendar.DAY_OF_WEEK) == dayOfWeek) {
                count++
                if (count == n) {
                    targetDay = d
                    break
                }
            }
        }
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, targetDay)
    }

    /**
     * Calculates the Gregorian date string ("YYYY-MM-DD") for the last occurrence
     * of a given day of the week in a month (e.g., last Monday in May).
     */
    private fun getLastWeekdayOfMonth(year: Int, month: Int, dayOfWeek: Int): String {
        val cal = Calendar.getInstance(Locale.US)
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        var targetDay = maxDays
        for (d in maxDays downTo 1) {
            cal.set(Calendar.DAY_OF_MONTH, d)
            if (cal.get(Calendar.DAY_OF_WEEK) == dayOfWeek) {
                targetDay = d
                break
            }
        }
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, targetDay)
    }

    fun getHolidaysForYear(year: Int): List<Holiday> {
        val holidays = mutableListOf<Holiday>()

        // 1. New Year's Day (January 1)
        holidays.add(
            Holiday(
                id = "us_new_years_$year",
                name = "New Year's Day",
                dateString = String.format(Locale.US, "%04d-01-01", year),
                isOfficialHoliday = true,
                calendarType = CalendarType.GREGORIAN,
                localizedDayDisplay = "Jan 1",
                description = "Official US Federal Holiday"
            )
        )

        // 2. Martin Luther King Jr. Day (Third Monday in January)
        val mlkDate = getNthWeekdayOfMonth(year, 1, Calendar.MONDAY, 3)
        holidays.add(
            Holiday(
                id = "us_mlk_$year",
                name = "Martin Luther King Jr. Day",
                dateString = mlkDate,
                isOfficialHoliday = true,
                calendarType = CalendarType.GREGORIAN,
                localizedDayDisplay = "Third Mon in Jan",
                description = "Official US Federal Holiday"
            )
        )

        // 3. Washington's Birthday / Presidents' Day (Third Monday in February)
        val presDate = getNthWeekdayOfMonth(year, 2, Calendar.MONDAY, 3)
        holidays.add(
            Holiday(
                id = "us_presidents_$year",
                name = "Washington's Birthday",
                dateString = presDate,
                isOfficialHoliday = true,
                calendarType = CalendarType.GREGORIAN,
                localizedDayDisplay = "Third Mon in Feb",
                description = "Official US Federal Holiday"
            )
        )

        // 4. Memorial Day (Last Monday in May)
        val memorialDate = getLastWeekdayOfMonth(year, 5, Calendar.MONDAY)
        holidays.add(
            Holiday(
                id = "us_memorial_$year",
                name = "Memorial Day",
                dateString = memorialDate,
                isOfficialHoliday = true,
                calendarType = CalendarType.GREGORIAN,
                localizedDayDisplay = "Last Mon in May",
                description = "Official US Federal Holiday"
            )
        )

        // 5. Juneteenth National Independence Day (June 19)
        holidays.add(
            Holiday(
                id = "us_juneteenth_$year",
                name = "Juneteenth National Independence Day",
                dateString = String.format(Locale.US, "%04d-06-19", year),
                isOfficialHoliday = true,
                calendarType = CalendarType.GREGORIAN,
                localizedDayDisplay = "Jun 19",
                description = "Official US Federal Holiday"
            )
        )

        // 6. Independence Day (July 4)
        holidays.add(
            Holiday(
                id = "us_independence_$year",
                name = "Independence Day",
                dateString = String.format(Locale.US, "%04d-07-04", year),
                isOfficialHoliday = true,
                calendarType = CalendarType.GREGORIAN,
                localizedDayDisplay = "Jul 4",
                description = "Official US Federal Holiday"
            )
        )

        // 7. Labor Day (First Monday in September)
        val laborDate = getNthWeekdayOfMonth(year, 9, Calendar.MONDAY, 1)
        holidays.add(
            Holiday(
                id = "us_labor_$year",
                name = "Labor Day",
                dateString = laborDate,
                isOfficialHoliday = true,
                calendarType = CalendarType.GREGORIAN,
                localizedDayDisplay = "First Mon in Sep",
                description = "Official US Federal Holiday"
            )
        )

        // 8. Columbus Day (Second Monday in October)
        val columbusDate = getNthWeekdayOfMonth(year, 10, Calendar.MONDAY, 2)
        holidays.add(
            Holiday(
                id = "us_columbus_$year",
                name = "Columbus Day",
                dateString = columbusDate,
                isOfficialHoliday = true,
                calendarType = CalendarType.GREGORIAN,
                localizedDayDisplay = "Second Mon in Oct",
                description = "Official US Federal Holiday"
            )
        )

        // 9. Veterans Day (November 11)
        holidays.add(
            Holiday(
                id = "us_veterans_$year",
                name = "Veterans Day",
                dateString = String.format(Locale.US, "%04d-11-11", year),
                isOfficialHoliday = true,
                calendarType = CalendarType.GREGORIAN,
                localizedDayDisplay = "Nov 11",
                description = "Official US Federal Holiday"
            )
        )

        // 10. Thanksgiving Day (Fourth Thursday in November)
        val thanksgivingDate = getNthWeekdayOfMonth(year, 11, Calendar.THURSDAY, 4)
        holidays.add(
            Holiday(
                id = "us_thanksgiving_$year",
                name = "Thanksgiving Day",
                dateString = thanksgivingDate,
                isOfficialHoliday = true,
                calendarType = CalendarType.GREGORIAN,
                localizedDayDisplay = "Fourth Thu in Nov",
                description = "Official US Federal Holiday"
            )
        )

        // 11. Christmas Day (December 25)
        holidays.add(
            Holiday(
                id = "us_christmas_$year",
                name = "Christmas Day",
                dateString = String.format(Locale.US, "%04d-12-25", year),
                isOfficialHoliday = true,
                calendarType = CalendarType.GREGORIAN,
                localizedDayDisplay = "Dec 25",
                description = "Official US Federal Holiday"
            )
        )

        return holidays.sortedBy { it.dateString }
    }
}
