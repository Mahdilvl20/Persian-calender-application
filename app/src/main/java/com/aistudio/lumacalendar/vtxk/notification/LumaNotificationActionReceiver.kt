package com.aistudio.lumacalendar.vtxk.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.aistudio.lumacalendar.vtxk.util.LocalizationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Handles action callbacks from the Luma Calendar notification, such as "Remind Later",
 * as well as individual event reminder snooze callbacks.
 */
class LumaNotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d("LumaDailyNotification", "LumaNotificationActionReceiver.onReceive: action=$action")

        if (action == LumaNotificationManager.ACTION_REMIND_LATER) {
            val pendingResult = goAsync()
            val calendarType = NotificationPreferences.getCalendarType(context)
            val isRtl = LocalizationManager.isRtl(calendarType)

            val snoozeMessage = if (isRtl) {
                "✨ یادآوری برای ۱ ساعت دیگر تنظیم شد"
            } else {
                "✨ Reminder snoozed for 1 hour"
            }

            try {
                Toast.makeText(context, snoozeMessage, Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {}

            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                try {
                    // Update notification with snooze acknowledgment
                    LumaNotificationManager.updateNotification(context, snoozeMessage)

                    // Schedule alarm to re-alert / update in 60 minutes
                    val alarmManager = context.getSystemService(AlarmManager::class.java)
                    if (alarmManager != null) {
                        val triggerAtMillis = SystemClock.elapsedRealtime() + (60 * 60 * 1000L)
                        val wakeupIntent = Intent(context, MidnightUpdateReceiver::class.java).apply {
                            this.action = "${context.packageName}.SNOOZE_WAKEUP"
                        }
                        val pi = PendingIntent.getBroadcast(
                            context,
                            LumaNotificationManager.REQUEST_CODE_SNOOZE,
                            wakeupIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        try {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
                                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pi)
                            } else {
                                alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pi)
                            }
                        } catch (_: SecurityException) {
                            alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pi)
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        } else if (action == EventNotificationScheduler.ACTION_SNOOZE_EVENT_REMINDER) {
            val eventId = intent.getLongExtra(EventNotificationScheduler.EXTRA_EVENT_ID, -1L)
            if (eventId > 0) {
                val notifId = EventNotificationScheduler.requestCode(eventId)
                NotificationManagerCompat.from(context).cancel(notifId)

                val calendarType = NotificationPreferences.getCalendarType(context)
                val isRtl = LocalizationManager.isRtl(calendarType)
                val toastMsg = if (isRtl) {
                    "✨ یادآوری برای ۱۰ دقیقه دیگر به تعویق افتاد"
                } else {
                    "✨ Reminder snoozed for 10 minutes"
                }
                try {
                    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                } catch (_: Exception) {}

                // Schedule alarm in 10 minutes
                val alarmManager = context.getSystemService(AlarmManager::class.java)
                if (alarmManager != null) {
                    val snoozeAlarmIntent = Intent(context, EventReminderReceiver::class.java).apply {
                        this.action = "${context.packageName}.EVENT_REMINDER"
                        this.data = Uri.parse("luma://alarm/$eventId")
                        putExtra(EventNotificationScheduler.EXTRA_EVENT_ID, eventId)
                    }
                    val pi = PendingIntent.getBroadcast(
                        context,
                        notifId,
                        snoozeAlarmIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    val triggerAt = System.currentTimeMillis() + (10 * 60 * 1000L)
                    try {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
                        } else {
                            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
                        }
                        Log.d("LumaEventReminder", "Event reminder snoozed for 10 minutes: eventId=$eventId, triggerAt=$triggerAt")
                    } catch (_: SecurityException) {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
                    }
                }
            }
        }
    }
}
