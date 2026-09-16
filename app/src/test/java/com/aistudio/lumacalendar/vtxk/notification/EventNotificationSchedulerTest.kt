package com.aistudio.lumacalendar.vtxk.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
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

    @Test
    fun `verifies trigger-at-millis timing calculation for arbitrary reminder minutes`() {
        val eventDate = "2026-10-15"
        val eventTime = "14:30"
        val reminderMinutes = 45

        val expectedAlarmMillis = millis("2026-10-15 13:45")
        val calculatedMillis = EventNotificationScheduler.calculateTriggerAtMillis(
            date = eventDate,
            startTime = eventTime,
            reminderMinutes = reminderMinutes,
            timeZone = timeZone
        )

        assertEquals("Alarm trigger time must be exactly event time minus reminder minutes", expectedAlarmMillis, calculatedMillis)
    }

    @Test
    fun `requestCode produces unique int and never equals 1001`() {
        val eventId1 = 1L
        val eventId2 = 2L
        val eventIdCollision = 1001L

        val req1 = EventNotificationScheduler.requestCode(eventId1)
        val req2 = EventNotificationScheduler.requestCode(eventId2)
        val reqCollision = EventNotificationScheduler.requestCode(eventIdCollision)

        assert(req1 != req2) { "Different event IDs must produce different requestCodes" }
        assert(req1 != LumaNotificationManager.NOTIFICATION_ID_DAILY) { "req1 must not equal 1001" }
        assert(req2 != LumaNotificationManager.NOTIFICATION_ID_DAILY) { "req2 must not equal 1001" }
        assert(reqCollision != LumaNotificationManager.NOTIFICATION_ID_DAILY) { "Collision fallback must not equal 1001" }
        assertEquals(1002, reqCollision)
    }
}
