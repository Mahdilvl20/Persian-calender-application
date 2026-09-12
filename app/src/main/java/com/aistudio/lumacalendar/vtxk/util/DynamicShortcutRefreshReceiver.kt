package com.aistudio.lumacalendar.vtxk.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DynamicShortcutRefreshReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.component?.className != javaClass.name) return
        DynamicIconManager.updateLiveCalendarShortcutAsync(context)
    }
}
