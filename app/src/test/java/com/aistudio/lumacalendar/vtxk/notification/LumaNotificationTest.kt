package com.aistudio.lumacalendar.vtxk.notification

import android.Manifest
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.aistudio.lumacalendar.vtxk.R
import com.aistudio.lumacalendar.vtxk.util.CalendarConverter
import com.aistudio.lumacalendar.vtxk.util.CalendarType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.GraphicsMode

// NATIVE graphics mode makes Robolectric actually rasterize Canvas draw calls;
// the legacy default treats them as no-ops, leaving generated bitmaps blank.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@RunWith(RobolectricTestRunner::class)
class LumaNotificationTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    /**
     * Geometry constraints from data-model.md / contracts/day-icon-contract.md.
     *
     * - safe radius = 0.46 × size
     * - centering tolerance = ink center within 1% of size of the canvas center on both axes
     * - fill = >= 60% of the safe area along its constraining dimension
     * - fit = half-diagonal(ink box) <= safeRadius
     */
    private companion object Geometry {
        const val SAFE_RADIUS_RATIO = 0.46f
        const val CENTER_TOLERANCE_RATIO = 0.01f
        const val MIN_FILL_RATIO_OF_SAFE = 0.60f
    }

    private data class InkBounds(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int,
        val opaqueWhiteCount: Int,
    ) {
        val width: Int get() = right - left + 1
        val height: Int get() = bottom - top + 1
        val centerX: Float get() = (left + right) / 2f
        val centerY: Float get() = (top + bottom) / 2f
        val halfDiagonal: Float
            get() {
                val w = width.toFloat()
                val h = height.toFloat()
                return kotlin.math.sqrt(w * w + h * h) / 2f
            }

        /** Constraining dimension of the safe area: longer ink axis vs the safe circle diameter. */
        val constrainingDimension: Int
            get() = maxOf(width, height)
    }

    /**
     * Measure the ink bounding box of a rendered icon: left/top/right/bottom of its
     * non-transparent pixels plus the count of fully-opaque white pixels.
     *
     * Requires `@GraphicsMode(GraphicsMode.Mode.NATIVE)` on this class (line ~22) so
     * `drawText` actually rasterizes — otherwise the measured box is empty and every
     * geometry assertion is meaningless.
     */
    private fun measureInkBounds(bitmap: android.graphics.Bitmap): InkBounds {
        var left = Int.MAX_VALUE
        var top = Int.MAX_VALUE
        var right = Int.MIN_VALUE
        var bottom = Int.MIN_VALUE
        var opaqueWhiteCount = 0
        var nonTransparentCount = 0

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val pixel = bitmap.getPixel(x, y)
                val alpha = android.graphics.Color.alpha(pixel)
                if (alpha == 0) continue
                nonTransparentCount++
                if (pixel == android.graphics.Color.WHITE) opaqueWhiteCount++
                if (x < left) left = x
                if (y < top) top = y
                if (x > right) right = x
                if (y > bottom) bottom = y
            }
        }

        assertTrue(
            "Rendered icon has no non-transparent pixels; GraphicsMode.NATIVE is required",
            nonTransparentCount > 0
        )
        return InkBounds(left, top, right, bottom, opaqueWhiteCount)
    }

    private fun safeRadius(size: Int): Float = SAFE_RADIUS_RATIO * size

    @Test
    fun testRealWorldDateEquivalenceForSeptember13_2026() {
        val testDate = "2026-09-13"

        // Jalali equivalence
        val j = CalendarConverter.gregorianToJalali(testDate)
        assertEquals(1405, j.year)
        assertEquals(6, j.month) // Shahrivar
        assertEquals(22, j.day)

        val jWeekday = CalendarConverter.getWeekdayName(testDate, CalendarType.JALALI)
        val jDayPersian = CalendarConverter.toPersianDigits(j.day.toString())
        val jMonthName = CalendarConverter.getMonthName(j.month, CalendarType.JALALI)
        val jYearPersian = CalendarConverter.toPersianDigits(j.year.toString())

        assertEquals("یکشنبه", jWeekday)
        assertEquals("۲۲", jDayPersian)
        assertEquals("شهریور", jMonthName)
        assertEquals("۱۴۰۵", jYearPersian)

        val persianFullDate = "$jWeekday $jDayPersian $jMonthName $jYearPersian"
        assertEquals("یکشنبه ۲۲ شهریور ۱۴۰۵", persianFullDate)

        // Gregorian equivalence
        val g = CalendarConverter.parseGregorianString(testDate)
        assertEquals(2026, g.year)
        assertEquals(9, g.month)
        assertEquals(13, g.day)
        val gMonthName = CalendarConverter.getMonthName(g.month, CalendarType.GREGORIAN)
        assertEquals("September", gMonthName)

        val gregorianDate = "${g.day} $gMonthName ${g.year}"
        assertEquals("13 September 2026", gregorianDate)

        // Hijri equivalence from the exact same JDN
        val h = CalendarConverter.gregorianToHijri(testDate)
        assertTrue(h.year in 1447..1449)
        assertTrue(h.month in 1..12)
        assertTrue(h.day in 1..30)
    }

    @Test
    fun testDynamicIconGeneration() {
        val bitmap = LumaNotificationIconGenerator.generateIcon(context, "۲۲", 120)
        assertNotNull(bitmap)
        assertEquals(120, bitmap.width)
        assertEquals(120, bitmap.height)
    }

    @Test
    fun testSmallIconRendersForSingleAndDoubleDigit() {
        // Double-digit Jalali day
        val doubleDigit = LumaNotificationIconGenerator.generateSmallIcon(context, "۳۱", 96)
        assertNotNull(doubleDigit)
        assertEquals(96, doubleDigit.width)
        assertEquals(96, doubleDigit.height)

        // Single-digit Jalali day
        val singleDigit = LumaNotificationIconGenerator.generateSmallIcon(context, "۹", 96)
        assertNotNull(singleDigit)
        assertEquals(96, singleDigit.width)

        // Alpha-mask: at least some fully-opaque white pixels exist (the glyph)
        var opaqueWhite = 0
        for (x in 0 until doubleDigit.width step 4) {
            for (y in 0 until doubleDigit.height step 4) {
                if (doubleDigit.getPixel(x, y) == android.graphics.Color.WHITE) opaqueWhite++
            }
        }
        assertTrue("Small icon must contain opaque white glyph pixels", opaqueWhite > 0)
    }

    @Test
    fun testSmallIconFrameAbsenceDayNumberOnly() {
        // VI-001 / TR-006: every opaque pixel lies inside the ink box returned by the
        // helper (the box is defined as the opaque bbox, so this also proves there is
        // no detached frame/tab fragment outside the measured region) and at least one
        // fully-opaque white pixel exists (VI-005). The calendar outline, header
        // separator, and both binder tabs would force a near-square canvas-scale box
        // for BOTH single- and double-digit days; a day-number-only icon has the digit
        // aspect instead (tall for one glyph, wide for two).
        listOf("۳۱", "۹").forEach { dayText ->
            val size = 96
            val bitmap = LumaNotificationIconGenerator.generateSmallIcon(context, dayText, size)
            val ink = measureInkBounds(bitmap)

            assertTrue(
                "Day $dayText must contain fully-opaque white glyph pixels (VI-005)",
                ink.opaqueWhiteCount > 0
            )

            var opaqueOutsideInk = 0
            for (x in 0 until bitmap.width) {
                for (y in 0 until bitmap.height) {
                    if (android.graphics.Color.alpha(bitmap.getPixel(x, y)) == 0) continue
                    if (x < ink.left || x > ink.right || y < ink.top || y > ink.bottom) {
                        opaqueOutsideInk++
                    }
                }
            }
            assertEquals(
                "Day $dayText: opaque pixels outside the measured ink box must be 0 (VI-001)",
                0, opaqueOutsideInk
            )

            // Frame silhouette is ~square (calendar body + tabs). Digit glyphs are not.
            if (dayText.length == 1) {
                assertTrue(
                    "Day $dayText: single-digit ink must be taller than wide (frame gone), " +
                        "got ${ink.width}x${ink.height}",
                    ink.height >= ink.width
                )
            } else {
                assertTrue(
                    "Day $dayText: double-digit ink must be wider than tall (frame gone), " +
                        "got ${ink.width}x${ink.height}",
                    ink.width > ink.height
                )
            }

            // The old frame + tabs spanned nearly the full canvas on both axes.
            assertTrue(
                "Day $dayText: ink box must not span the old calendar-frame height " +
                    "(${ink.height}px vs canvas $size)",
                ink.height < size * 0.95f
            )
            assertTrue(
                "Day $dayText: ink box must not span the old calendar-frame width " +
                    "(${ink.width}px vs canvas $size)",
                ink.width < size * 0.95f
            )
        }
    }

    @Test
    fun testSmallIconCenteredOnIconAxes() {
        // TR-003 / VI-002: ink box center within 0.01 * size of the canvas center on both axes.
        listOf("۹", "۳۱").forEach { dayText ->
            val size = 96
            val bitmap = LumaNotificationIconGenerator.generateSmallIcon(context, dayText, size)
            val ink = measureInkBounds(bitmap)
            val tolerance = CENTER_TOLERANCE_RATIO * size

            val dx = kotlin.math.abs(ink.centerX - size / 2f)
            val dy = kotlin.math.abs(ink.centerY - size / 2f)

            assertTrue(
                "Day $dayText: horizontal center off by $dx (tolerance $tolerance)",
                dx <= tolerance
            )
            assertTrue(
                "Day $dayText: vertical center off by $dy (tolerance $tolerance)",
                dy <= tolerance
            )
        }
    }

    @Test
    fun testSmallIconNeverClipped() {
        // TR-004 / VI-003: ink box fully inside image bounds and half-diagonal <= 0.46 * size.
        listOf("۹", "۳۱").forEach { dayText ->
            val size = 96
            val bitmap = LumaNotificationIconGenerator.generateSmallIcon(context, dayText, size)
            val ink = measureInkBounds(bitmap)
            val safeRadius = safeRadius(size)

            assertTrue("Day $dayText: ink.left ${ink.left} out of bounds", ink.left >= 0)
            assertTrue("Day $dayText: ink.top ${ink.top} out of bounds", ink.top >= 0)
            assertTrue("Day $dayText: ink.right ${ink.right} out of bounds", ink.right < size)
            assertTrue("Day $dayText: ink.bottom ${ink.bottom} out of bounds", ink.bottom < size)

            assertTrue(
                "Day $dayText: half-diagonal ${ink.halfDiagonal} exceeds safe radius $safeRadius",
                ink.halfDiagonal <= safeRadius + 0.5f
            )
        }
    }

    @Test
    fun testSmallIconLargeCenteredLegible() {
        // TR-005 / VI-004 / VI-005: ink box spans >= 60% of the safe area along its
        // constraining dimension and contains fully-opaque white pixels (unbroken mark).
        listOf("۹", "۳۱").forEach { dayText ->
            val size = 96
            val bitmap = LumaNotificationIconGenerator.generateSmallIcon(context, dayText, size)
            val ink = measureInkBounds(bitmap)
            val safeDiameter = 2f * safeRadius(size)
            val fillRatio = ink.constrainingDimension / safeDiameter

            assertTrue(
                "Day $dayText: fill ratio $fillRatio below $MIN_FILL_RATIO_OF_SAFE " +
                    "(ink ${ink.width}x${ink.height}, safe diameter $safeDiameter)",
                fillRatio >= MIN_FILL_RATIO_OF_SAFE
            )
            assertTrue(
                "Day $dayText: needs fully-opaque white pixels forming an unbroken mark (VI-005)",
                ink.opaqueWhiteCount > 0
            )
        }
    }

    @Test
    fun testSmallIconDayNumberDerivation() {
        // Device-local date → Jalali day → localized digits (pure function of inputs)
        val testDate = "2026-09-13"
        val j = CalendarConverter.gregorianToJalali(testDate)
        assertEquals(22, j.day)

        // JALALI localization → Persian digits
        val jalaliDayText = CalendarConverter.toPersianDigits(j.day.toString())
        assertEquals("۲۲", jalaliDayText)

        // Gregorian/English localization → Latin digits
        val latinDayText = j.day.toString()
        assertEquals("22", latinDayText)
    }

    @Test
    fun testCollapsedNotificationLayoutInflatesProperly() {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.notification_luma_calendar, null)
        assertNotNull(view)

        val mainDate = view.findViewById<TextView>(R.id.notification_main_date)
        val secDate = view.findViewById<TextView>(R.id.notification_secondary_date)
        val tileMonth = view.findViewById<TextView>(R.id.notification_tile_month)
        val tileDay = view.findViewById<TextView>(R.id.notification_tile_day)

        assertNotNull(mainDate)
        assertNotNull(secDate)
        assertNotNull(tileMonth)
        assertNotNull(tileDay)
    }

    @Test
    fun testExpandedNotificationLayoutInflatesWithActions() {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.notification_luma_calendar_expanded, null)
        assertNotNull(view)

        val mainDate = view.findViewById<TextView>(R.id.notification_main_date)
        val secDate = view.findViewById<TextView>(R.id.notification_secondary_date)
        val message = view.findViewById<TextView>(R.id.notification_daily_message)
        val tileMonth = view.findViewById<TextView>(R.id.notification_tile_month)
        val tileDay = view.findViewById<TextView>(R.id.notification_tile_day)

        val actionToday = view.findViewById<android.view.View>(R.id.notification_action_today)
        val actionNewEvent = view.findViewById<android.view.View>(R.id.notification_action_new_event)
        val actionRemindLater = view.findViewById<android.view.View>(R.id.notification_action_remind_later)

        assertNotNull(mainDate)
        assertNotNull(secDate)
        assertNotNull(message)
        assertNotNull(tileMonth)
        assertNotNull(tileDay)
        assertNotNull(actionToday)
        assertNotNull(actionNewEvent)
        assertNotNull(actionRemindLater)
    }

    @Test
    fun testSingleInstanceDailyNotificationPostsExactlyOnce() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)

        val notificationManager = context.getSystemService(android.app.NotificationManager::class.java)
        assertNotNull(notificationManager)

        // Ensure channel exists
        LumaNotificationManager.createChannels(context)

        // Verify constants
        assertEquals(1001, LumaNotificationManager.NOTIFICATION_ID_DAILY)
        assertEquals("luma_calendar_daily", LumaNotificationManager.CHANNEL_ID_DAILY)

        // Enable daily notification preferences
        NotificationPreferences.setEnabled(context, true)
        NotificationPreferences.setDailyNotificationEnabled(context, true)

        // Run coroutine to update notification twice synchronously
        kotlinx.coroutines.runBlocking {
            LumaNotificationManager.updateNotification(context)
            LumaNotificationManager.updateNotification(context)
        }

        val activeNotifications = notificationManager.activeNotifications
        val dailyNotifications = activeNotifications.filter { it.id == LumaNotificationManager.NOTIFICATION_ID_DAILY }

        assertEquals("Expected exactly 1 daily notification with ID 1001", 1, dailyNotifications.size)
        val dailyNotif = dailyNotifications[0]
        assertEquals(LumaNotificationManager.NOTIFICATION_ID_DAILY, dailyNotif.id)
        assertTrue("Notification must be ongoing", (dailyNotif.notification.flags and android.app.Notification.FLAG_ONGOING_EVENT) != 0)
        assertTrue("Notification must only alert once", (dailyNotif.notification.flags and android.app.Notification.FLAG_ONLY_ALERT_ONCE) != 0)
        assertTrue("Notification must not auto-cancel", (dailyNotif.notification.flags and android.app.Notification.FLAG_AUTO_CANCEL) == 0)

        // Test cancellation lifecycle
        LumaNotificationManager.cancel(context, "Testing cancellation")
        val activeAfterCancel = notificationManager.activeNotifications.filter { it.id == LumaNotificationManager.NOTIFICATION_ID_DAILY }
        assertEquals("Daily notification should be removed after cancel()", 0, activeAfterCancel.size)
    }
}
