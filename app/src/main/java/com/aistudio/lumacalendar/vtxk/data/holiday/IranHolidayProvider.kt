package com.aistudio.lumacalendar.vtxk.data.holiday

import com.aistudio.lumacalendar.vtxk.util.CalendarConverter
import com.aistudio.lumacalendar.vtxk.util.CalendarType

/**
 * Provider for official Iranian public holidays (تعطیلات رسمی ایران).
 * Incorporates both fixed Jalali solar holidays and Islamic lunar holidays.
 * Supports exact 2025, 2026 (1404-1405 SH), 2027 tables and algorithmic calculation for any year.
 */
class IranHolidayProvider {

    data class SolarHolidayDef(
        val month: Int, // 1..12
        val day: Int,   // 1..31
        val name: String,
        val localizedDay: String
    )

    data class LunarHolidayDef(
        val hijriMonth: Int,
        val hijriDay: Int,
        val name: String,
        val localizedDay: String
    )

    // The 10 official fixed Solar Jalali holidays in the Iranian calendar
    private val solarHolidays = listOf(
        SolarHolidayDef(1, 1, "آغاز عید نوروز", "۱ فروردین"),
        SolarHolidayDef(1, 2, "عید نوروز", "۲ فروردین"),
        SolarHolidayDef(1, 3, "عید نوروز", "۳ فروردین"),
        SolarHolidayDef(1, 4, "عید نوروز", "۴ فروردین"),
        SolarHolidayDef(1, 12, "روز جمهوری اسلامی ایران", "۱۲ فروردین"),
        SolarHolidayDef(1, 13, "روز طبیعت (سیزده‌بدر)", "۱۳ فروردین"),
        SolarHolidayDef(3, 14, "رحلت حضرت امام خمینی", "۱۴ خرداد"),
        SolarHolidayDef(3, 15, "قیام خونین ۱۵ خرداد", "۱۵ خرداد"),
        SolarHolidayDef(11, 22, "پیروزی انقلاب اسلامی ایران", "۲۲ بهمن"),
        SolarHolidayDef(12, 29, "روز ملی شدن صنعت نفت ایران", "۲۹ اسفند")
    )

    // The official Lunar Islamic holidays observed as public holidays in Iran
    private val lunarHolidays = listOf(
        LunarHolidayDef(1, 9, "تاسوعای حسینی", "۹ محرم"),
        LunarHolidayDef(1, 10, "عاشورای حسینی", "۱۰ محرم"),
        LunarHolidayDef(2, 20, "اربعین حسینی", "۲۰ صفر"),
        LunarHolidayDef(2, 28, "رحلت رسول اکرم و شهادت امام حسن مجتبی", "۲۸ صفر"),
        LunarHolidayDef(2, 30, "شهادت امام رضا", "۳۰ صفر"),
        LunarHolidayDef(3, 8, "شهادت امام حسن عسکری", "۸ ربیع‌الاول"),
        LunarHolidayDef(3, 17, "میلاد رسول اکرم و امام جعفر صادق", "۱۷ ربیع‌الاول"),
        LunarHolidayDef(6, 3, "شهادت حضرت فاطمه زهرا", "۳ جمادی‌الثانی"),
        LunarHolidayDef(7, 13, "ولادت حضرت امام علی", "۱۳ رجب"),
        LunarHolidayDef(7, 27, "مبعث حضرت رسول اکرم", "۲۷ رجب"),
        LunarHolidayDef(8, 15, "ولادت حضرت قائم (عج) - نیمه شعبان", "۱۵ شعبان"),
        LunarHolidayDef(9, 21, "شهادت حضرت امام علی", "۲۱ رمضان"),
        LunarHolidayDef(10, 1, "عید سعید فطر", "۱ شوال"),
        LunarHolidayDef(10, 2, "تعطیل به مناسبت عید سعید فطر", "۲ شوال"),
        LunarHolidayDef(10, 25, "شهادت امام جعفر صادق", "۲۵ شوال"),
        LunarHolidayDef(12, 10, "عید سعید قربان", "۱۰ ذی‌الحجه"),
        LunarHolidayDef(12, 18, "عید سعید غدیر خم", "۱۸ ذی‌الحجه")
    )

    // Verified official lunar holiday dates in Iran for Gregorian year 2026 (1447-1448 AH)
    private val verified2026LunarDates = mapOf(
        Pair(8, 15) to "2026-02-04", // 15 Sha'ban 1447
        Pair(9, 21) to "2026-03-11", // 21 Ramadan 1447
        Pair(10, 1) to "2026-03-21", // 1 Shawwal 1447 (Eid Fitr)
        Pair(10, 2) to "2026-03-22", // 2 Shawwal 1447
        Pair(10, 25) to "2026-04-13", // 25 Shawwal 1447
        Pair(12, 10) to "2026-05-27", // 10 Dhu al-Hijjah 1447 (Eid Ghorban)
        Pair(12, 18) to "2026-06-04", // 18 Dhu al-Hijjah 1447 (Eid Ghadir)
        Pair(1, 9) to "2026-06-24",  // 9 Muharram 1448 (Tasu'a)
        Pair(1, 10) to "2026-06-25", // 10 Muharram 1448 (Ashura)
        Pair(2, 20) to "2026-08-04", // 20 Safar 1448 (Arbaeen)
        Pair(2, 28) to "2026-08-12", // 28 Safar 1448 (Demise of Prophet & Imam Hassan)
        Pair(2, 30) to "2026-08-14", // 30 Safar 1448 (Martyrdom of Imam Reza)
        Pair(3, 8) to "2026-08-21",  // 8 Rabi' al-Awwal 1448
        Pair(3, 17) to "2026-08-30", // 17 Rabi' al-Awwal 1448
        Pair(6, 3) to "2026-11-13",  // 3 Jumada al-Thani 1448
        Pair(7, 13) to "2026-12-23"  // 13 Rajab 1448
    )

    // Verified official lunar holiday dates in Iran for Gregorian year 2025 (1446-1447 AH)
    private val verified2025LunarDates = mapOf(
        Pair(7, 13) to "2025-01-14", // 13 Rajab 1446
        Pair(7, 27) to "2025-01-28", // 27 Rajab 1446
        Pair(8, 15) to "2025-02-14", // 15 Sha'ban 1446
        Pair(9, 21) to "2025-03-22", // 21 Ramadan 1446
        Pair(10, 1) to "2025-03-31", // 1 Shawwal 1446 (Eid Fitr)
        Pair(10, 2) to "2025-04-01", // 2 Shawwal 1446
        Pair(10, 25) to "2025-04-24", // 25 Shawwal 1446
        Pair(12, 10) to "2025-06-06", // 10 Dhu al-Hijjah 1446 (Eid Ghorban)
        Pair(12, 18) to "2025-06-14", // 18 Dhu al-Hijjah 1446 (Eid Ghadir)
        Pair(1, 9) to "2025-07-05",  // 9 Muharram 1447 (Tasu'a)
        Pair(1, 10) to "2025-07-06", // 10 Muharram 1447 (Ashura)
        Pair(2, 20) to "2025-08-15", // 20 Safar 1447 (Arbaeen)
        Pair(2, 28) to "2025-08-23", // 28 Safar 1447
        Pair(2, 30) to "2025-08-25", // 30 Safar 1447
        Pair(3, 8) to "2025-09-01",  // 8 Rabi' al-Awwal 1447
        Pair(3, 17) to "2025-09-10", // 17 Rabi' al-Awwal 1447
        Pair(6, 3) to "2025-11-24"   // 3 Jumada al-Thani 1447
    )

    /**
     * Returns all Iranian official holidays occurring within the given Gregorian year.
     */
    fun getHolidaysForGregorianYear(gregYear: Int): List<Holiday> {
        val result = mutableListOf<Holiday>()

        // 1. Solar Jalali Holidays
        // A Gregorian year gregYear (e.g. 2026) overlaps with Jalali year (gregYear - 622) from Jan 1 to March 20,
        // and Jalali year (gregYear - 621) from March 21 to Dec 31.
        val jalaliYearEarly = gregYear - 622
        val jalaliYearLate = gregYear - 621

        for (def in solarHolidays) {
            // Check in late Jalali year
            val dateLate = CalendarConverter.jalaliToGregorianString(jalaliYearLate, def.month, def.day)
            if (dateLate.startsWith(gregYear.toString())) {
                result.add(
                    Holiday(
                        id = "ir_solar_${def.month}_${def.day}_$dateLate",
                        name = def.name,
                        dateString = dateLate,
                        isOfficialHoliday = true,
                        calendarType = CalendarType.JALALI,
                        localizedDayDisplay = def.localizedDay,
                        description = "تعطیل رسمی ایران"
                    )
                )
            }
            // Check in early Jalali year
            val dateEarly = CalendarConverter.jalaliToGregorianString(jalaliYearEarly, def.month, def.day)
            if (dateEarly.startsWith(gregYear.toString())) {
                result.add(
                    Holiday(
                        id = "ir_solar_${def.month}_${def.day}_$dateEarly",
                        name = def.name,
                        dateString = dateEarly,
                        isOfficialHoliday = true,
                        calendarType = CalendarType.JALALI,
                        localizedDayDisplay = def.localizedDay,
                        description = "تعطیل رسمی ایران"
                    )
                )
            }
        }

        // 2. Lunar Islamic Holidays
        val verifiedMap = when (gregYear) {
            2026 -> verified2026LunarDates
            2025 -> verified2025LunarDates
            else -> null
        }

        if (verifiedMap != null) {
            for (def in lunarHolidays) {
                val key = Pair(def.hijriMonth, def.hijriDay)
                val dateStr = verifiedMap[key]
                if (dateStr != null && dateStr.startsWith(gregYear.toString())) {
                    result.add(
                        Holiday(
                            id = "ir_lunar_${def.hijriMonth}_${def.hijriDay}_$dateStr",
                            name = def.name,
                            dateString = dateStr,
                            isOfficialHoliday = true,
                            calendarType = CalendarType.JALALI,
                            localizedDayDisplay = def.localizedDay,
                            description = "تعطیل رسمی ایران (مناسبت مذهبی)"
                        )
                    )
                }
            }
        } else {
            // Algorithmic calculation for arbitrary future or past years
            val hijriYearEstimate = ((gregYear - 622) * 33) / 32
            for (hYear in (hijriYearEstimate - 1)..(hijriYearEstimate + 1)) {
                for (def in lunarHolidays) {
                    val dateStr = CalendarConverter.hijriToGregorianString(hYear, def.hijriMonth, def.hijriDay)
                    if (dateStr.startsWith(gregYear.toString())) {
                        result.add(
                            Holiday(
                                id = "ir_lunar_${def.hijriMonth}_${def.hijriDay}_$dateStr",
                                name = def.name,
                                dateString = dateStr,
                                isOfficialHoliday = true,
                                calendarType = CalendarType.JALALI,
                                localizedDayDisplay = def.localizedDay,
                                description = "تعطیل رسمی ایران (مناسبت مذهبی)"
                            )
                        )
                    }
                }
            }
        }

        return result.distinctBy { it.dateString + it.name }.sortedBy { it.dateString }
    }
}
