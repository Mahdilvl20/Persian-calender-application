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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aistudio.lumacalendar.vtxk.MainActivity
import com.aistudio.lumacalendar.vtxk.R
import com.aistudio.lumacalendar.vtxk.data.CalendarEvent
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object NotificationPreferences {
    private const val FILE_NAME = "notification_preferences"
    private const val KEY_ENABLED = "event_reminders_enabled"
    private const val KEY_PERMISSION_REQUESTED = "notification_permission_requested"

    fun isEnabled(context: Context): Boolean = preferences(context).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        preferences(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
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
    private const val CHANNEL_ID = "event_reminders"

    fun hasNotificationPermission(context: Context): Boolean {
        val runtimePermissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        return runtimePermissionGranted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun calculateTriggerAtMillis(
        date: String,
        startTime: String,
        reminderMinutes: Int,
        timeZone: TimeZone = TimeZone.getDefault()
    ): Long? {
        if (reminderMinutes < 0) return null
        val normalizedTime = com.aistudio.lumacalendar.vtxk.util.TimeValidator.normalizeDigits(startTime).trim()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply {
            isLenient = false
            this.timeZone = timeZone
        }
        return try {
            formatter.parse("$date $normalizedTime")?.time?.minus(reminderMinutes * 60_000L)
        } catch (_: ParseException) {
            null
        }
    }

    fun calculateFutureTriggerAtMillis(
        date: String,
        startTime: String,
        reminderMinutes: Int,
        nowMillis: Long,
        timeZone: TimeZone = TimeZone.getDefault()
    ): Long? = calculateTriggerAtMillis(date, startTime, reminderMinutes, timeZone)
        ?.takeIf { it > nowMillis }

    fun schedule(context: Context, event: CalendarEvent, nowMillis: Long = System.currentTimeMillis()): Boolean {
        cancel(context, event.id)
        if (event.id <= 0 || !NotificationPreferences.isEnabled(context)) return false
        val triggerAt = calculateFutureTriggerAtMillis(
            event.date,
            event.startTime,
            event.reminderMinutes,
            nowMillis
        ) ?: return false
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = alarmPendingIntent(context, event.id, PendingIntent.FLAG_UPDATE_CURRENT)
            ?: return false
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
        return true
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
    }

    fun cancelAll(context: Context, events: Iterable<CalendarEvent>) {
        events.forEach { cancel(context, it.id) }
    }

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
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
    }

    fun show(context: Context, eventId: Long, title: String, time: String, location: String, notes: String) {
        if (!NotificationPreferences.isEnabled(context) || !hasNotificationPermission(context)) return
        createChannel(context)
        val contentText = when {
            location.isNotBlank() -> context.getString(R.string.notification_at_location, time, location)
            else -> context.getString(R.string.notification_at_time, time)
        }
        val expandedText = buildString {
            append(contentText)
            if (notes.isNotBlank()) append("\n").append(notes)
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            requestCode(eventId),
            Intent(context, MainActivity::class.java).apply {
                action = "${context.packageName}.OPEN_EVENT"
                data = Uri.parse("luma://event/$eventId")
                putExtra(EXTRA_EVENT_ID, eventId)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_luma)
            .setColor(ContextCompat.getColor(context, R.color.notification_accent))
            .setContentTitle(title)
            .setContentText(contentText)
            .setSubText(context.getString(R.string.app_name))
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setContentIntent(contentIntent)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(requestCode(eventId), notification)
        } catch (_: SecurityException) {
            // Permission may have been revoked between the check and notify call.
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

    private fun requestCode(eventId: Long): Int = (eventId xor (eventId ushr 32)).toInt()
}
