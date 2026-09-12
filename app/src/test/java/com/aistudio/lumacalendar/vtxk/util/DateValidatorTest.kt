package com.aistudio.lumacalendar.vtxk.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DateValidatorTest {

    private val enStrings = AppStrings.English
    private val faStrings = AppStrings.Persian

    @Test
    fun testEmptyAndWhitespaceInput() {
        val r1 = DateValidator.validateAndParse("", CalendarType.GREGORIAN, enStrings)
        assertTrue(r1 is DateValidationResult.Invalid)

        val r2 = DateValidator.validateAndParse("   ", CalendarType.JALALI, faStrings)
        assertTrue(r2 is DateValidationResult.Invalid)

        val r3 = DateValidator.validateAndParse("\t\n", CalendarType.HIJRI, enStrings)
        assertTrue(r3 is DateValidationResult.Invalid)
    }

    @Test
    fun testMalformedAndPartialInputNeverCrashes() {
        val malformedInputs = listOf(
            "2026",
            "2026-",
            "2026-09",
            "2026/09/",
            "not-a-date",
            "hello/world/foo",
            "12/34/56/78",
            "-2026-09-11",
            "2026--09--11",
            "??/??/????",
            "99999999999999999999999-99-99",
            "NaN/NaN/NaN",
            "null",
            "0000-00-00",
            "2026-00-11",
            "2026-11-00"
        )

        for (input in malformedInputs) {
            for (calType in CalendarType.values()) {
                val res = DateValidator.validateAndParse(input, calType, enStrings)
                assertTrue("Expected Invalid for input '$input' in $calType", res is DateValidationResult.Invalid)
            }
        }
    }

    @Test
    fun testValidGregorianDates() {
        val res = DateValidator.validateAndParse("2026-09-11", CalendarType.GREGORIAN, enStrings)
        assertTrue(res is DateValidationResult.Valid)
        val valid = res as DateValidationResult.Valid
        assertEquals("2026-09-11", valid.canonicalGregorianDate)
        assertEquals(2026, valid.year)
        assertEquals(9, valid.month)
        assertEquals(11, valid.day)

        // Leap year 2024-02-29
        val leapRes = DateValidator.validateAndParse("2024-02-29", CalendarType.GREGORIAN, enStrings)
        assertTrue(leapRes is DateValidationResult.Valid)

        // Non-leap year 2025-02-29 should fail
        val nonLeapRes = DateValidator.validateAndParse("2025-02-29", CalendarType.GREGORIAN, enStrings)
        assertTrue(nonLeapRes is DateValidationResult.Invalid)
    }

    @Test
    fun testValidJalaliDates() {
        // 1405-06-20 Jalali corresponds to 2026-09-11 Gregorian
        val res = DateValidator.validateAndParse("1405/06/20", CalendarType.JALALI, faStrings)
        assertTrue("Expected 1405/06/20 to be valid Jalali date", res is DateValidationResult.Valid)
        val valid = res as DateValidationResult.Valid
        assertEquals("2026-09-11", valid.canonicalGregorianDate)
        assertEquals(1405, valid.year)
        assertEquals(6, valid.month)
        assertEquals(20, valid.day)

        // 1405/06/21 corresponds to 2026-09-12
        val res21 = DateValidator.validateAndParse("1405/06/21", CalendarType.JALALI, faStrings)
        assertTrue(res21 is DateValidationResult.Valid)
        assertEquals("2026-09-12", (res21 as DateValidationResult.Valid).canonicalGregorianDate)

        // Nowruz 1405/01/01 corresponds to 2026-03-21
        val nowruz = DateValidator.validateAndParse("1405/01/01", CalendarType.JALALI, enStrings)
        assertTrue(nowruz is DateValidationResult.Valid)
        assertEquals("2026-03-21", (nowruz as DateValidationResult.Valid).canonicalGregorianDate)
    }

    @Test
    fun testValidHijriDates() {
        // 1448-03-28 Hijri corresponds to 2026-09-11 Gregorian in tabular islamic calendar
        val res = DateValidator.validateAndParse("1448/03/28", CalendarType.HIJRI, enStrings)
        assertTrue("Expected 1448/03/28 to be valid Hijri date", res is DateValidationResult.Valid)
        val valid = res as DateValidationResult.Valid
        assertEquals(1448, valid.year)
        assertEquals(3, valid.month)
        assertEquals(28, valid.day)
    }

    @Test
    fun testPersianAndArabicNumeralsNormalization() {
        // Persian digits for "1405/06/20" -> "2026-09-11"
        val persianStr = "۱۴۰۵/۰۶/۲۰"
        val resPersian = DateValidator.validateAndParse(persianStr, CalendarType.JALALI, faStrings)
        assertTrue("Persian digits should be parsed successfully", resPersian is DateValidationResult.Valid)
        assertEquals("2026-09-11", (resPersian as DateValidationResult.Valid).canonicalGregorianDate)

        // Arabic digits for "2026-09-11"
        val arabicStr = "٢٠٢٦-٠٩-١١"
        val resArabic = DateValidator.validateAndParse(arabicStr, CalendarType.GREGORIAN, enStrings)
        assertTrue("Arabic digits should be parsed successfully", resArabic is DateValidationResult.Valid)
        assertEquals("2026-09-11", (resArabic as DateValidationResult.Valid).canonicalGregorianDate)
    }

    @Test
    fun testVariousDelimitersAndContinuousDigits() {
        // Slash delimiter
        val r1 = DateValidator.validateAndParse("2026/09/11", CalendarType.GREGORIAN, enStrings)
        assertTrue(r1 is DateValidationResult.Valid)

        // Dot delimiter
        val r2 = DateValidator.validateAndParse("2026.09.11", CalendarType.GREGORIAN, enStrings)
        assertTrue(r2 is DateValidationResult.Valid)

        // Space delimiter
        val r3 = DateValidator.validateAndParse("2026 09 11", CalendarType.GREGORIAN, enStrings)
        assertTrue(r3 is DateValidationResult.Valid)

        // 8 consecutive digits: 20260911
        val r4 = DateValidator.validateAndParse("20260911", CalendarType.GREGORIAN, enStrings)
        assertTrue(r4 is DateValidationResult.Valid)
        assertEquals("2026-09-11", (r4 as DateValidationResult.Valid).canonicalGregorianDate)
    }

    @Test
    fun testOutOfRangeDates() {
        // Month 13
        val r1 = DateValidator.validateAndParse("2026-13-11", CalendarType.GREGORIAN, enStrings)
        assertTrue(r1 is DateValidationResult.Invalid)

        // Day 32
        val r2 = DateValidator.validateAndParse("2026-05-32", CalendarType.GREGORIAN, enStrings)
        assertTrue(r2 is DateValidationResult.Invalid)

        // Jalali month 12 day 30 on non-leap year (1404 is not leap, 1403 was leap)
        val r3 = DateValidator.validateAndParse("1404/12/30", CalendarType.JALALI, faStrings)
        assertTrue(r3 is DateValidationResult.Invalid)

        // Hijri month 2 (even months have 29 days in civil tabular) day 30
        val r4 = DateValidator.validateAndParse("1448/02/30", CalendarType.HIJRI, enStrings)
        assertTrue(r4 is DateValidationResult.Invalid)
    }

    @Test
    fun testSafeCalendarConverterFallbacks() {
        // Passing bogus string into parseGregorianString
        val gBogus = CalendarConverter.parseGregorianString("completely-invalid-garbage")
        assertEquals(2026, gBogus.year)
        assertEquals(9, gBogus.month)
        assertEquals(11, gBogus.day)

        // Negative year modulo shouldn't crash
        val weekDays = CalendarConverter.getWeekDays(
            selectedDate = "2026-09-11",
            todayDate = "2026-09-11",
            calendarType = CalendarType.JALALI,
            firstDayMonday = false
        )
        assertEquals(7, weekDays.size)
        assertTrue(weekDays.any { it.isSelected })
    }
}
