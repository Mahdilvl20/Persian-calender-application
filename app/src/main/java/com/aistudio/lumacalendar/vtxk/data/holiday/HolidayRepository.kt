package com.aistudio.lumacalendar.vtxk.data.holiday

import com.aistudio.lumacalendar.vtxk.data.repository.PersianCalendarRepository
import com.aistudio.lumacalendar.vtxk.util.CalendarConverter
import com.aistudio.lumacalendar.vtxk.util.CalendarType

interface HolidayRepository {
    fun getHolidaysForYear(year: Int, calendarType: CalendarType): List<Holiday>
    fun getHolidayForDate(dateStr: String, calendarType: CalendarType): Holiday?
    fun isOfficialHoliday(dateStr: String, calendarType: CalendarType): Boolean
}

class HolidayRepositoryImpl(
    private val iranHolidayProvider: IranHolidayProvider,
    private val usFederalHolidayProvider: USFederalHolidayProvider,
    var persianCalendarRepository: PersianCalendarRepository? = null
) : HolidayRepository {

    // In-memory cache by "calendarType_year"
    private val cache = mutableMapOf<String, List<Holiday>>()

    fun updatePersianCalendarRepository(repo: PersianCalendarRepository) {
        this.persianCalendarRepository = repo
        // Invalidate Jalali cached items to immediately reflect authoritative API data
        val keysToRemove = cache.keys.filter { it.startsWith(CalendarType.JALALI.name) }
        keysToRemove.forEach { cache.remove(it) }
    }

    override fun getHolidaysForYear(year: Int, calendarType: CalendarType): List<Holiday> {
        val cacheKey = "${calendarType.name}_$year"
        val cached = cache[cacheKey]
        if (cached != null) return cached

        val holidays = when (calendarType) {
            CalendarType.JALALI -> {
                // First priority: Use Persian Calendar API data from PersianCalendarRepository
                val apiDays = persianCalendarRepository?.getCachedDaysForYear(year)
                if (!apiDays.isNullOrEmpty()) {
                    apiDays.filter { it.isHoliday }.map { day ->
                        Holiday(
                            id = "jalali_${day.date}",
                            name = day.holidayDescription ?: "تعطیل رسمی",
                            dateString = day.date,
                            isOfficialHoliday = true,
                            calendarType = CalendarType.JALALI,
                            description = day.holidayDescription ?: "تعطیل رسمی"
                        )
                    }
                } else {
                    // Fallback to Iran holiday provider covering overlapping Gregorian years
                    val gregYearApprox = year + 621
                    val list1 = iranHolidayProvider.getHolidaysForGregorianYear(gregYearApprox)
                    val list2 = iranHolidayProvider.getHolidaysForGregorianYear(gregYearApprox + 1)
                    (list1 + list2).filter { h ->
                        val j = CalendarConverter.gregorianToJalali(h.dateString)
                        j.year == year
                    }.distinctBy { it.dateString + it.name }
                }
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
        if (calendarType == CalendarType.JALALI) {
            val (jYear, _) = CalendarConverter.getYearAndMonth(dateStr, CalendarType.JALALI)
            val apiDays = persianCalendarRepository?.getCachedDaysForYear(jYear)
            val day = apiDays?.firstOrNull { it.date == dateStr }
            if (day != null) {
                return if (day.isHoliday) {
                    Holiday(
                        id = "jalali_${day.date}",
                        name = day.holidayDescription ?: "تعطیل رسمی",
                        dateString = day.date,
                        isOfficialHoliday = true,
                        calendarType = CalendarType.JALALI,
                        description = day.holidayDescription ?: "تعطیل رسمی"
                    )
                } else {
                    null
                }
            }
        }

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
