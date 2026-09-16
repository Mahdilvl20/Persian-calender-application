package com.aistudio.lumacalendar.vtxk.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aistudio.lumacalendar.vtxk.MainActivity
import com.aistudio.lumacalendar.vtxk.R
import com.aistudio.lumacalendar.vtxk.data.CalendarEvent
import com.aistudio.lumacalendar.vtxk.data.LumaDatabase
import com.aistudio.lumacalendar.vtxk.util.CalendarConverter
import com.aistudio.lumacalendar.vtxk.util.CalendarType
import com.aistudio.lumacalendar.vtxk.util.DateUtils
import com.aistudio.lumacalendar.vtxk.util.LocalizationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime

/**
 * Manages the permanent daily calendar notification for Luma Calendar.
 * 
 * Features:
 * - Real-world local device date (never UTC or stale cached dates).
 * - High-end dark translucent glassmorphism design with frosted effect.
 * - Dynamic app icon with deep blue -> violet glass gradient and actual current day number.
 * - Prominent Persian date in Vazirmatn typography with proper RTL support.
 * - Secondary Gregorian & Hijri dates referencing the exact same day.
 * - Contextual daily message.
 * - Compact miniature calendar tile on the right (month name + large day number).
 * - Three subtle glass action buttons in expanded view: Today, New Event, Remind Later.
 * - Automatic dynamic midnight update without creating extra activities or tasks.
 */
object LumaNotificationManager {

    private const val TAG = "LumaDailyNotification"

    const val CHANNEL_ID_DAILY = "luma_calendar_daily"
    const val NOTIFICATION_ID_DAILY = 1001

    const val ACTION_TODAY = "com.aistudio.lumacalendar.vtxk.ACTION_TODAY"
    const val ACTION_NEW_EVENT = "com.aistudio.lumacalendar.vtxk.ACTION_NEW_EVENT"
    const val ACTION_REMIND_LATER = "com.aistudio.lumacalendar.vtxk.ACTION_REMIND_LATER"

    private const val REQUEST_CODE_CONTENT = 1010
    private const val REQUEST_CODE_TODAY = 1011
    private const val REQUEST_CODE_NEW_EVENT = 1012
    private const val REQUEST_CODE_REMIND_LATER = 1013
    const val REQUEST_CODE_MIDNIGHT = 1014
    const val REQUEST_CODE_SNOOZE = 1015

    private val notificationMutex = Mutex()

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val existing = manager.getNotificationChannel(CHANNEL_ID_DAILY)
        if (existing != null) {
            Log.d(TAG, "createChannels: Channel $CHANNEL_ID_DAILY already exists (importance=${existing.importance})")
            return
        }

        val dailyChannel = NotificationChannel(
            CHANNEL_ID_DAILY,
            context.getString(R.string.notification_daily_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_daily_channel_description)
            enableLights(true)
            lightColor = Color.rgb(99, 102, 241) // #6366F1
            setShowBadge(true)
        }
        manager.createNotificationChannel(dailyChannel)
        Log.d(TAG, "createChannels: Created channel $CHANNEL_ID_DAILY with IMPORTANCE_DEFAULT")
    }

    /**
     * Asynchronously updates the notification using the current real device date.
     */
    fun updateNotificationAsync(context: Context, customMessage: String? = null) {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                updateNotification(context, customMessage)
            } catch (e: Exception) {
                Log.e(TAG, "updateNotificationAsync failed: ${e.message}", e)
            }
        }
    }

    /**
     * Builds and posts the persistent Luma Calendar notification.
     */
    suspend fun updateNotification(context: Context, customMessage: String? = null) = withContext(Dispatchers.IO) {
        notificationMutex.withLock {
            val areNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
            val hasRuntimePermission = EventNotificationScheduler.hasNotificationPermission(context)
            val isDailyPrefEnabled = NotificationPreferences.isDailyNotificationEnabled(context)

            Log.d(TAG, "updateNotification() called: systemEnabled=$areNotificationsEnabled, runtimePermission=$hasRuntimePermission, dailyPrefEnabled=$isDailyPrefEnabled")

            if (!isDailyPrefEnabled) {
                Log.w(TAG, "updateNotification() SKIPPED: Daily notification is disabled in preferences")
                return@withLock
            }

            if (!hasRuntimePermission) {
                Log.w(TAG, "updateNotification() SKIPPED: POST_NOTIFICATIONS permission not granted")
                return@withLock
            }

            createChannels(context)

            // Verify channel state
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(NotificationManager::class.java)
                val channel = manager?.getNotificationChannel(CHANNEL_ID_DAILY)
                Log.d(TAG, "Channel check: exists=${channel != null}, importance=${channel?.importance}")
            }

            // 1. Resolve ACTUAL current device local date (e.g. "2026-09-13")
            val todayDateStr = DateUtils.getRealDeviceDate()
            val calendarType = NotificationPreferences.getCalendarType(context)
            val isRtl = LocalizationManager.isRtl(calendarType)

            // 2. Fetch today's scheduled events from local Room database
            val eventsToday: List<CalendarEvent> = try {
                val db = LumaDatabase.getDatabase(context, CoroutineScope(SupervisorJob() + Dispatchers.IO))
                db.eventDao().getEventsForDateSnapshot(todayDateStr)
            } catch (e: Exception) {
                Log.w(TAG, "Could not load today events: ${e.message}")
                emptyList<CalendarEvent>()
            }

            // 3. Compute Dates referencing the EXACT SAME real-world date
            // Persian / Jalali components
            val j = CalendarConverter.gregorianToJalali(todayDateStr)
            val jWeekday = CalendarConverter.getWeekdayName(todayDateStr, CalendarType.JALALI)
            val jDayPersian = CalendarConverter.toPersianDigits(j.day.toString())
            val jMonthName = CalendarConverter.getMonthName(j.month, CalendarType.JALALI)
            val jYearPersian = CalendarConverter.toPersianDigits(j.year.toString())
            val persianFullDate = "$jWeekday $jDayPersian $jMonthName $jYearPersian"

            // Gregorian components
            val g = CalendarConverter.parseGregorianString(todayDateStr)
            val gWeekday = CalendarConverter.getWeekdayName(todayDateStr, CalendarType.GREGORIAN)
            val gMonthName = CalendarConverter.getMonthName(g.month, CalendarType.GREGORIAN)
            val gregorianDate = "${g.day} $gMonthName ${g.year}"
            val gregorianFullDate = "$gWeekday, $gMonthName ${g.day}, ${g.year}"

            // Hijri components
            val h = CalendarConverter.gregorianToHijri(todayDateStr)
            val hMonthName = CalendarConverter.getMonthName(h.month, CalendarType.HIJRI)
            val hDayPersian = CalendarConverter.toPersianDigits(h.day.toString())
            val hYearPersian = CalendarConverter.toPersianDigits(h.year.toString())
            val hijriDate = "$hDayPersian $hMonthName $hYearPersian"

            val appStrings = LocalizationManager.getStrings(calendarType)

            // 4. Determine display content based on active calendar
            val mainDateText: String
            val secondaryDateText: String
            val tileMonthText: String
            val tileDayText: String
            val iconDayText: String
            val dailyMessageText: String

            val actionTodayText = appStrings.today
            val actionNewEventText = appStrings.newEvent
            val actionRemindLaterText = if (isRtl) "یادآوری بعداً" else "Remind Later"

            when (calendarType) {
                CalendarType.GREGORIAN -> {
                    mainDateText = gregorianFullDate
                    secondaryDateText = "$persianFullDate  •  $hijriDate"
                    tileMonthText = gMonthName.take(3).uppercase()
                    tileDayText = g.day.toString()
                    iconDayText = g.day.toString()
                    dailyMessageText = customMessage ?: when {
                        eventsToday.isNotEmpty() -> "✨ ${eventsToday.size} event${if (eventsToday.size > 1) "s" else ""} scheduled for today"
                        else -> appStrings.notificationDailyMsgEmpty
                    }
                }
                CalendarType.HIJRI -> {
                    mainDateText = "$jWeekday $hijriDate"
                    secondaryDateText = "$persianFullDate  •  $gregorianDate"
                    tileMonthText = hMonthName
                    tileDayText = hDayPersian
                    iconDayText = hDayPersian
                    dailyMessageText = customMessage ?: when {
                        eventsToday.isNotEmpty() -> {
                            val count = CalendarConverter.toPersianDigits(eventsToday.size.toString())
                            "✨ $count رویداد برای امروز ثبت شده است"
                        }
                        else -> "✨ امروز روز پر برکتی برای شما باشد"
                    }
                }
                CalendarType.JALALI -> {
                    mainDateText = persianFullDate
                    secondaryDateText = "$gregorianDate  •  $hijriDate"
                    tileMonthText = jMonthName
                    tileDayText = jDayPersian
                    iconDayText = jDayPersian
                    dailyMessageText = customMessage ?: when {
                        eventsToday.isNotEmpty() -> {
                            val count = CalendarConverter.toPersianDigits(eventsToday.size.toString())
                            "✨ $count رویداد برای امروز ثبت شده است"
                        }
                        else -> appStrings.notificationDailyMsgEmpty
                    }
                }
            }

            Log.d(TAG, "Display data resolved: mainDate='$mainDateText', secondary='$secondaryDateText', iconDay='$iconDayText'")

            // 5. Generate Dynamic App Icon with current day number & glass gradient
            val appIconBitmap = LumaNotificationIconGenerator.generateIcon(
                context = context,
                dayText = iconDayText,
                sizePx = 120
            )

            // 6. Build RemoteViews for Collapsed and Expanded notifications
            val collapsedViews = RemoteViews(context.packageName, R.layout.notification_luma_calendar).apply {
                setImageViewBitmap(R.id.notification_app_icon, appIconBitmap)
                setTextViewText(R.id.notification_main_date, mainDateText)
                setTextViewText(R.id.notification_secondary_date, secondaryDateText)
                setTextViewText(R.id.notification_tile_month, tileMonthText)
                setTextViewText(R.id.notification_tile_day, tileDayText)
            }

            val expandedViews = RemoteViews(context.packageName, R.layout.notification_luma_calendar_expanded).apply {
                setTextViewText(R.id.notification_main_date, mainDateText)
                setTextViewText(R.id.notification_secondary_date, secondaryDateText)
                setTextViewText(R.id.notification_daily_message, dailyMessageText)
                setTextViewText(R.id.notification_tile_month, tileMonthText)
                setTextViewText(R.id.notification_tile_day, tileDayText)

                setTextViewText(R.id.notification_action_today_label, actionTodayText)
                setTextViewText(R.id.notification_action_new_event_label, actionNewEventText)
                setTextViewText(R.id.notification_action_remind_later_label, actionRemindLaterText)
            }

            // 7. Setup Action PendingIntents
            val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
                action = "${context.packageName}.OPEN_TODAY"
                data = Uri.parse("luma://calendar/today")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val contentPendingIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE_CONTENT,
                mainActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val todayIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_TODAY
                putExtra("EXTRA_ACTION", "TODAY")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val todayPendingIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE_TODAY,
                todayIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val newEventIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_NEW_EVENT
                putExtra("EXTRA_ACTION", "NEW_EVENT")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val newEventPendingIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE_NEW_EVENT,
                newEventIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val remindLaterIntent = Intent(context, LumaNotificationActionReceiver::class.java).apply {
                action = ACTION_REMIND_LATER
            }
            val remindLaterPendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_REMIND_LATER,
                remindLaterIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Bind clicks to expanded RemoteViews controls
            expandedViews.setOnClickPendingIntent(R.id.notification_action_today, todayPendingIntent)
            expandedViews.setOnClickPendingIntent(R.id.notification_action_new_event, newEventPendingIntent)
            expandedViews.setOnClickPendingIntent(R.id.notification_action_remind_later, remindLaterPendingIntent)

            // 8. Assemble Notification (NO duplicate subText, NO duplicate OS action buttons)
            val notification = NotificationCompat.Builder(context, CHANNEL_ID_DAILY)
                .setSmallIcon(R.drawable.ic_notification_luma)
                .setColor(ContextCompat.getColor(context, R.color.notification_accent))
                .setCustomContentView(collapsedViews)
                .setCustomBigContentView(expandedViews)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setContentIntent(contentPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .build()

            try {
                Log.d(TAG, "Calling NotificationManagerCompat.notify($NOTIFICATION_ID_DAILY)")
                NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_DAILY, notification)
                Log.d(TAG, "notify($NOTIFICATION_ID_DAILY) COMPLETE successfully")
            } catch (e: Exception) {
                Log.e(TAG, "ERROR calling notify($NOTIFICATION_ID_DAILY): ${e.message}", e)
            }
        }
    }

    /**
     * Schedules an alarm to update the notification right after midnight in the local timezone.
     */
    fun scheduleMidnightUpdate(context: Context) {
        if (!NotificationPreferences.isDailyNotificationEnabled(context)) {
            Log.d(TAG, "scheduleMidnightUpdate() SKIPPED: Daily notification disabled")
            cancelMidnightUpdate(context)
            return
        }

        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val zone = DateUtils.getDeviceZoneId()
        val now = ZonedDateTime.now(zone)
        val midnight = now.toLocalDate().plusDays(1).atStartOfDay(zone)
        val triggerMillis = midnight.toInstant().toEpochMilli() + 2000L // 2 seconds after midnight

        Log.d(TAG, "scheduleMidnightUpdate() scheduled at triggerMillis=$triggerMillis ($midnight in zone=$zone)")

        val intent = Intent(context, MidnightUpdateReceiver::class.java).apply {
            action = "${context.packageName}.MIDNIGHT_UPDATE"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_MIDNIGHT,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
            Log.d(TAG, "Midnight update alarm set successfully")
        } catch (e: Exception) {
            Log.w(TAG, "Exact alarm failed for midnight update, falling back: ${e.message}")
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        }
    }

    /**
     * Cancels any pending midnight update alarm.
     */
    fun cancelMidnightUpdate(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, MidnightUpdateReceiver::class.java).apply {
            action = "${context.packageName}.MIDNIGHT_UPDATE"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_MIDNIGHT,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Midnight update alarm cancelled")
        }
    }

    /**
     * Cancels any pending snooze / remind later alarm.
     */
    fun cancelSnoozeAlarm(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val wakeupIntent = Intent(context, MidnightUpdateReceiver::class.java).apply {
            action = "${context.packageName}.SNOOZE_WAKEUP"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_SNOOZE,
            wakeupIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Snooze alarm cancelled")
        }
    }

    fun cancel(context: Context, reason: String = "manual") {
        Log.d(TAG, "cancel() called with reason='$reason'")
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_DAILY)
        cancelMidnightUpdate(context)
        cancelSnoozeAlarm(context)
    }
}
