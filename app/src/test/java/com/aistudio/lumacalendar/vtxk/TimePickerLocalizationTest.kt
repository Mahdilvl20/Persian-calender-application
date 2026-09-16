package com.aistudio.lumacalendar.vtxk

import androidx.compose.ui.unit.LayoutDirection
import com.aistudio.lumacalendar.vtxk.util.CalendarType
import com.aistudio.lumacalendar.vtxk.util.LocalizationManager
import com.aistudio.lumacalendar.vtxk.util.ParsedTime
import com.aistudio.lumacalendar.vtxk.util.TimeValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimePickerLocalizationTest {

    @Test
    fun testCalendarTypeDrivesLayoutDirection() {
        // JALALI and HIJRI calendars must resolve to RTL layout direction
        assertEquals(LayoutDirection.Rtl, LocalizationManager.getLayoutDirection(CalendarType.JALALI))
        assertEquals(LayoutDirection.Rtl, LocalizationManager.getLayoutDirection(CalendarType.HIJRI))
        assertTrue(LocalizationManager.isRtl(CalendarType.JALALI))
        assertTrue(LocalizationManager.isRtl(CalendarType.HIJRI))

        // GREGORIAN must resolve to LTR layout direction
        assertEquals(LayoutDirection.Ltr, LocalizationManager.getLayoutDirection(CalendarType.GREGORIAN))
        assertFalse(LocalizationManager.isRtl(CalendarType.GREGORIAN))
    }

    @Test
    fun testPersianTimeFormattingPreservesOrder() {
        val time = ParsedTime.ofSafe(14, 30)

        // 24-hour format in RTL uses Persian digits: "۱۴:۳۰"
        val formatted24 = time.format24Hour(isRtl = true)
        assertEquals("۱۴:۳۰", formatted24)

        // 12-hour format in RTL uses Persian digits and Persian marker: "۲:۳۰ ب.ظ"
        val formatted12 = time.format12Hour(isRtl = true)
        assertEquals("۲:۳۰ ب.ظ", formatted12)

        // 12-hour format in LTR uses Western digits and AM/PM: "2:30 PM"
        val formatted12Ltr = time.format12Hour(isRtl = false)
        assertEquals("2:30 PM", formatted12Ltr)
    }

    @Test
    fun testMorningTimeFormatting() {
        val time = ParsedTime.ofSafe(9, 15)

        val formatted24Rtl = time.format24Hour(isRtl = true)
        assertEquals("۰۹:۱۵", formatted24Rtl)

        val formatted12Rtl = time.format12Hour(isRtl = true)
        assertEquals("۹:۱۵ ق.ظ", formatted12Rtl)

        val formatted12Ltr = time.format12Hour(isRtl = false)
        assertEquals("9:15 AM", formatted12Ltr)
    }

    @Test
    fun testSafeBoundaryHandling() {
        // Safe creation handles wrap-around/clamping correctly
        val clampedTime = ParsedTime.ofSafe(25, 65)
        assertEquals(23, clampedTime.hour)
        assertEquals(59, clampedTime.minute)

        val minTime = ParsedTime.ofSafe(-5, -10)
        assertEquals(0, minTime.hour)
        assertEquals(0, minTime.minute)
    }

    @Test
    fun testTimeParseRoundTrip() {
        val original = "16:45"
        val parsed = TimeValidator.parseTime(original)
        assertEquals(16, parsed.hour)
        assertEquals(45, parsed.minute)
        assertEquals("16:45", parsed.canonicalTime)

        // Add 30 mins
        val after30 = TimeValidator.addMinutes(original, 30)
        assertEquals("17:15", after30)
    }
}
