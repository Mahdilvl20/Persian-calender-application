package com.example.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.example.widget.LumaCalendarWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

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

    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Ensures that com.example.MainActivity is explicitly enabled in the device's PackageManager.
     * This repairs any corrupted PackageManager state left behind by earlier versions or custom ROMs.
     */
    fun ensureMainActivityEnabled(context: Context) {
        scope.launch {
            try {
                val pm = context.packageManager
                val component = ComponentName(context, "com.example.MainActivity")
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
                val today = LocalDate.now()
                val currentDay = today.dayOfMonth
                val todayIso = today.toString()

                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val lastAppliedDay = prefs.getInt(KEY_LAST_APPLIED_DAY, -1)
                val lastSyncDate = prefs.getString(KEY_LAST_SYNC_DATE, null)

                if (!force && lastAppliedDay == currentDay && lastSyncDate == todayIso) {
                    // Already up-to-date for today. Idempotent early return.
                    return@launch
                }

                // Persist the successfully verified device day
                prefs.edit()
                    .putInt(KEY_LAST_APPLIED_DAY, currentDay)
                    .putString(KEY_LAST_SYNC_DATE, todayIso)
                    .apply()

                // Update Home Screen Dynamic Calendar Widget
                LumaCalendarWidgetProvider.updateAllWidgets(context)

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
        return LocalDate.now().dayOfMonth
    }

    /**
     * Returns the last recorded synced day of the month.
     */
    fun getLastAppliedDay(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getInt(KEY_LAST_APPLIED_DAY, -1)
        return if (saved in 1..31) saved else LocalDate.now().dayOfMonth
    }
}
