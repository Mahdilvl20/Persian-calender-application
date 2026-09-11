package com.aistudio.lumacalendar.vtxk.data.holiday

import com.aistudio.lumacalendar.vtxk.util.CalendarConverter
import com.aistudio.lumacalendar.vtxk.util.CalendarType

interface HolidayRepository {
    fun getHolidaysForYear(year: Int, calendarType: CalendarType): List<Holiday>
    fun getHolidayForDate(dateStr: String, calendarType: CalendarType): Holiday?
    fun isOfficialHoliday(dateStr: String, calendarType: CalendarType): Boolean
}

class HolidayRepositoryImpl(
    private val iranHolidayProvider: IranHolidayProvider,
    private val usFederalHolidayProvider: USFederalHolidayProvider
) : HolidayRepository {

    // In-memory cache by "calendarType_year"
    private val cache = mutableMapOf<String, List<Holiday>>()

    override fun getHolidaysForYear(year: Int, calendarType: CalendarType): List<Holiday> {
        val cacheKey = "${calendarType.name}_$year"
        val cached = cache[cacheKey]
        if (cached != null) return cached

        val holidays = when (calendarType) {
            CalendarType.JALALI -> {
                // When in Jalali, year is a Jalali year (e.g. 1405).
                // 1405 SH begins in Gregorian year 2026.
                val gregYearApprox = year + 621
                // Retrieve holidays covering both overlapping Gregorian years
                val list1 = iranHolidayProvider.getHolidaysForGregorianYear(gregYearApprox)
                val list2 = iranHolidayProvider.getHolidaysForGregorianYear(gregYearApprox + 1)
                (list1 + list2).filter { h ->
                    val j = CalendarConverter.gregorianToJalali(h.dateString)
                    j.year == year
                }.distinctBy { it.dateString + it.name }
            }
            CalendarType.GREGORIAN -> {
                usFederalHolidayProvider.getHolidaysForYear(year)
            }
            CalendarType.HIJRI -> {
                // Islamic / Iranian lunar and solar holidays for the respective Gregorian timeframe
                val gregYearApprox = ((year * 32) / 33) + 622
                val list1 = iranHolidayProvider.getHolidaysForGregorianYear(gregYearApprox)
                val list2 = iranHolidayProvider.getHolidaysForGregorianYear(gregYearApprox + 1)
                (list1 + list2).filter { h ->
                    val hij = CalendarConverter.gregorianToHijri(h.dateString)
                    hij.year == year
                }.distinctBy { it.dateString + it.name }
            }
        }

        cache[cacheKey] = holidays
        return holidays
    }

    override fun getHolidayForDate(dateStr: String, calendarType: CalendarType): Holiday? {
        val g = CalendarConverter.parseGregorianString(dateStr)
        val holidays = when (calendarType) {
            CalendarType.JALALI, CalendarType.HIJRI -> iranHolidayProvider.getHolidaysForGregorianYear(g.year)
            CalendarType.GREGORIAN -> usFederalHolidayProvider.getHolidaysForYear(g.year)
        }
        return holidays.firstOrNull { it.dateString == dateStr && it.isOfficialHoliday }
            ?: holidays.firstOrNull { it.dateString == dateStr }
    }

    override fun isOfficialHoliday(dateStr: String, calendarType: CalendarType): Boolean {
        val holiday = getHolidayForDate(dateStr, calendarType)
        return holiday?.isOfficialHoliday == true
    }
}
