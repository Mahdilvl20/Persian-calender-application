package com.aistudio.lumacalendar.vtxk

import com.aistudio.lumacalendar.vtxk.data.holiday.HolidayService
import com.aistudio.lumacalendar.vtxk.util.CalendarConverter
import com.aistudio.lumacalendar.vtxk.util.CalendarType
import com.aistudio.lumacalendar.vtxk.util.DynamicIconManager
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
}
