package com.aistudio.lumacalendar.vtxk.notification

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aistudio.lumacalendar.vtxk.data.LumaDatabase
import com.aistudio.lumacalendar.vtxk.util.DynamicIconManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class EventReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getLongExtra(EventNotificationScheduler.EXTRA_EVENT_ID, -1L)
        if (eventId <= 0 || !NotificationPreferences.isEnabled(context)) return
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
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
        )
        if (intent.action !in validActions) return
        DynamicIconManager.updateLiveCalendarShortcutAsync(context)
        if (!NotificationPreferences.isEnabled(context)) return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val events = LumaDatabase.getDatabase(
                    context,
                    CoroutineScope(SupervisorJob() + Dispatchers.IO)
                ).eventDao().getAllEventsSnapshot()
                events.forEach { EventNotificationScheduler.schedule(context, it) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
