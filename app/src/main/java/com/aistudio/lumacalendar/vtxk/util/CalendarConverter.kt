package com.aistudio.lumacalendar.vtxk.util

/**
 * Supported calendar types for Luma Calendar.
 */
enum class CalendarType(
    val farsiArabicName: String,
    val englishName: String,
    val shortLabel: String
) {
    JALALI(farsiArabicName = "شمسی", englishName = "Jalali", shortLabel = "شمسی"),
    GREGORIAN(farsiArabicName = "میلادی", englishName = "Gregorian", shortLabel = "میلادی"),
    HIJRI(farsiArabicName = "قمری", englishName = "Hijri", shortLabel = "قمری")
}

data class JalaliDate(val year: Int, val month: Int, val day: Int)
data class GregorianDate(val year: Int, val month: Int, val day: Int)
data class HijriDate(val year: Int, val month: Int, val day: Int)

/**
 * Universal Calendar conversion and calculation utility for Gregorian, Jalali, and Hijri.
 * Based on exact Julian Day Number (JDN) algorithms.
 */
object CalendarConverter {

    // --- Digit localization helpers ---
    fun toPersianDigits(text: String): String {
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val sb = StringBuilder(text.length)
        for (c in text) {
            if (c in '0'..'9') {
                sb.append(persianDigits[c - '0'])
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    fun toArabicDigits(text: String): String {
        val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        val sb = StringBuilder(text.length)
        for (c in text) {
            if (c in '0'..'9') {
                sb.append(arabicDigits[c - '0'])
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    // --- Julian Day Number (JDN) conversions ---

    /**
     * Converts Gregorian (year, month 1..12, day 1..31) to Julian Day Number (JDN).
     */
    fun gregorianToJdn(year: Int, month: Int, day: Int): Long {
        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        return day + (153 * m + 2) / 5 + 365L * y + y / 4 - y / 100 + y / 400 - 32045
    }

    /**
     * Converts Julian Day Number (JDN) to Gregorian (year, month 1..12, day 1..31).
     */
    fun jdnToGregorian(jdn: Long): GregorianDate {
        val a = jdn + 32044
        val b = (4 * a + 3) / 146097
        val c = a - (146097 * b) / 4
        val d = (4 * c + 3) / 1461
        val e = c - (1461 * d) / 4
        val m = (5 * e + 2) / 153
        val day = (e - (153 * m + 2) / 5 + 1).toInt()
        val month = (m + 3 - 12 * (m / 10)).toInt()
        val year = (100 * b + d - 4800 + m / 10).toInt()
        return GregorianDate(year, month, day)
    }

    /**
     * Converts Jalali (year, month 1..12, day 1..31) to Julian Day Number (JDN).
     * Uses the astronomical / Borkowski algorithm for the 2820-year cycle.
     */
    fun jalaliToJdn(year: Int, month: Int, day: Int): Long {
        val epbase = year - if (year >= 0) 474 else 473
        val epcalc = 474 + (epbase % 2820 + 2820) % 2820
        val md = if (month <= 7) (month - 1) * 31 else (month - 1) * 30 + 6
        return day + md + ((epcalc * 682) - 110) / 2816 + (epcalc - 1) * 365L + (epbase / 2820) * 1029983L + 1948320
    }

    /**
     * Converts Julian Day Number (JDN) to Jalali (year, month 1..12, day 1..31).
     */
    fun jdnToJalali(jdn: Long): JalaliDate {
        val depoch = jdn - 2121446L
        val cycle = Math.floorDiv(depoch, 1029983L)
        val cday = Math.floorMod(depoch, 1029983L)
        val ycycle: Long
        if (cday == 1029982L) {
            ycycle = 2820
        } else {
            val aux1 = cday / 366L
            val aux2 = cday % 366L
            ycycle = ((2134 * aux1 + 2816 * aux2 + 2815) / 1028522L) + aux1 + 1
        }
        var year = (ycycle + 2820 * cycle + 474).toInt()
        if (year <= 0) year--
        val jdn1 = jalaliToJdn(year, 1, 1)
        val dayseq = (jdn - jdn1).toInt()
        val month = if (dayseq < 186) {
            dayseq / 31 + 1
        } else {
            (dayseq - 186) / 30 + 7
        }
        val day = (jdn - jalaliToJdn(year, month, 1) + 1).toInt()
        return JalaliDate(year, month.coerceIn(1, 12), day.coerceIn(1, 31))
    }

    /**
     * Converts Hijri (Islamic lunar: year, month 1..12, day 1..30) to JDN.
     * Uses standard Arithmetical/Tabular civil Islamic calendar (epoch JDN 1948440).
     */
    fun hijriToJdn(year: Int, month: Int, day: Int): Long {
        val y = year - 1
        val daysBeforeThisYear = y * 354L + (11 * y + 14) / 30
        val daysBeforeThisMonth = ((month - 1) * 59 + 1) / 2
        return day + daysBeforeThisMonth + daysBeforeThisYear + 1948439L
    }

    /**
     * Converts JDN to Hijri (year, month 1..12, day 1..30).
     */
    fun jdnToHijri(jdn: Long): HijriDate {
        val daysSinceEpoch = jdn - 1948440L
        val cycles = Math.floorDiv(daysSinceEpoch, 10631L)
        val dayInCycle = Math.floorMod(daysSinceEpoch, 10631L)

        val yearInCycle = ((30 * dayInCycle + 15) / 10631).toInt()
        val daysBeforeYearInCycle = yearInCycle * 354 + (11 * yearInCycle + 14) / 30
        var dayInYear = (dayInCycle - daysBeforeYearInCycle).toInt()

        var year = (cycles * 30 + yearInCycle + 1).toInt()
        if (dayInYear < 0) {
            year--
            val y = year - 1
            val daysBefore = y * 354L + (11 * y + 14) / 30
            dayInYear = (jdn - (daysBefore + 1948440L)).toInt()
        }

        var month = 1
        while (month < 12) {
            val daysInM = if (month % 2 == 1) 30 else 29
            if (dayInYear < daysInM) break
            dayInYear -= daysInM
            month++
        }
        val day = dayInYear + 1
        return HijriDate(year, month.coerceIn(1, 12), day.coerceIn(1, 30))
    }

    // --- Direct conversions between Gregorian string "YYYY-MM-DD" and specific systems ---

    fun parseGregorianString(dateStr: String): GregorianDate {
        return try {
            val normalized = DateValidator.normalizeDigits(dateStr)
            val parts = normalized.split('-', '/', '.', ' ').filter { it.isNotBlank() }
            if (parts.size >= 3) {
                val y = parts[0].toIntOrNull() ?: 2026
                val m = (parts[1].toIntOrNull() ?: 9).coerceIn(1, 12)
                val maxDay = getDaysInGregorianMonth(y, m)
                val d = (parts[2].toIntOrNull() ?: 11).coerceIn(1, maxDay)
                GregorianDate(y, m, d)
            } else if (normalized.length == 8 && normalized.all { it.isDigit() }) {
                val y = normalized.substring(0, 4).toIntOrNull() ?: 2026
                val m = (normalized.substring(4, 6).toIntOrNull() ?: 9).coerceIn(1, 12)
                val maxDay = getDaysInGregorianMonth(y, m)
                val d = (normalized.substring(6, 8).toIntOrNull() ?: 11).coerceIn(1, maxDay)
                GregorianDate(y, m, d)
            } else {
                GregorianDate(2026, 9, 11)
            }
        } catch (e: Exception) {
            GregorianDate(2026, 9, 11)
        }
    }

    fun formatGregorianString(date: GregorianDate): String {
        val y = date.year
        val m = date.month.coerceIn(1, 12)
        val d = date.day.coerceIn(1, 31)
        return String.format(java.util.Locale.US, "%04d-%02d-%02d", y, m, d)
    }

    fun gregorianToJalali(dateStr: String): JalaliDate {
        return try {
            val g = parseGregorianString(dateStr)
            val jdn = gregorianToJdn(g.year, g.month, g.day)
            jdnToJalali(jdn)
        } catch (e: Exception) {
            JalaliDate(1405, 6, 21)
        }
    }

    fun jalaliToGregorianString(year: Int, month: Int, day: Int): String {
        return try {
            val safeMonth = month.coerceIn(1, 12)
            val safeDay = day.coerceIn(1, 31)
            val jdn = jalaliToJdn(year, safeMonth, safeDay)
            val g = jdnToGregorian(jdn)
            formatGregorianString(g)
        } catch (e: Exception) {
            "2026-09-11"
        }
    }

    fun gregorianToHijri(dateStr: String): HijriDate {
        return try {
            val g = parseGregorianString(dateStr)
            val jdn = gregorianToJdn(g.year, g.month, g.day)
            jdnToHijri(jdn)
        } catch (e: Exception) {
            HijriDate(1448, 3, 28)
        }
    }

    fun hijriToGregorianString(year: Int, month: Int, day: Int): String {
        return try {
            val safeMonth = month.coerceIn(1, 12)
            val safeDay = day.coerceIn(1, 30)
            val jdn = hijriToJdn(year, safeMonth, safeDay)
            val g = jdnToGregorian(jdn)
            formatGregorianString(g)
        } catch (e: Exception) {
            "2026-09-11"
        }
    }

    // --- Days in Month calculations ---

    fun isGregorianLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    fun getDaysInGregorianMonth(year: Int, month: Int): Int {
        return when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isGregorianLeapYear(year)) 29 else 28
            else -> 30
        }
    }

    fun isJalaliLeapYear(year: Int): Boolean {
        val epbase = year - if (year >= 0) 474 else 473
        val epcalc = 474 + (epbase % 2820 + 2820) % 2820
        return ((epcalc * 682) - 110) % 2816 < 682
    }

    fun getDaysInJalaliMonth(year: Int, month: Int): Int {
        return when {
            month in 1..6 -> 31
            month in 7..11 -> 30
            month == 12 -> if (isJalaliLeapYear(year)) 30 else 29
            else -> 30
        }
    }

    fun isHijriLeapYear(year: Int): Boolean {
        return ((11 * year + 14) % 30) < 11
    }

    fun getDaysInHijriMonth(year: Int, month: Int): Int {
        return when {
            month in listOf(1, 3, 5, 7, 9, 11) -> 30
            month in listOf(2, 4, 6, 8, 10) -> 29
            month == 12 -> if (isHijriLeapYear(year)) 30 else 29
            else -> 29
        }
    }

    fun getDaysInMonth(year: Int, month: Int, calendarType: CalendarType): Int {
        return when (calendarType) {
            CalendarType.GREGORIAN -> getDaysInGregorianMonth(year, month)
            CalendarType.JALALI -> getDaysInJalaliMonth(year, month)
            CalendarType.HIJRI -> getDaysInHijriMonth(year, month)
        }
    }

    // --- Month Names ---

    val GREGORIAN_MONTHS = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val JALALI_MONTHS = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    val JALALI_MONTHS_EN = listOf(
        "Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar",
        "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand"
    )

    val HIJRI_MONTHS = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الثاني", "جمادى الأولى", "جمادى الآخرة",
        "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    val HIJRI_MONTHS_EN = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani", "Jumada al-Ula", "Jumada al-Akhirah",
        "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    fun getMonthName(month: Int, calendarType: CalendarType): String {
        val idx = (month - 1).coerceIn(0, 11)
        return when (calendarType) {
            CalendarType.GREGORIAN -> GREGORIAN_MONTHS[idx]
            CalendarType.JALALI -> JALALI_MONTHS[idx]
            CalendarType.HIJRI -> HIJRI_MONTHS[idx]
        }
    }

    fun getMonthSecondaryName(month: Int, calendarType: CalendarType): String {
        val idx = (month - 1).coerceIn(0, 11)
        return when (calendarType) {
            CalendarType.GREGORIAN -> ""
            CalendarType.JALALI -> JALALI_MONTHS_EN[idx]
            CalendarType.HIJRI -> HIJRI_MONTHS_EN[idx]
        }
    }

    // --- Weekday Names & Offsets ---
    // In JDN, (jdn + 1) % 7 gives:
    // 0 = Sunday, 1 = Monday, 2 = Tuesday, 3 = Wednesday, 4 = Thursday, 5 = Friday, 6 = Saturday

    fun getDayOfWeekIndex(dateStr: String): Int {
        return try {
            val g = parseGregorianString(dateStr)
            val jdn = gregorianToJdn(g.year, g.month, g.day)
            (((jdn + 1) % 7 + 7) % 7).toInt().coerceIn(0, 6) // 0 = Sun .. 6 = Sat
        } catch (e: Exception) {
            5 // Friday safe fallback
        }
    }

    val GREGORIAN_WEEKDAYS_SUN_FIRST = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val GREGORIAN_WEEKDAYS_MON_FIRST = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    // Jalali week starts on Saturday (شنبه)
    val JALALI_WEEKDAYS_SHORT = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
    val JALALI_WEEKDAYS_FULL = listOf(
        "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه"
    )

    // Hijri week (Sunday to Saturday) with Persian names
    val HIJRI_WEEKDAYS_SHORT = listOf("ی", "د", "س", "چ", "پ", "ج", "ش")
    val HIJRI_WEEKDAYS_FULL = listOf(
        "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه", "شنبه"
    )

    fun getWeekdayLabels(calendarType: CalendarType, firstDayMonday: Boolean): List<String> {
        return when (calendarType) {
            CalendarType.GREGORIAN -> if (firstDayMonday) GREGORIAN_WEEKDAYS_MON_FIRST else GREGORIAN_WEEKDAYS_SUN_FIRST
            CalendarType.JALALI -> JALALI_WEEKDAYS_SHORT
            CalendarType.HIJRI -> HIJRI_WEEKDAYS_SHORT
        }
    }

    fun getWeekdayName(dateStr: String, calendarType: CalendarType): String {
        val dow = getDayOfWeekIndex(dateStr).coerceIn(0, 6) // 0 = Sun, 1 = Mon, 2 = Tue, 3 = Wed, 4 = Thu, 5 = Fri, 6 = Sat
        return when (calendarType) {
            CalendarType.GREGORIAN -> {
                val full = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
                full.getOrElse(dow) { "Friday" }
            }
            CalendarType.JALALI -> {
                // In Jalali: Saturday is index 0 in JALALI_WEEKDAYS_FULL
                // dow: 6 (Sat) -> 0; 0 (Sun) -> 1; 1 (Mon) -> 2 ...
                val jalaliDow = (((dow + 1) % 7 + 7) % 7).coerceIn(0, 6)
                JALALI_WEEKDAYS_FULL.getOrElse(jalaliDow) { "جمعه" }
            }
            CalendarType.HIJRI -> {
                HIJRI_WEEKDAYS_FULL.getOrElse(dow) { "الجمعة" }
            }
        }
    }

    // --- Full Date Formatting ---

    fun formatDisplayDate(dateStr: String, calendarType: CalendarType): String {
        val weekday = getWeekdayName(dateStr, calendarType)
        return when (calendarType) {
            CalendarType.GREGORIAN -> {
                val g = parseGregorianString(dateStr)
                "$weekday, ${getMonthName(g.month, CalendarType.GREGORIAN)} ${g.day}, ${g.year}"
            }
            CalendarType.JALALI -> {
                val j = gregorianToJalali(dateStr)
                val dayStr = toPersianDigits(j.day.toString())
                val yearStr = toPersianDigits(j.year.toString())
                "$weekday، $dayStr ${getMonthName(j.month, CalendarType.JALALI)} $yearStr"
            }
            CalendarType.HIJRI -> {
                val h = gregorianToHijri(dateStr)
                val dayStr = toPersianDigits(h.day.toString())
                val yearStr = toPersianDigits(h.year.toString())
                "$weekday، $dayStr ${getMonthName(h.month, CalendarType.HIJRI)} $yearStr"
            }
        }
    }

    fun formatHeaderDate(dateStr: String, calendarType: CalendarType, todayDate: String): String {
        val isToday = dateStr == todayDate
        val full = formatDisplayDate(dateStr, calendarType)
        val prefix = when {
            isToday && (calendarType == CalendarType.JALALI || calendarType == CalendarType.HIJRI) -> "امروز • "
            isToday -> "Today • "
            else -> ""
        }
        return "$prefix$full"
    }

    fun formatMonthDay(dateStr: String, calendarType: CalendarType): String {
        return when (calendarType) {
            CalendarType.GREGORIAN -> {
                val g = parseGregorianString(dateStr)
                "${getMonthName(g.month, CalendarType.GREGORIAN)} ${g.day}"
            }
            CalendarType.JALALI -> {
                val j = gregorianToJalali(dateStr)
                "${toPersianDigits(j.day.toString())} ${getMonthName(j.month, CalendarType.JALALI)}"
            }
            CalendarType.HIJRI -> {
                val h = gregorianToHijri(dateStr)
                "${toPersianDigits(h.day.toString())} ${getMonthName(h.month, CalendarType.HIJRI)}"
            }
        }
    }

    fun formatYearString(year: Int, calendarType: CalendarType): String {
        return when (calendarType) {
            CalendarType.GREGORIAN -> year.toString()
            CalendarType.JALALI, CalendarType.HIJRI -> toPersianDigits(year.toString())
        }
    }

    fun formatDayNumberString(dayNumber: Int, calendarType: CalendarType): String {
        return when (calendarType) {
            CalendarType.GREGORIAN -> dayNumber.toString()
            CalendarType.JALALI, CalendarType.HIJRI -> toPersianDigits(dayNumber.toString())
        }
    }

    // --- Month Addition / Navigation ---

    fun addMonths(year: Int, month: Int, delta: Int, calendarType: CalendarType): Pair<Int, Int> {
        var m = month + delta
        var y = year
        while (m < 1) {
            m += 12
            y -= 1
        }
        while (m > 12) {
            m -= 12
            y += 1
        }
        return Pair(y, m)
    }

    /**
     * Converts a given canonical Gregorian dateStr into (year, month) in the requested calendarType.
     */
    fun getYearAndMonth(dateStr: String, calendarType: CalendarType): Pair<Int, Int> {
        return when (calendarType) {
            CalendarType.GREGORIAN -> {
                val g = parseGregorianString(dateStr)
                Pair(g.year, g.month)
            }
            CalendarType.JALALI -> {
                val j = gregorianToJalali(dateStr)
                Pair(j.year, j.month)
            }
            CalendarType.HIJRI -> {
                val h = gregorianToHijri(dateStr)
                Pair(h.year, h.month)
            }
        }
    }

    /**
     * Generates a 42-day calendar matrix for the given year and month in the specified calendarType.
     * All cells contain the canonical Gregorian dateString, along with localized display day number.
     */
    fun getMonthDays(
        year: Int,
        month: Int,
        selectedDate: String,
        todayDate: String,
        calendarType: CalendarType,
        firstDayMonday: Boolean = false
    ): List<CalendarDayData> {
        return when (calendarType) {
            CalendarType.GREGORIAN -> getGregorianMonthDays(year, month, selectedDate, todayDate, firstDayMonday)
            CalendarType.JALALI -> getJalaliMonthDays(year, month, selectedDate, todayDate)
            CalendarType.HIJRI -> getHijriMonthDays(year, month, selectedDate, todayDate)
        }
    }

    private fun getGregorianMonthDays(
        year: Int,
        month: Int,
        selectedDate: String,
        todayDate: String,
        firstDayMonday: Boolean
    ): List<CalendarDayData> {
        val safeMonth = month.coerceIn(1, 12)
        val daysInMonth = getDaysInGregorianMonth(year, safeMonth)
        val firstJdn = gregorianToJdn(year, safeMonth, 1)
        val dow = (((firstJdn + 1) % 7 + 7) % 7).toInt().coerceIn(0, 6) // 0 = Sun.. 6 = Sat

        // Leading days from previous month
        val firstDayOfWeek = if (firstDayMonday) 1 else 0
        var leadingDays = dow - firstDayOfWeek
        if (leadingDays < 0) leadingDays += 7

        val startJdn = firstJdn - leadingDays
        val list = mutableListOf<CalendarDayData>()

        for (i in 0 until 42) {
            val cellJdn = startJdn + i
            val g = jdnToGregorian(cellJdn)
            val dateStr = formatGregorianString(g)
            val isCurrentMonth = g.year == year && g.month == safeMonth

            list.add(
                CalendarDayData(
                    dateString = dateStr,
                    dayNumber = g.day,
                    displayNumber = g.day.toString(),
                    isCurrentMonth = isCurrentMonth,
                    isToday = dateStr == todayDate,
                    isSelected = dateStr == selectedDate
                )
            )
        }
        return list
    }

    private fun getJalaliMonthDays(
        year: Int,
        month: Int,
        selectedDate: String,
        todayDate: String
    ): List<CalendarDayData> {
        val safeMonth = month.coerceIn(1, 12)
        val daysInMonth = getDaysInJalaliMonth(year, safeMonth)
        val firstJdn = jalaliToJdn(year, safeMonth, 1)
        val dow = (((firstJdn + 1) % 7 + 7) % 7).toInt().coerceIn(0, 6) // 0 = Sun.. 6 = Sat

        // In Jalali calendar, week starts on Saturday (dow = 6)
        // Saturday -> 0 leading days, Sunday -> 1, Monday -> 2, ..., Friday -> 6
        val leadingDays = (((dow + 1) % 7 + 7) % 7).toInt().coerceIn(0, 6)

        val startJdn = firstJdn - leadingDays
        val list = mutableListOf<CalendarDayData>()

        for (i in 0 until 42) {
            val cellJdn = startJdn + i
            val j = jdnToJalali(cellJdn)
            val g = jdnToGregorian(cellJdn)
            val dateStr = formatGregorianString(g)
            val isCurrentMonth = j.year == year && j.month == safeMonth

            list.add(
                CalendarDayData(
                    dateString = dateStr,
                    dayNumber = j.day,
                    displayNumber = toPersianDigits(j.day.toString()),
                    isCurrentMonth = isCurrentMonth,
                    isToday = dateStr == todayDate,
                    isSelected = dateStr == selectedDate
                )
            )
        }
        return list
    }

    private fun getHijriMonthDays(
        year: Int,
        month: Int,
        selectedDate: String,
        todayDate: String
    ): List<CalendarDayData> {
        val safeMonth = month.coerceIn(1, 12)
        val daysInMonth = getDaysInHijriMonth(year, safeMonth)
        val firstJdn = hijriToJdn(year, safeMonth, 1)
        val dow = (((firstJdn + 1) % 7 + 7) % 7).toInt().coerceIn(0, 6) // 0 = Sun.. 6 = Sat

        // In Hijri calendar, week starts on Sunday (dow = 0)
        val leadingDays = dow

        val startJdn = firstJdn - leadingDays
        val list = mutableListOf<CalendarDayData>()

        for (i in 0 until 42) {
            val cellJdn = startJdn + i
            val h = jdnToHijri(cellJdn)
            val g = jdnToGregorian(cellJdn)
            val dateStr = formatGregorianString(g)
            val isCurrentMonth = h.year == year && h.month == safeMonth

            list.add(
                CalendarDayData(
                    dateString = dateStr,
                    dayNumber = h.day,
                    displayNumber = toArabicDigits(h.day.toString()),
                    isCurrentMonth = isCurrentMonth,
                    isToday = dateStr == todayDate,
                    isSelected = dateStr == selectedDate
                )
            )
        }
        return list
    }

    /**
     * Generates 7 consecutive days for Week View in the specified calendar system.
     */
    fun getWeekDays(
        selectedDate: String,
        todayDate: String,
        calendarType: CalendarType,
        firstDayMonday: Boolean
    ): List<WeekDayData> {
        val g = parseGregorianString(selectedDate)
        val targetJdn = gregorianToJdn(g.year, g.month, g.day)
        val dow = (((targetJdn + 1) % 7 + 7) % 7).toInt().coerceIn(0, 6) // 0 = Sun.. 6 = Sat

        val offset = when (calendarType) {
            CalendarType.GREGORIAN -> {
                val startDay = if (firstDayMonday) 1 else 0
                var off = dow - startDay
                if (off < 0) off += 7
                off
            }
            CalendarType.JALALI -> {
                // Starts on Saturday
                (((dow + 1) % 7 + 7) % 7).toInt().coerceIn(0, 6)
            }
            CalendarType.HIJRI -> {
                // Starts on Sunday
                dow
            }
        }

        val startJdn = targetJdn - offset
        val result = mutableListOf<WeekDayData>()

        for (i in 0 until 7) {
            val curJdn = startJdn + i
            val curG = jdnToGregorian(curJdn)
            val curDateStr = formatGregorianString(curG)
            val curDow = (((curJdn + 1) % 7 + 7) % 7).toInt().coerceIn(0, 6)

            val (dayName, dayNumberStr) = when (calendarType) {
                CalendarType.GREGORIAN -> {
                    Pair(GREGORIAN_WEEKDAYS_SUN_FIRST.getOrElse(curDow) { "Sun" }, curG.day.toString())
                }
                CalendarType.JALALI -> {
                    val j = jdnToJalali(curJdn)
                    val jalaliDow = (((curDow + 1) % 7 + 7) % 7).toInt().coerceIn(0, 6)
                    Pair(JALALI_WEEKDAYS_SHORT.getOrElse(jalaliDow) { "ش" }, toPersianDigits(j.day.toString()))
                }
                CalendarType.HIJRI -> {
                    val h = jdnToHijri(curJdn)
                    Pair(HIJRI_WEEKDAYS_SHORT.getOrElse(curDow) { "ی" }, toArabicDigits(h.day.toString()))
                }
            }

            result.add(
                WeekDayData(
                    dateString = curDateStr,
                    dayOfWeekName = dayName,
                    dayNumberString = dayNumberStr,
                    isToday = curDateStr == todayDate,
                    isSelected = curDateStr == selectedDate
                )
            )
        }
        return result
    }
}

data class CalendarDayData(
    val dateString: String,
    val dayNumber: Int,
    val displayNumber: String,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean
)

data class WeekDayData(
    val dateString: String,
    val dayOfWeekName: String,
    val dayNumberString: String,
    val isToday: Boolean,
    val isSelected: Boolean
)
