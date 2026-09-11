package com.example.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import java.time.LocalDate

/**
 * DynamicIconManager:
 * Manages dynamic launcher icon updates reflecting the day of the month (1..31).
 * Supports single-digit (1..9) and double-digit (10..31) days.
 * Provides graceful fallback handling when launchers or device permissions restrict component state changes.
 */
object DynamicIconManager {
    private const val TAG = "DynamicIconManager"
    private const val PREFS_NAME = "luma_dynamic_icon_prefs"
    private const val KEY_ACTIVE_DAY = "active_icon_day"
    private const val KEY_DYNAMIC_SUPPORTED = "dynamic_icon_supported"

    private const val MAIN_ACTIVITY = "com.example.MainActivity"
    private const val ALIAS_PREFIX = "com.example.MainActivityAliasDay"

    /**
     * Updates the launcher icon to reflect the given day of month (1..31).
     * Enables the corresponding activity-alias and disables previous aliases.
     * Fails gracefully if the device launcher restricts dynamic activity alias switches.
     */
    fun updateLauncherIcon(context: Context, dayOfMonth: Int): Boolean {
        val validDay = dayOfMonth.coerceIn(1, 31)
        val pm = context.packageManager

        return try {
            val targetAliasName = "$ALIAS_PREFIX$validDay"
            val targetComponent = ComponentName(context, targetAliasName)

            // 1. Enable the new target alias first
            pm.setComponentEnabledSetting(
                targetComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            // 2. Disable default MainActivity launcher component
            val mainComponent = ComponentName(context, MAIN_ACTIVITY)
            pm.setComponentEnabledSetting(
                mainComponent,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )

            // 3. Disable all other day aliases
            for (day in 1..31) {
                if (day != validDay) {
                    val otherAlias = ComponentName(context, "$ALIAS_PREFIX$day")
                    val currentState = pm.getComponentEnabledSetting(otherAlias)
                    if (currentState != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                        pm.setComponentEnabledSetting(
                            otherAlias,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP
                        )
                    }
                }
            }

            // Save active day in preferences
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_ACTIVE_DAY, validDay)
                .putBoolean(KEY_DYNAMIC_SUPPORTED, true)
                .apply()

            Log.d(TAG, "Successfully updated dynamic launcher icon to day $validDay")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Dynamic launcher icon not permitted by environment; falling back safely", e)
            ensureDefaultIconEnabled(context)
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_DYNAMIC_SUPPORTED, false)
                .apply()
            false
        }
    }

    /**
     * Safely resets or ensures default MainActivity launcher icon is active (Graceful Fallback).
     */
    fun ensureDefaultIconEnabled(context: Context) {
        try {
            val pm = context.packageManager
            val mainComponent = ComponentName(context, MAIN_ACTIVITY)
            pm.setComponentEnabledSetting(
                mainComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore default icon", e)
        }
    }

    /**
     * Returns the currently active day of the month configured for the icon.
     */
    fun getActiveDay(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getInt(KEY_ACTIVE_DAY, -1)
        if (saved in 1..31) return saved
        return LocalDate.now().dayOfMonth
    }

    /**
     * Checks if dynamic icon updates are supported without exceptions.
     */
    fun isDynamicSupported(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DYNAMIC_SUPPORTED, true)
    }

    /**
     * Syncs launcher icon to today's date based on the active calendar type.
     */
    fun syncToToday(context: Context, calendarType: CalendarType = CalendarType.GREGORIAN): Boolean {
        val now = LocalDate.now()
        val todayIso = String.format("%04d-%02d-%02d", now.year, now.monthValue, now.dayOfMonth)
        val todayDay = when (calendarType) {
            CalendarType.JALALI -> {
                CalendarConverter.gregorianToJalali(todayIso).day
            }
            CalendarType.HIJRI -> {
                CalendarConverter.gregorianToHijri(todayIso).day
            }
            CalendarType.GREGORIAN -> {
                now.dayOfMonth
            }
        }
        return updateLauncherIcon(context, todayDay)
    }
}
