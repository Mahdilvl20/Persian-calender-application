package com.aistudio.lumacalendar.vtxk

import com.aistudio.lumacalendar.vtxk.data.holiday.HolidayService
import com.aistudio.lumacalendar.vtxk.util.CalendarConverter
import com.aistudio.lumacalendar.vtxk.util.CalendarType
import com.aistudio.lumacalendar.vtxk.util.DynamicIconManager
import com.aistudio.lumacalendar.vtxk.util.HijriDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CalendarAndHolidayTest {

    @Test
    fun testCalendarSwitchingPreservesDate() {
        // Test date: March 21, 2026 (Nowruz / 1 Farvardin 1405)
        val gYear = 2026
        val gMonth = 3
        val gDay = 21

        val jdn = CalendarConverter.gregorianToJdn(gYear, gMonth, gDay)
        val jalaliDate = CalendarConverter.jdnToJalali(jdn)
        assertEquals(1405, jalaliDate.year)
        assertEquals(1, jalaliDate.month)
        assertEquals(1, jalaliDate.day)

        // Convert back to Gregorian
        val gBack = CalendarConverter.jdnToGregorian(jdn)
        assertEquals(gYear, gBack.year)
        assertEquals(gMonth, gBack.month)
        assertEquals(gDay, gBack.day)
    }

    @Test
    fun testIranOfficialHolidaysLoaded() {
        val holidayService = HolidayService.default
        val holidays1405 = holidayService.getHolidaysForYear(1405, CalendarType.JALALI)

        assertTrue("Iranian holidays for 1405 should not be empty", holidays1405.isNotEmpty())

        // Check Nowruz (2026-03-21)
        val nowruz = holidayService.getHoliday("2026-03-21", CalendarType.JALALI)
        assertNotNull("Nowruz should be a registered holiday", nowruz)
        assertTrue("Nowruz must be an official holiday", nowruz!!.isOfficialHoliday)
        assertTrue("Nowruz name should contain نوروز", nowruz.name.contains("نوروز"))

        // Check Nature Day (2026-04-02 / 13 Farvardin)
        val natureDay = holidayService.getHoliday("2026-04-02", CalendarType.JALALI)
        assertNotNull("13 Farvardin should be a registered holiday", natureDay)
        assertTrue("Nature day must be official holiday", natureDay!!.isOfficialHoliday)
    }

    @Test
    fun testUSFederalHolidaysLoaded() {
        val holidayService = HolidayService.default
        val newYearsDay = holidayService.getHoliday("2026-01-01", CalendarType.GREGORIAN)
        assertNotNull("Jan 1 should be US holiday", newYearsDay)
        assertTrue("Jan 1 must be official", newYearsDay!!.isOfficialHoliday)
        assertEquals("New Year's Day", newYearsDay.name)

        val independenceDay = holidayService.getHoliday("2026-07-04", CalendarType.GREGORIAN)
        assertNotNull("July 4 should be US holiday", independenceDay)
        assertEquals("Independence Day", independenceDay!!.name)
    }

    @Test
    fun testDigitLocalization() {
        assertEquals("۱۲۳۴۵", CalendarConverter.toPersianDigits("12345"))
        assertEquals("١٢٣٤٥", CalendarConverter.toArabicDigits("12345"))
    }

    @Test
    fun testDynamicIconManagerRealDeviceDay() {
        val realDay = DynamicIconManager.getRealDeviceDay()
        val expected = LocalDate.now().dayOfMonth
        assertEquals(expected, realDay)
    }

    @Test
    fun testTripleCalendarDaysUseSameGregorianDate() {
        val days = DynamicIconManager.getTripleCalendarDays("2026-03-21")

        assertEquals(1, days.jalali)
        assertEquals(21, days.gregorian)
        assertEquals(2, days.hijri)
    }

    @Test
    fun testCivilHijriKnownDates() {
        listOf(
            "2025-03-20" to HijriDate(1446, 9, 20),
            "2026-09-11" to HijriDate(1448, 3, 28),
            "2026-09-12" to HijriDate(1448, 3, 29)
        ).forEach { (gregorian, hijri) ->
            assertEquals(hijri, CalendarConverter.gregorianToHijri(gregorian))
            assertEquals(gregorian, CalendarConverter.hijriToGregorianString(hijri.year, hijri.month, hijri.day))
        }
    }

    @Test
    fun testPersianCalendarApiDtoParsingWithTypoHandling() {
        // Verifies the exact API typo 'holidayDesription' is handled and resolved to holidayDescription
        val rawJson = """
            {
              "data": [
                {
                  "date": "1403/01/04",
                  "shamsiDate": "1403/01/04",
                  "isHoliday": true,
                  "holidayDesription": "عیدنوروز"
                },
                {
                  "date": "1403/01/05",
                  "shamsiDate": "1403/01/05",
                  "isHoliday": false,
                  "holidayDescription": null
                }
              ]
            }
        """.trimIndent()

        val adapter = com.aistudio.lumacalendar.vtxk.data.api.ApiClient.moshi
            .adapter(com.aistudio.lumacalendar.vtxk.data.api.dto.PersianCalendarYearResponseDto::class.java)
        val response = adapter.fromJson(rawJson)

        assertNotNull(response)
        assertNotNull(response?.data)
        assertEquals(2, response?.data?.size)

        val day1 = response!!.data[0]
        assertEquals("1403/01/04", day1.date)
        assertTrue(day1.isHoliday)
        assertEquals("عیدنوروز", day1.resolvedDescription)

        // Convert to domain model
        val domain = com.aistudio.lumacalendar.vtxk.data.model.PersianCalendarDay(
            date = "2024-03-23",
            shamsiDate = day1.shamsiDate,
            isHoliday = day1.isHoliday,
            holidayDescription = day1.resolvedDescription
        )
        assertEquals("1403/01/04", domain.shamsiDate)
        assertTrue(domain.isHoliday)
        assertEquals("عیدنوروز", domain.holidayDescription)

        val day2 = response.data[1]
        org.junit.Assert.assertFalse(day2.isHoliday)
        org.junit.Assert.assertNull(day2.resolvedDescription)
    }

    @Test
    fun testPersianCalendarRepositoryMockDelegationInHolidayRepository() {
        val mockRepo = object : com.aistudio.lumacalendar.vtxk.data.repository.PersianCalendarRepository {
            private val days = listOf(
                com.aistudio.lumacalendar.vtxk.data.model.PersianCalendarDay(
                    date = "2026-03-21",
                    shamsiDate = "1405/01/01",
                    isHoliday = true,
                    holidayDescription = "عیدنوروز (منبع API)"
                ),
                com.aistudio.lumacalendar.vtxk.data.model.PersianCalendarDay(
                    date = "2026-03-22",
                    shamsiDate = "1405/01/02",
                    isHoliday = true,
                    holidayDescription = "عیدنوروز"
                )
            )

            override suspend fun getDaysForYear(year: Int) = Result.success(days)
            override fun getCachedDaysForYear(year: Int) = days
            override suspend fun getHolidaysForYear(year: Int) = days.filter { it.isHoliday }
            override suspend fun getDayByGregorianDate(dateStr: String) = days.firstOrNull { it.date == dateStr }
            override suspend fun getDayByShamsiDate(shamsiDate: String) = days.firstOrNull { it.shamsiDate == shamsiDate }
            override suspend fun convertMiladiToShamsi(dateStr: String) = "1405/01/01"
            override suspend fun convertShamsiToMiladi(dateStr: String) = "2026-03-21"
        }

        val iranProvider = com.aistudio.lumacalendar.vtxk.data.holiday.IranHolidayProvider()
        val usProvider = com.aistudio.lumacalendar.vtxk.data.holiday.USFederalHolidayProvider()
        val holidayRepo = com.aistudio.lumacalendar.vtxk.data.holiday.HolidayRepositoryImpl(
            iranHolidayProvider = iranProvider,
            usFederalHolidayProvider = usProvider,
            persianCalendarRepository = mockRepo
        )

        // Query holidays for Jalali year 1405
        val holidays = holidayRepo.getHolidaysForYear(1405, CalendarType.JALALI)
        assertEquals(2, holidays.size)
        assertEquals("عیدنوروز (منبع API)", holidays[0].name)
        assertTrue(holidays[0].isOfficialHoliday)

        // Query specific date
        val nowruzHoliday = holidayRepo.getHolidayForDate("2026-03-21", CalendarType.JALALI)
        assertNotNull(nowruzHoliday)
        assertEquals("عیدنوروز (منبع API)", nowruzHoliday?.name)
    }

    @Test
    fun testComprehensiveJdnRoundTripConversions() {
        // Test Gregorian round trips across standard years, leap years, century boundaries
        val gregorianDates = listOf(
            Triple(2000, 2, 29), // Leap century
            Triple(1900, 2, 28), // Non-leap century
            Triple(2024, 2, 29), // Leap year
            Triple(2025, 2, 28), // Common year
            Triple(2026, 1, 1),
            Triple(2026, 9, 16),
            Triple(2026, 12, 31)
        )
        for ((y, m, d) in gregorianDates) {
            val jdn = CalendarConverter.gregorianToJdn(y, m, d)
            val gBack = CalendarConverter.jdnToGregorian(jdn)
            assertEquals("Gregorian round-trip failed for $y-$m-$d", Triple(y, m, d), Triple(gBack.year, gBack.month, gBack.day))
        }

        // Test Jalali round trips across leap years, month boundaries, and end-of-year
        val jalaliDates = listOf(
            Triple(1400, 1, 1),
            Triple(1400, 12, 29),
            Triple(1401, 7, 15),
            Triple(1402, 3, 31),
            Triple(1403, 12, 29),
            Triple(1404, 12, 29),
            Triple(1405, 1, 1),   // Nowruz
            Triple(1405, 6, 25),
            Triple(1405, 12, 29)
        )
        for ((y, m, d) in jalaliDates) {
            val jdn = CalendarConverter.jalaliToJdn(y, m, d)
            val jBack = CalendarConverter.jdnToJalali(jdn)
            assertEquals("Jalali round-trip failed for $y-$m-$d", Triple(y, m, d), Triple(jBack.year, jBack.month, jBack.day))
        }

        // Test Hijri round trips across 30-day and 29-day months
        val hijriDates = listOf(
            Triple(1445, 1, 1),
            Triple(1446, 9, 1),
            Triple(1448, 3, 24),
            Triple(1448, 12, 29)
        )
        for ((y, m, d) in hijriDates) {
            val jdn = CalendarConverter.hijriToJdn(y, m, d)
            val hBack = CalendarConverter.jdnToHijri(jdn)
            assertEquals("Hijri round-trip failed for $y-$m-$d", Triple(y, m, d), Triple(hBack.year, hBack.month, hBack.day))
        }
    }

    @Test
    fun testTimezoneChangeScenarioForRealDeviceDate() {
        val originalTz = java.util.TimeZone.getDefault()
        try {
            val testTimezones = listOf(
                "Asia/Tehran",
                "America/New_York",
                "Pacific/Kiritimati", // UTC+14 (furthest ahead)
                "Pacific/Pago_Pago",  // UTC-11 (furthest behind)
                "UTC"
            )

            for (tzId in testTimezones) {
                val tz = java.util.TimeZone.getTimeZone(tzId)
                java.util.TimeZone.setDefault(tz)

                val expectedDate = java.time.LocalDate.now(java.time.ZoneId.of(tzId)).toString()
                val actualDate = com.aistudio.lumacalendar.vtxk.util.DateUtils.getRealDeviceDate()
                assertEquals("Real device date must match local date in timezone $tzId", expectedDate, actualDate)

                val actualZoneId = com.aistudio.lumacalendar.vtxk.util.DateUtils.getDeviceZoneId()
                assertEquals(tz.toZoneId(), actualZoneId)
            }
        } finally {
            java.util.TimeZone.setDefault(originalTz)
        }
    }

    @Test
    fun testLocalizationManagerRtlAndLayoutDirection() {
        // Jalali and Hijri should be RTL
        assertTrue(com.aistudio.lumacalendar.vtxk.util.LocalizationManager.isRtl(CalendarType.JALALI))
        assertTrue(com.aistudio.lumacalendar.vtxk.util.LocalizationManager.isRtl(CalendarType.HIJRI))
        assertEquals(androidx.compose.ui.unit.LayoutDirection.Rtl, com.aistudio.lumacalendar.vtxk.util.LocalizationManager.getLayoutDirection(CalendarType.JALALI))
        assertEquals(androidx.compose.ui.unit.LayoutDirection.Rtl, com.aistudio.lumacalendar.vtxk.util.LocalizationManager.getLayoutDirection(CalendarType.HIJRI))

        // Gregorian should be LTR
        org.junit.Assert.assertFalse(com.aistudio.lumacalendar.vtxk.util.LocalizationManager.isRtl(CalendarType.GREGORIAN))
        assertEquals(androidx.compose.ui.unit.LayoutDirection.Ltr, com.aistudio.lumacalendar.vtxk.util.LocalizationManager.getLayoutDirection(CalendarType.GREGORIAN))

        // Strings variants
        assertEquals("تقویم", com.aistudio.lumacalendar.vtxk.util.LocalizationManager.getStrings(CalendarType.JALALI).tabCalendar)
        assertEquals("Calendar", com.aistudio.lumacalendar.vtxk.util.LocalizationManager.getStrings(CalendarType.GREGORIAN).tabCalendar)
    }

    @Test
    fun testCalendarConverterEdgeCaseJdn() {
        // Safe handling of extreme/edge-case JDNs (day 0, negative JDN)
        // 0 JDN corresponds to ancient astronomical epoch (~4713 BC)
        val g0 = CalendarConverter.jdnToGregorian(0L)
        assertNotNull(g0)
        assertTrue(g0.month in 1..12)
        assertTrue(g0.day in 1..31)

        val j0 = CalendarConverter.jdnToJalali(0L)
        assertNotNull(j0)
        assertTrue(j0.month in 1..12)
        assertTrue(j0.day in 1..31)

        val h0 = CalendarConverter.jdnToHijri(0L)
        assertNotNull(h0)
        assertTrue(h0.month in 1..12)
        assertTrue(h0.day in 1..30)

        // Negative JDN
        val gNeg = CalendarConverter.jdnToGregorian(-1000L)
        assertNotNull(gNeg)
        val jNeg = CalendarConverter.jdnToJalali(-1000L)
        assertNotNull(jNeg)
        val hNeg = CalendarConverter.jdnToHijri(-1000L)
        assertNotNull(hNeg)
    }
}

