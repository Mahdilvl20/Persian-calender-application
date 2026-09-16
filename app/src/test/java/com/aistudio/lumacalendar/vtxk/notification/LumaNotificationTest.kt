package com.aistudio.lumacalendar.vtxk.notification

import android.Manifest
import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
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

@RunWith(RobolectricTestRunner::class)
class LumaNotificationTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

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
    fun testCollapsedNotificationLayoutInflatesProperly() {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.notification_luma_calendar, null)
        assertNotNull(view)

        val icon = view.findViewById<ImageView>(R.id.notification_app_icon)
        val headerTitle = view.findViewById<TextView>(R.id.notification_header_title)
        val headerTime = view.findViewById<TextView>(R.id.notification_header_time)
        val mainDate = view.findViewById<TextView>(R.id.notification_main_date)
        val secDate = view.findViewById<TextView>(R.id.notification_secondary_date)
        val tileMonth = view.findViewById<TextView>(R.id.notification_tile_month)
        val tileDay = view.findViewById<TextView>(R.id.notification_tile_day)

        assertNotNull(icon)
        assertNotNull(headerTitle)
        assertNotNull(headerTime)
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

        val icon = view.findViewById<ImageView>(R.id.notification_app_icon)
        val headerTitle = view.findViewById<TextView>(R.id.notification_header_title)
        val mainDate = view.findViewById<TextView>(R.id.notification_main_date)
        val secDate = view.findViewById<TextView>(R.id.notification_secondary_date)
        val message = view.findViewById<TextView>(R.id.notification_daily_message)
        val tileMonth = view.findViewById<TextView>(R.id.notification_tile_month)
        val tileDay = view.findViewById<TextView>(R.id.notification_tile_day)

        val actionToday = view.findViewById<android.view.View>(R.id.notification_action_today)
        val actionNewEvent = view.findViewById<android.view.View>(R.id.notification_action_new_event)
        val actionRemindLater = view.findViewById<android.view.View>(R.id.notification_action_remind_later)

        assertNotNull(icon)
        assertNotNull(headerTitle)
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
