package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.holiday.HolidayService
import com.example.util.CalendarType
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * LumaCalendarWidgetProvider:
 * Guaranteed, reliable dynamic home screen widget that updates to reflect
 * the current day of month (1..31), weekday, month name, and official holidays/events.
 * Provides the rock-solid fallback on Android where launcher activity-alias switching
 * may be limited by third-party launchers or manufacturer power policies.
 */
class LumaCalendarWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_DATE_CHANGED ||
            intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED ||
            intent.action == ACTION_UPDATE_WIDGET
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, LumaCalendarWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (id in allWidgetIds) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.example.widget.UPDATE_LUMA_WIDGET"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_luma_calendar)
            val now = LocalDate.now()

            val dayNumber = now.dayOfMonth
            val monthName = now.month.getDisplayName(TextStyle.FULL, Locale.getDefault()).uppercase()
            val weekdayName = now.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())

            // Single digit days (1..9) and double digit days (10..31) format cleanly
            views.setTextViewText(R.id.widget_day_number, dayNumber.toString())
            views.setTextViewText(R.id.widget_month_text, monthName)
            views.setTextViewText(R.id.widget_weekday_text, weekdayName)

            // Check if today is a holiday
            val todayStr = String.format("%04d-%02d-%02d", now.year, now.monthValue, now.dayOfMonth)
            val holiday = HolidayService.default.getHoliday(todayStr, CalendarType.GREGORIAN)

            if (holiday != null) {
                views.setTextViewText(R.id.widget_event_text, "★ ${holiday.name}")
                views.setTextColor(R.id.widget_event_text, Color.parseColor("#FF453A"))
                views.setTextColor(R.id.widget_day_number, Color.parseColor("#FF453A"))
            } else {
                views.setTextViewText(R.id.widget_event_text, "Luma Calendar")
                views.setTextColor(R.id.widget_event_text, Color.parseColor("#38BDF8"))
                views.setTextColor(R.id.widget_day_number, Color.WHITE)
            }

            // Clicking widget launches MainActivity
            val clickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val thisWidget = ComponentName(context, LumaCalendarWidgetProvider::class.java)
                val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                for (id in allWidgetIds) {
                    updateAppWidget(context, appWidgetManager, id)
                }
            } catch (e: Exception) {
                // Ignore widget update failures
            }
        }
    }
}
