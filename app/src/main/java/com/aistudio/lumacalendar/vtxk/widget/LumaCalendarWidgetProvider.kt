package com.aistudio.lumacalendar.vtxk.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.aistudio.lumacalendar.vtxk.MainActivity
import com.aistudio.lumacalendar.vtxk.R
import com.aistudio.lumacalendar.vtxk.data.holiday.HolidayService
import com.aistudio.lumacalendar.vtxk.util.CalendarConverter
import com.aistudio.lumacalendar.vtxk.util.CalendarType
import com.aistudio.lumacalendar.vtxk.util.DateUtils
import com.aistudio.lumacalendar.vtxk.util.DynamicIconManager
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
        DynamicIconManager.updateLiveCalendarShortcutAsync(context)
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
            if (intent.action != ACTION_UPDATE_WIDGET) {
                DynamicIconManager.updateLiveCalendarShortcutAsync(context)
            }
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, LumaCalendarWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (id in allWidgetIds) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.aistudio.lumacalendar.vtxk.widget.UPDATE_LUMA_WIDGET"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_luma_calendar)
            val now = DateUtils.getRealDeviceLocalDate()
            val todayIsoStr = String.format(Locale.US, "%04d-%02d-%02d", now.year, now.monthValue, now.dayOfMonth)

            // Convert to Jalali and Hijri via canonical JDN pathway
            val jalaliDate = CalendarConverter.gregorianToJalali(todayIsoStr)
            val hijriDate = CalendarConverter.gregorianToHijri(todayIsoStr)

            // Level 1: Persian Month at Top
            val jalaliMonthName = CalendarConverter.getMonthName(jalaliDate.month, CalendarType.JALALI)
            views.setTextViewText(R.id.widget_month_text, jalaliMonthName)

            // Level 2: Dominant Jalali Day Number in Persian Digits
            val jalaliDayStr = CalendarConverter.toPersianDigits(jalaliDate.day.toString())
            views.setTextViewText(R.id.widget_day_number, jalaliDayStr)

            // Level 3: Balanced Secondary Calendars (Gregorian on Left, Hijri on Right)
            val gregorianDayStr = CalendarConverter.toPersianDigits(now.dayOfMonth.toString())
            val hijriDayStr = CalendarConverter.toPersianDigits(hijriDate.day.toString())
            views.setTextViewText(R.id.widget_secondary_left, gregorianDayStr)
            views.setTextViewText(R.id.widget_secondary_right, hijriDayStr)

            // Holiday check for Jalali calendar
            val holiday = HolidayService.default.getHoliday(todayIsoStr, CalendarType.JALALI)
            val isOfficialHoliday = holiday?.isOfficialHoliday == true

            if (isOfficialHoliday) {
                views.setTextColor(R.id.widget_day_number, Color.parseColor("#FF453A"))
            } else {
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
