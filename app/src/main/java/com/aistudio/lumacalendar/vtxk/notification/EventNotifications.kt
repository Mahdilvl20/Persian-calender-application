package com.aistudio.lumacalendar.vtxk.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aistudio.lumacalendar.vtxk.MainActivity
import com.aistudio.lumacalendar.vtxk.R
import com.aistudio.lumacalendar.vtxk.data.CalendarEvent
import com.aistudio.lumacalendar.vtxk.util.DateUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object NotificationPreferences {
    private const val FILE_NAME = "notification_preferences"
    private const val KEY_DAILY_ENABLED = "daily_calendar_notification_enabled"
    private const val KEY_EVENT_REMINDERS_ENABLED = "event_reminders_enabled"
    private const val KEY_PERMISSION_REQUESTED = "notification_permission_requested"
    private const val KEY_CALENDAR_TYPE = "active_calendar_type"
    const val KEY_SNOOZE_MINUTES = "daily_notification_snooze_minutes"
    const val DEFAULT_SNOOZE_MINUTES = 60
    const val MIN_SNOOZE_MINUTES = 15
    const val MAX_SNOOZE_MINUTES = 480

    /**
     * Controls the snooze duration for the daily notification (in minutes).
     * Defaults to 60, range clamped between 15 and 480 minutes.
     */
    fun getSnoozeMinutes(context: Context): Int {
        val stored = preferences(context).getInt(KEY_SNOOZE_MINUTES, DEFAULT_SNOOZE_MINUTES)
        return stored.coerceIn(MIN_SNOOZE_MINUTES, MAX_SNOOZE_MINUTES)
    }

    fun setSnoozeMinutes(context: Context, minutes: Int) {
        val clamped = minutes.coerceIn(MIN_SNOOZE_MINUTES, MAX_SNOOZE_MINUTES)
        preferences(context).edit().putInt(KEY_SNOOZE_MINUTES, clamped).apply()
    }

    /**
     * Controls whether the permanent daily calendar notification is active.
     * Defaults to true so it stays visible whenever notifications are permitted.
     */
    fun isDailyNotificationEnabled(context: Context): Boolean =
        preferences(context).getBoolean(KEY_DAILY_ENABLED, true)

    fun setDailyNotificationEnabled(context: Context, enabled: Boolean) {
        preferences(context).edit().putBoolean(KEY_DAILY_ENABLED, enabled).apply()
    }

    /**
     * Controls whether individual event reminder alerts are active.
     * Defaults to true.
     */
    fun areEventRemindersEnabled(context: Context): Boolean =
        preferences(context).getBoolean(KEY_EVENT_REMINDERS_ENABLED, true)

    fun setEventRemindersEnabled(context: Context, enabled: Boolean) {
        preferences(context).edit().putBoolean(KEY_EVENT_REMINDERS_ENABLED, enabled).apply()
    }

    /**
     * Backward-compatible convenience check.
     */
    fun isEnabled(context: Context): Boolean =
        isDailyNotificationEnabled(context) || areEventRemindersEnabled(context)

    fun setEnabled(context: Context, enabled: Boolean) {
        preferences(context).edit()
            .putBoolean(KEY_DAILY_ENABLED, enabled)
            .putBoolean(KEY_EVENT_REMINDERS_ENABLED, enabled)
            .apply()
    }

    fun getCalendarType(context: Context): com.aistudio.lumacalendar.vtxk.util.CalendarType {
        val name = preferences(context).getString(KEY_CALENDAR_TYPE, com.aistudio.lumacalendar.vtxk.util.CalendarType.JALALI.name)
        return try {
            com.aistudio.lumacalendar.vtxk.util.CalendarType.valueOf(name ?: com.aistudio.lumacalendar.vtxk.util.CalendarType.JALALI.name)
        } catch (_: Exception) {
            com.aistudio.lumacalendar.vtxk.util.CalendarType.JALALI
        }
    }

    fun setCalendarType(context: Context, type: com.aistudio.lumacalendar.vtxk.util.CalendarType) {
        preferences(context).edit().putString(KEY_CALENDAR_TYPE, type.name).apply()
    }

    fun wasPermissionRequested(context: Context): Boolean =
        preferences(context).getBoolean(KEY_PERMISSION_REQUESTED, false)

    fun markPermissionRequested(context: Context) {
        preferences(context).edit().putBoolean(KEY_PERMISSION_REQUESTED, true).apply()
    }

    private fun preferences(context: Context) =
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
}

object EventNotificationScheduler {
    const val EXTRA_EVENT_ID = "event_id"
    const val ACTION_SNOOZE_EVENT_REMINDER = "com.aistudio.lumacalendar.vtxk.ACTION_SNOOZE_EVENT_REMINDER"
    private const val CHANNEL_ID = "event_reminders"
    private const val TAG = "LumaEventReminder"

    fun hasNotificationPermission(context: Context): Boolean {
        val runtimePermissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        val appNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        return runtimePermissionGranted && appNotificationsEnabled
    }

    fun calculateTriggerAtMillis(
        date: String,
        startTime: String,
        reminderMinutes: Int,
        timeZone: TimeZone = TimeZone.getTimeZone(DateUtils.getDeviceZoneId())
    ): Long? {
        if (reminderMinutes < 0) return null
        val normalizedTime = com.aistudio.lumacalendar.vtxk.util.TimeValidator.normalizeDigits(startTime).trim()
        val parts = normalizedTime.split(":")
        val paddedTime = if (parts.size >= 2) {
            "${parts[0].padStart(2, '0')}:${parts[1].padStart(2, '0')}"
        } else {
            normalizedTime
        }
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply {
            isLenient = false
            this.timeZone = timeZone
        }
        return try {
            formatter.parse("$date $paddedTime")?.time?.minus(reminderMinutes * 60_000L)
        } catch (e: ParseException) {
            try {
                Log.e(TAG, "calculateTriggerAtMillis: Failed to parse date='$date', time='$paddedTime'", e)
            } catch (_: Throwable) {}
            null
        } catch (e: Exception) {
            try {
                Log.e(TAG, "calculateTriggerAtMillis: Unexpected error for date='$date', time='$paddedTime'", e)
            } catch (_: Throwable) {}
            null
        }
    }

    fun calculateFutureTriggerAtMillis(
        date: String,
        startTime: String,
        reminderMinutes: Int,
        nowMillis: Long,
        timeZone: TimeZone = TimeZone.getTimeZone(DateUtils.getDeviceZoneId())
    ): Long? = calculateTriggerAtMillis(date, startTime, reminderMinutes, timeZone)
        ?.takeIf { it > nowMillis }

    fun schedule(context: Context, event: CalendarEvent, nowMillis: Long = System.currentTimeMillis()): Boolean {
        cancel(context, event.id)
        if (event.id <= 0) {
            Log.d(TAG, "schedule() SKIPPED: Invalid event id=${event.id}")
            return false
        }
        if (!NotificationPreferences.areEventRemindersEnabled(context)) {
            Log.d(TAG, "schedule() SKIPPED: Event reminders disabled in preferences for eventId=${event.id}")
            return false
        }
        val triggerAt = calculateFutureTriggerAtMillis(
            date = event.date,
            startTime = event.startTime,
            reminderMinutes = event.reminderMinutes,
            nowMillis = nowMillis
        )
        if (triggerAt == null) {
            val pastTrigger = calculateTriggerAtMillis(event.date, event.startTime, event.reminderMinutes)
            Log.d(TAG, "schedule() SKIPPED: Trigger time is null or in past (calculated=$pastTrigger, now=$nowMillis) for eventId=${event.id}")
            return false
        }

        Log.d(TAG, "EventNotification: scheduling eventId=${event.id} | eventStart=${event.date} ${event.startTime} | reminderMinutes=${event.reminderMinutes} | currentTime=$nowMillis | calculated trigger time=$triggerAt | actual alarm trigger time=$triggerAt")

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = alarmPendingIntent(context, event.id, PendingIntent.FLAG_UPDATE_CURRENT)
            ?: return false
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
            Log.d(TAG, "Alarm scheduled successfully for eventId=${event.id} at $triggerAt")
            return true
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm permission missing, falling back to setAndAllowWhileIdle for eventId=${event.id}")
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm for eventId=${event.id}", e)
            return false
        }
    }

    fun cancel(context: Context, eventId: Long) {
        if (eventId <= 0) return
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = alarmPendingIntent(context, eventId, PendingIntent.FLAG_NO_CREATE)
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
        NotificationManagerCompat.from(context).cancel(requestCode(eventId))
        Log.d(TAG, "Cancelled alarm and notification for eventId=$eventId")
    }

    fun cancelAll(context: Context, events: Iterable<CalendarEvent>) {
        events.forEach { cancel(context, it.id) }
    }

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null) {
            Log.d(TAG, "createChannel: Channel $CHANNEL_ID already exists (importance=${existing.importance})")
            return
        }
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_description)
            enableVibration(true)
            lightColor = Color.rgb(99, 102, 241)
            enableLights(true)
        }
        manager.createNotificationChannel(channel)
        Log.d(TAG, "Created channel $CHANNEL_ID with IMPORTANCE_HIGH")
    }

    fun show(context: Context, eventId: Long, title: String, time: String, location: String, notes: String) {
        if (!NotificationPreferences.areEventRemindersEnabled(context)) {
            Log.d(TAG, "show() SKIPPED: event reminders disabled in preferences")
            return
        }
        if (!hasNotificationPermission(context)) {
            Log.d(TAG, "show() SKIPPED: missing notification permission")
            return
        }

        createChannel(context)

        val notifId = requestCode(eventId)
        Log.d(TAG, "EventNotification: show() eventId=$eventId, notifId=$notifId, title='$title', time='$time', location='$location'")

        val contentText = if (location.isNotBlank()) "$time · $location" else time
        val expandedText = buildString {
            append(time)
            if (location.isNotBlank()) {
                append("\n📍 ").append(location)
            }
            if (notes.isNotBlank()) {
                append("\n\n").append(notes)
            }
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            notifId,
            Intent(context, MainActivity::class.java).apply {
                action = "${context.packageName}.OPEN_EVENT"
                data = Uri.parse("luma://event/$eventId")
                putExtra(EXTRA_EVENT_ID, eventId)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, LumaNotificationActionReceiver::class.java).apply {
            action = ACTION_SNOOZE_EVENT_REMINDER
            putExtra(EXTRA_EVENT_ID, eventId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notifId + 100000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_luma)
            .setColor(ContextCompat.getColor(context, R.color.notification_accent))
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setContentIntent(contentIntent)
            .addAction(
                R.drawable.ic_notification_action_today,
                context.getString(R.string.notification_action_open_event),
                contentIntent
            )
            .addAction(
                R.drawable.ic_notification_action_snooze,
                context.getString(R.string.notification_action_snooze_event),
                snoozePendingIntent
            )
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setOngoing(false)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notifId, notification)
            Log.d(TAG, "Event notification posted successfully: id=$notifId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post event notification id=$notifId", e)
        }
    }

    private fun alarmPendingIntent(
        context: Context,
        eventId: Long,
        flags: Int
    ): PendingIntent? = PendingIntent.getBroadcast(
        context,
        requestCode(eventId),
        Intent(context, EventReminderReceiver::class.java).apply {
            action = "${context.packageName}.EVENT_REMINDER"
            data = Uri.parse("luma://alarm/$eventId")
            putExtra(EXTRA_EVENT_ID, eventId)
        },
        flags or PendingIntent.FLAG_IMMUTABLE
    )

    fun requestCode(eventId: Long): Int {
        val hash = (eventId xor (eventId ushr 32)).toInt()
        // Ensure event notification / alarm ID never collides with NOTIFICATION_ID_DAILY (1001)
        return if (hash == LumaNotificationManager.NOTIFICATION_ID_DAILY) 1002 else hash
    }
}
