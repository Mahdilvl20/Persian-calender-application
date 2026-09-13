package com.aistudio.lumacalendar.vtxk.notification

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.aistudio.lumacalendar.vtxk.data.LumaDatabase
import com.aistudio.lumacalendar.vtxk.util.DynamicIconManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class EventReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getLongExtra(EventNotificationScheduler.EXTRA_EVENT_ID, -1L)
        Log.d("EventNotification", "EventReminderReceiver.onReceive: eventId=$eventId, action=${intent.action}")
        if (eventId <= 0 || !NotificationPreferences.areEventRemindersEnabled(context)) {
            Log.d("EventNotification", "EventReminderReceiver: skipped because eventId=$eventId or reminders disabled")
            return
        }
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val event = LumaDatabase.getDatabase(
                    context,
                    CoroutineScope(SupervisorJob() + Dispatchers.IO)
                ).eventDao().getEventById(eventId)
                if (event != null) {
                    EventNotificationScheduler.show(
                        context = context,
                        eventId = event.id,
                        title = event.title,
                        time = event.startTime,
                        location = event.location,
                        notes = event.notes
                    )
                } else {
                    Log.w("EventNotification", "EventReminderReceiver: event not found for id=$eventId")
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

class ReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val validActions = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            "android.intent.action.TIME_SET",
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
        )
        if (intent.action !in validActions) return
        Log.d("LumaDailyNotification", "Triggered by ReminderRescheduleReceiver (action=${intent.action})")

        try {
            DynamicIconManager.updateLiveCalendarShortcutAsync(context)
        } catch (_: Exception) {}

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                if (NotificationPreferences.isDailyNotificationEnabled(context)) {
                    LumaNotificationManager.updateNotification(context)
                    LumaNotificationManager.scheduleMidnightUpdate(context)
                }
                if (NotificationPreferences.areEventRemindersEnabled(context)) {
                    val events = LumaDatabase.getDatabase(
                        context,
                        CoroutineScope(SupervisorJob() + Dispatchers.IO)
                    ).eventDao().getAllEventsSnapshot()
                    events.forEach { EventNotificationScheduler.schedule(context, it) }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
