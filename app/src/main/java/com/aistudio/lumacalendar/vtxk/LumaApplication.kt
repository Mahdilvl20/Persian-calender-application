package com.aistudio.lumacalendar.vtxk

import android.app.Application
import android.util.Log
import com.aistudio.lumacalendar.vtxk.notification.EventNotificationScheduler
import com.aistudio.lumacalendar.vtxk.notification.LumaNotificationManager
import com.aistudio.lumacalendar.vtxk.notification.NotificationPreferences
import com.aistudio.lumacalendar.vtxk.util.DynamicIconManager

class LumaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("LumaDailyNotification", "LumaApplication.onCreate() started")

        // 1. Initialize notification channels early
        LumaNotificationManager.createChannels(this)
        EventNotificationScheduler.createChannel(this)

        // 2. Ensure dynamic launcher shortcuts are synced
        try {
            DynamicIconManager.updateLiveCalendarShortcutAsync(this)
        } catch (_: Exception) {}

        // 3. Post daily notification if permission is granted and notification is enabled
        if (NotificationPreferences.isDailyNotificationEnabled(this) &&
            EventNotificationScheduler.hasNotificationPermission(this)) {
            Log.d("LumaDailyNotification", "LumaApplication: Posting daily notification at startup")
            LumaNotificationManager.updateNotificationAsync(this)
            LumaNotificationManager.scheduleMidnightUpdate(this)
        } else {
            Log.d("LumaDailyNotification", "LumaApplication: Daily notification not posted at startup (dailyEnabled=${NotificationPreferences.isDailyNotificationEnabled(this)}, perm=${EventNotificationScheduler.hasNotificationPermission(this)})")
        }
    }
}
