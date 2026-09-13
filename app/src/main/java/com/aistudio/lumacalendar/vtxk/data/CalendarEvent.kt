package com.aistudio.lumacalendar.vtxk.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val date: String, // Format: "YYYY-MM-DD" e.g., "2026-09-11"
    val startTime: String, // Format: "HH:mm" e.g., "11:00"
    val endTime: String = "", // Legacy or optional: defaults to empty or matching startTime
    val category: String, // "Work", "Personal", "Health", "Social", "Finance"
    val colorHex: String, // "#6366F1", "#38BDF8", "#10B981", "#FB7185", etc.
    val location: String = "",
    val notes: String = "",
    val reminderMinutes: Int = 15,
    val calendarType: String = "Personal" // "Personal", "Work", "Holidays"
) {
    /** Single event time accessor */
    val time: String
        get() = startTime
}
