package com.aistudio.lumacalendar.vtxk.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aistudio.lumacalendar.vtxk.util.DynamicIconManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Handles automatic midnight updates and snooze wakeups.
 * Updates the Luma Calendar notification dynamically when the real-world day changes
 * without creating or launching any Activities.
 */
class MidnightUpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        android.util.Log.d("LumaDailyNotification", "MidnightUpdateReceiver: receiver fired, action=${intent?.action}")
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                // Refresh notification for the new day
                android.util.Log.d("LumaDailyNotification", "MidnightUpdateReceiver: notification update triggered")
                LumaNotificationManager.updateNotification(context)

                // Refresh any launcher shortcuts or widgets
                try {
                    DynamicIconManager.updateLiveCalendarShortcutAsync(context)
                } catch (_: Exception) {}

                // Schedule next day's midnight alarm
                android.util.Log.d("LumaDailyNotification", "MidnightUpdateReceiver: scheduling next midnight alarm")
                LumaNotificationManager.scheduleMidnightUpdate(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
