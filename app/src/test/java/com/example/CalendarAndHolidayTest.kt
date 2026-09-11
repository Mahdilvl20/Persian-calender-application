package com.example

import com.example.data.holiday.HolidayService
import com.example.util.CalendarConverter
import com.example.util.CalendarType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

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
}
