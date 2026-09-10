package com.example

import com.example.util.CalendarConverter
import com.example.util.CalendarType
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testGregorianJdnRoundTrip() {
        val jdn = CalendarConverter.gregorianToJdn(2026, 9, 11)
        val g = CalendarConverter.jdnToGregorian(jdn)
        assertEquals(2026, g.year)
        assertEquals(9, g.month)
        assertEquals(11, g.day)
    }

    @Test
    fun testJalaliConversion() {
        // September 11, 2026 is 1405-06-20 (20 Shahrivar 1405)
        val j = CalendarConverter.gregorianToJalali("2026-09-11")
        assertEquals(1405, j.year)
        assertEquals(6, j.month)
        assertEquals(20, j.day)

        val backG = CalendarConverter.jalaliToGregorianString(j.year, j.month, j.day)
        assertEquals("2026-09-11", backG)
    }

    @Test
    fun testHijriConversion() {
        val h = CalendarConverter.gregorianToHijri("2026-09-11")
        assertEquals(1448, h.year)
        assertTrue(h.month in 2..3) // Safar or Rabi' al-Awwal 1448
        assertTrue(h.day in 1..30)

        val backG = CalendarConverter.hijriToGregorianString(h.year, h.month, h.day)
        assertEquals("2026-09-11", backG)
    }

    @Test
    fun testMonthDaysGeneration() {
        for (calType in CalendarType.values()) {
            val (y, m) = CalendarConverter.getYearAndMonth("2026-09-11", calType)
            val days = CalendarConverter.getMonthDays(
                year = y,
                month = m,
                selectedDate = "2026-09-11",
                todayDate = "2026-09-11",
                calendarType = calType
            )
            assertEquals(42, days.size)
            val selectedCount = days.count { it.isSelected }
            assertEquals("Should have exactly 1 selected day for $calType", 1, selectedCount)
        }
    }
}

