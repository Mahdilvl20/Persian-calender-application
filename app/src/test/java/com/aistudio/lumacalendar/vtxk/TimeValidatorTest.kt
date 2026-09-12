package com.aistudio.lumacalendar.vtxk

import com.aistudio.lumacalendar.vtxk.util.ParsedTime
import com.aistudio.lumacalendar.vtxk.util.TimeValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class TimeValidatorTest {

    @Test
    fun testParseStandard24Hour() {
        val parsed = TimeValidator.parseTime("14:30")
        assertEquals(14, parsed.hour)
        assertEquals(30, parsed.minute)
        assertEquals("14:30", parsed.canonicalTime)
        assertEquals(2, parsed.hour12)
        assertTrue(parsed.isPm)
    }

    @Test
    fun testParseMorning24Hour() {
        val parsed = TimeValidator.parseTime("08:15")
        assertEquals(8, parsed.hour)
        assertEquals(15, parsed.minute)
        assertEquals("08:15", parsed.canonicalTime)
        assertEquals(8, parsed.hour12)
        assertFalse(parsed.isPm)
    }

    @Test
    fun testParseMidnightAndNoon() {
        val midnight = TimeValidator.parseTime("00:00")
        assertEquals(0, midnight.hour)
        assertEquals(0, midnight.minute)
        assertEquals(12, midnight.hour12)
        assertFalse(midnight.isPm)

        val noon = TimeValidator.parseTime("12:00")
        assertEquals(12, noon.hour)
        assertEquals(0, noon.minute)
        assertEquals(12, noon.hour12)
        assertTrue(noon.isPm)
    }

    @Test
    fun testParse12HourWithAmPm() {
        val amTime = TimeValidator.parseTime("9:45 AM")
        assertEquals(9, amTime.hour)
        assertEquals(45, amTime.minute)
        assertFalse(amTime.isPm)

        val pmTime = TimeValidator.parseTime("03:20 pm")
        assertEquals(15, pmTime.hour)
        assertEquals(20, pmTime.minute)
        assertTrue(pmTime.isPm)

        val noon12 = TimeValidator.parseTime("12:00 PM")
        assertEquals(12, noon12.hour)
        assertTrue(noon12.isPm)

        val midnight12 = TimeValidator.parseTime("12:00 AM")
        assertEquals(0, midnight12.hour)
        assertFalse(midnight12.isPm)
    }

    @Test
    fun testParsePersianDigitsAndAmPm() {
        // "۰۹:۳۰"
        val persianTime = TimeValidator.parseTime("۰۹:۳۰")
        assertEquals(9, persianTime.hour)
        assertEquals(30, persianTime.minute)
        assertEquals("09:30", persianTime.canonicalTime)

        // "۲:۴۵ ب.ظ"
        val persianPm = TimeValidator.parseTime("۲:۴۵ ب.ظ")
        assertEquals(14, persianPm.hour)
        assertEquals(45, persianPm.minute)
        assertTrue(persianPm.isPm)
    }

    @Test
    fun testParseArabicDigits() {
        // "١٤:٤٥"
        val arabicTime = TimeValidator.parseTime("١٤:٤٥")
        assertEquals(14, arabicTime.hour)
        assertEquals(45, arabicTime.minute)
        assertEquals("14:45", arabicTime.canonicalTime)
    }

    @Test
    fun testParseNullEmptyAndGarbage() {
        val fromNull = TimeValidator.parseTime(null, 9, 0)
        assertEquals(9, fromNull.hour)
        assertEquals(0, fromNull.minute)

        val fromBlank = TimeValidator.parseTime("   ", 10, 30)
        assertEquals(10, fromBlank.hour)
        assertEquals(30, fromBlank.minute)

        val fromGarbage = TimeValidator.parseTime("not a time", 8, 0)
        assertEquals(8, fromGarbage.hour)
        assertEquals(0, fromGarbage.minute)
    }

    @Test
    fun testOutOrBoundsClamping() {
        val outOfBounds = TimeValidator.parseTime("99:99")
        assertEquals(23, outOfBounds.hour)
        assertEquals(59, outOfBounds.minute)

        val negativeLike = TimeValidator.parseTime("-5:-10", 9, 0)
        // Coerces safely into 0..23 and 0..59
        assertTrue(negativeLike.hour in 0..23)
        assertTrue(negativeLike.minute in 0..59)
    }

    @Test
    fun testIsValidTime() {
        assertTrue(TimeValidator.isValidTime("09:00"))
        assertTrue(TimeValidator.isValidTime("23:59"))
        assertTrue(TimeValidator.isValidTime("00:00"))
        assertTrue(TimeValidator.isValidTime("۰۹:۳۰")) // Persian digits
        assertFalse(TimeValidator.isValidTime("24:00"))
        assertFalse(TimeValidator.isValidTime("25:00"))
        assertFalse(TimeValidator.isValidTime("12:60"))
        assertFalse(TimeValidator.isValidTime("-1:00"))
        assertFalse(TimeValidator.isValidTime(null))
        assertFalse(TimeValidator.isValidTime(""))
        assertFalse(TimeValidator.isValidTime("invalid"))
    }

    @Test
    fun testNormalizeTime() {
        assertEquals("09:00", TimeValidator.normalizeTime("9:0"))
        assertEquals("14:05", TimeValidator.normalizeTime("14:5"))
        assertEquals("09:00", TimeValidator.normalizeTime(""))
        assertEquals("10:00", TimeValidator.normalizeTime(null, "10:00"))
    }

    @Test
    fun testEnsureValidRange() {
        // Normal case: start 09:00, end 10:00 -> unchanged
        val (s1, e1) = TimeValidator.ensureValidRange("09:00", "10:00")
        assertEquals("09:00", s1)
        assertEquals("10:00", e1)

        // Invalid reversed case: start 14:00, end 11:00 -> end pushed to 15:00 (+60 min default)
        val (s2, e2) = TimeValidator.ensureValidRange("14:00", "11:00")
        assertEquals("14:00", s2)
        assertEquals("15:00", e2)

        // Equal case: start 10:00, end 10:00 -> end pushed to 11:00
        val (s3, e3) = TimeValidator.ensureValidRange("10:00", "10:00")
        assertEquals("10:00", s3)
        assertEquals("11:00", e3)

        // Late evening near midnight: start 23:30, end 22:00 -> end capped at 23:59
        val (s4, e4) = TimeValidator.ensureValidRange("23:30", "22:00")
        assertEquals("23:30", s4)
        assertEquals("23:59", e4)
    }

    @Test
    fun testAddMinutesAndDuration() {
        assertEquals("10:30", TimeValidator.addMinutes("09:30", 60))
        assertEquals("11:15", TimeValidator.addMinutes("10:45", 30))
        assertEquals(75, TimeValidator.calculateDurationMinutes("09:15", "10:30"))
        assertEquals(0, TimeValidator.calculateDurationMinutes("11:00", "09:00"))
    }

    @Test
    fun testCreateSafeLocalTimeAndDateTime() {
        val lt = TimeValidator.createSafeLocalTime(14, 30)
        assertEquals(LocalTime.of(14, 30), lt)

        val clampedLt = TimeValidator.createSafeLocalTime(30, 80)
        assertEquals(LocalTime.of(23, 59), clampedLt)

        val ldt = TimeValidator.createSafeLocalDateTime("2026-09-12", "15:45")
        assertNotNull(ldt)
        assertEquals(2026, ldt.year)
        assertEquals(9, ldt.monthValue)
        assertEquals(12, ldt.dayOfMonth)
        assertEquals(15, ldt.hour)
        assertEquals(45, ldt.minute)

        // Garbage date and time
        val safeLdt = TimeValidator.createSafeLocalDateTime("invalid-date", "invalid-time")
        assertNotNull(safeLdt)
    }
}
