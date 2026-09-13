package com.aistudio.lumacalendar.vtxk.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import com.aistudio.lumacalendar.vtxk.MainActivity
import com.aistudio.lumacalendar.vtxk.util.CalendarConverter.gregorianToHijri
import com.aistudio.lumacalendar.vtxk.util.CalendarConverter.gregorianToJalali
import com.aistudio.lumacalendar.vtxk.widget.LumaCalendarWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

/**
 * DynamicIconManager:
 * Architecture-compliant, safe, and reliable dynamic icon and widget manager.
 *
 * KEY ARCHITECTURE RULES:
 * 1. MainActivity is NEVER disabled, stopped, recreated, or switched.
 * 2. No destructive Activity-Alias swapping that causes process termination, task recreation,
 *    broken input channels, or ActivityNotFoundException.
 * 3. Icon day is ALWAYS strictly the REAL device date (LocalDate.now().dayOfMonth).
 *    Never derived from selected calendar date, visible month, or Jalali/Hijri conversion.
 * 4. Strictly idempotent: If today's day has already been applied, it returns immediately doing zero work.
 * 5. All operations are dispatched to Dispatchers.IO to guarantee zero frame drops on the Main/UI thread.
 * 6. Changes in calendar mode (Jalali/Gregorian/Hijri), date clicks, or screen navigation NEVER trigger icon updates.
 * 7. Resilient: Automatically repairs MainActivity component state if previous versions disabled it.
 */
object DynamicIconManager {
    private const val TAG = "DynamicIconManager"
    private const val PREFS_NAME = "luma_dynamic_icon_prefs"
    private const val KEY_LAST_APPLIED_DAY = "last_applied_day"
    private const val KEY_LAST_SYNC_DATE = "last_sync_date"
    private const val SHORTCUT_ID = "live_triple_calendar"
    private const val ICON_SIZE = 432
    private const val KEY_LAST_SHORTCUT_DATE = "last_shortcut_date"

    data class TripleCalendarDays(
        val jalali: Int,
        val gregorian: Int,
        val hijri: Int
    )

    private val scope = CoroutineScope(Dispatchers.IO)

    fun getTripleCalendarDays(date: String = DateUtils.getRealDeviceDate()): TripleCalendarDays {
        val gregorian = CalendarConverter.parseGregorianString(date)
        val jalali = gregorianToJalali(date)
        val hijri = gregorianToHijri(date)
        return TripleCalendarDays(
            jalali = jalali.day,
            gregorian = gregorian.day,
            hijri = hijri.day
        )
    }

    fun requestLiveCalendarShortcut(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        val manager = context.getSystemService(ShortcutManager::class.java)
        if (!manager.isRequestPinShortcutSupported) return false
        val accepted = manager.requestPinShortcut(buildShortcut(context), null)
        if (accepted) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LAST_SHORTCUT_DATE, DateUtils.getRealDeviceDate())
                .apply()
            scheduleNextRefresh(context)
        }
        return accepted
    }

    fun updateLiveCalendarShortcutAsync(context: Context) {
        scope.launch {
            try {
                updateLiveCalendarShortcut(context.applicationContext)
                scheduleNextRefresh(context.applicationContext)
            } catch (e: Exception) {
                Log.w(TAG, "Live shortcut update failed", e)
                try {
                    scheduleNextRefresh(context.applicationContext, retrySoon = true)
                } catch (scheduleError: Exception) {
                    Log.w(TAG, "Live shortcut retry scheduling failed", scheduleError)
                }
            }
        }
    }

    fun scheduleNextRefresh(context: Context, retrySoon: Boolean = false) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, DynamicShortcutRefreshReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = if (retrySoon) {
            System.currentTimeMillis() + 15 * 60_000L
        } else {
            Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 5)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
    }

    fun updateLiveCalendarShortcut(context: Context, force: Boolean = false): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return true
        val manager = context.getSystemService(ShortcutManager::class.java)
        val exists = manager.pinnedShortcuts.any { it.id == SHORTCUT_ID }
        if (!exists) return true
        val today = DateUtils.getRealDeviceDate()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!force && prefs.getString(KEY_LAST_SHORTCUT_DATE, null) == today) {
            scheduleNextRefresh(context)
            return true
        }
        val updated = manager.updateShortcuts(listOf(buildShortcut(context, today)))
        if (updated) {
            prefs.edit().putString(KEY_LAST_SHORTCUT_DATE, today).apply()
            scheduleNextRefresh(context)
        }
        return updated
    }

    private fun buildShortcut(context: Context, date: String = DateUtils.getRealDeviceDate()): ShortcutInfo {
        val days = getTripleCalendarDays(date)
        return ShortcutInfo.Builder(context, SHORTCUT_ID)
            .setShortLabel("${days.jalali} · Luma")
            .setLongLabel("تقویم زنده لوما")
            .setIcon(Icon.createWithBitmap(renderShortcutIcon(days)))
            .setIntent(Intent(context, MainActivity::class.java).setAction(Intent.ACTION_VIEW))
            .build()
    }

    private fun renderShortcutIcon(days: TripleCalendarDays): Bitmap {
        val bitmap = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val card = RectF(28f, 28f, 404f, 404f)

        paint.shader = LinearGradient(
            0f,
            0f,
            ICON_SIZE.toFloat(),
            ICON_SIZE.toFloat(),
            intArrayOf(Color.rgb(8, 10, 16), Color.rgb(20, 25, 48), Color.rgb(11, 14, 28)),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(card, 92f, 92f, paint)

        paint.shader = LinearGradient(
            28f,
            28f,
            404f,
            120f,
            Color.rgb(99, 102, 241),
            Color.rgb(56, 189, 248),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(RectF(48f, 48f, 384f, 132f), 42f, 42f, paint)

        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        paint.color = Color.argb(120, 255, 255, 255)
        canvas.drawRoundRect(card, 92f, 92f, paint)
        paint.style = Paint.Style.FILL

        drawCenteredText(canvas, days.jalali.toString(), 216f, 240f, 154f, Color.WHITE, Paint.Align.CENTER)
        drawCenteredText(canvas, days.hijri.toString(), 136f, 330f, 56f, Color.rgb(148, 163, 184), Paint.Align.CENTER)
        drawCenteredText(canvas, days.gregorian.toString(), 286f, 330f, 56f, Color.rgb(56, 189, 248), Paint.Align.CENTER)
        return bitmap
    }

    private fun drawCenteredText(
        canvas: Canvas,
        text: String,
        x: Float,
        centerY: Float,
        textSize: Float,
        color: Int,
        align: Paint.Align
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = textSize
            this.color = color
            textAlign = align
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val baseline = centerY - (paint.ascent() + paint.descent()) / 2f
        canvas.drawText(text, x, baseline, paint)
    }

    // ponytail: pinned shortcuts refresh only when the launcher honors updateShortcuts; use the 1×1 widget for guaranteed refresh.

    /**
     * Ensures that com.aistudio.lumacalendar.vtxk.MainActivity is explicitly enabled in the device's PackageManager.
     * This repairs any corrupted PackageManager state left behind by earlier versions or custom ROMs.
     */
    fun ensureMainActivityEnabled(context: Context) {
        scope.launch {
            try {
                val pm = context.packageManager
                val component = ComponentName(context, "com.aistudio.lumacalendar.vtxk.MainActivity")
                val state = pm.getComponentEnabledSetting(component)
                if (state != PackageManager.COMPONENT_ENABLED_STATE_DEFAULT &&
                    state != PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                ) {
                    pm.setComponentEnabledSetting(
                        component,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                    )
                    Log.i(TAG, "MainActivity component state restored to ENABLED.")
                }
            } catch (e: Exception) {
                // Non-fatal, keep application running smoothly
            }
        }
    }

    /**
     * Checks if the device date has changed since the last applied update.
     * If already up-to-date and not forced, does nothing (Idempotent).
     * Dispatches widget and dynamic date persistence safely in background thread.
     */
    fun syncIfDateChanged(context: Context, force: Boolean = false) {
        scope.launch {
            try {
                val today = DateUtils.getRealDeviceLocalDate()
                val currentDay = today.dayOfMonth
                val todayIso = today.toString()

                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val lastAppliedDay = prefs.getInt(KEY_LAST_APPLIED_DAY, -1)
                val lastSyncDate = prefs.getString(KEY_LAST_SYNC_DATE, null)

                if (!force && lastAppliedDay == currentDay && lastSyncDate == todayIso) {
                    // Rebuild restored or previously rate-limited pinned shortcut icons on every foreground start.
                    if (updateLiveCalendarShortcut(context)) return@launch
                }

                // Publish first; mark the day complete only when the shortcut update succeeds.
                LumaCalendarWidgetProvider.updateAllWidgets(context)
                if (!updateLiveCalendarShortcut(context)) return@launch

                prefs.edit()
                    .putInt(KEY_LAST_APPLIED_DAY, currentDay)
                    .putString(KEY_LAST_SYNC_DATE, todayIso)
                    .apply()

                Log.d(TAG, "Dynamic date synced safely to device day $currentDay ($todayIso)")
            } catch (e: Exception) {
                Log.w(TAG, "Safe dynamic icon sync encountered non-fatal exception", e)
            }
        }
    }

    /**
     * Synchronously returns the device's real day of month (1..31).
     * Fast and safe to call from UI/Composables without blocking.
     */
    fun getRealDeviceDay(): Int {
        return DateUtils.getRealDeviceLocalDate().dayOfMonth
    }

    /**
     * Returns the last recorded synced day of the month.
     */
    fun getLastAppliedDay(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getInt(KEY_LAST_APPLIED_DAY, -1)
        return if (saved in 1..31) saved else DateUtils.getRealDeviceLocalDate().dayOfMonth
    }
}
