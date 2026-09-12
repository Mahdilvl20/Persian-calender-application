package com.aistudio.lumacalendar.vtxk.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class EventNotificationSchedulerTest {
    private val timeZone = TimeZone.getTimeZone("Asia/Tehran")

    private fun millis(value: String): Long = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply {
        isLenient = false
        timeZone = this@EventNotificationSchedulerTest.timeZone
    }.parse(value)!!.time

    @Test
    fun `calculates arbitrary-minute reminder in local timezone`() {
        val expected = millis("2026-09-12 01:17")

        assertEquals(
            expected,
            EventNotificationScheduler.calculateTriggerAtMillis("2026-09-12", "01:32", 15, timeZone)
        )
    }

    @Test
    fun `supports at-time and one-day reminders`() {
        val eventTime = millis("2026-09-12 09:00")
        val previousDay = millis("2026-09-11 09:00")

        assertEquals(eventTime, EventNotificationScheduler.calculateTriggerAtMillis("2026-09-12", "09:00", 0, timeZone))
        assertEquals(previousDay, EventNotificationScheduler.calculateTriggerAtMillis("2026-09-12", "09:00", 1440, timeZone))
    }

    @Test
    fun `rejects disabled invalid and past reminders`() {
        val now = millis("2026-09-12 10:00")

        assertNull(EventNotificationScheduler.calculateTriggerAtMillis("2026-09-12", "09:00", -1, timeZone))
        assertNull(EventNotificationScheduler.calculateTriggerAtMillis("2026-02-30", "09:00", 15, timeZone))
        assertNull(EventNotificationScheduler.calculateTriggerAtMillis("2026-09-12", "25:00", 15, timeZone))
        assertNull(EventNotificationScheduler.calculateFutureTriggerAtMillis("2026-09-12", "09:00", 0, now, timeZone))
    }
}
