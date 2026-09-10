package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [CalendarEvent::class], version = 1, exportSchema = false)
abstract class LumaDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: LumaDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): LumaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LumaDatabase::class.java,
                    "luma_calendar_db"
                )
                    .addCallback(LumaDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val initialSampleEvents = listOf(
            CalendarEvent(
                title = "Design Review",
                date = "2026-09-11",
                startTime = "09:00",
                endTime = "10:00",
                category = "Work",
                colorHex = "#6366F1",
                location = "Meeting Room A",
                notes = "Review final iOS liquid glass UI mockups with design lead.",
                reminderMinutes = 15,
                calendarType = "Work"
            ),
            CalendarEvent(
                title = "Lunch with Sarah",
                date = "2026-09-11",
                startTime = "13:30",
                endTime = "14:30",
                category = "Personal",
                colorHex = "#38BDF8",
                location = "Blue Bottle Cafe",
                notes = "Catch up on weekend plans & travel ideas.",
                reminderMinutes = 30,
                calendarType = "Personal"
            ),
            CalendarEvent(
                title = "Gym & Mobility",
                date = "2026-09-11",
                startTime = "18:00",
                endTime = "19:15",
                category = "Health",
                colorHex = "#10B981",
                location = "Equinox Downtown",
                notes = "Leg day focus + 15 min sauna & stretch.",
                reminderMinutes = 15,
                calendarType = "Personal"
            ),
            CalendarEvent(
                title = "Weekly Architecture Sync",
                date = "2026-09-10",
                startTime = "10:00",
                endTime = "11:00",
                category = "Work",
                colorHex = "#6366F1",
                location = "Zoom Studio 4",
                notes = "Q4 architecture roadmap and cloud deployment sync.",
                reminderMinutes = 10,
                calendarType = "Work"
            ),
            CalendarEvent(
                title = "Podcast Recording",
                date = "2026-09-10",
                startTime = "16:30",
                endTime = "17:30",
                category = "Social",
                colorHex = "#A855F7",
                location = "Studio B",
                notes = "Discussing modern mobile design and translucent glass ergonomics.",
                reminderMinutes = 30,
                calendarType = "Personal"
            ),
            CalendarEvent(
                title = "Morning Trail Run",
                date = "2026-09-12",
                startTime = "07:30",
                endTime = "08:45",
                category = "Health",
                colorHex = "#10B981",
                location = "Presidio Trails",
                notes = "7k morning trail run with running club.",
                reminderMinutes = 15,
                calendarType = "Personal"
            ),
            CalendarEvent(
                title = "Farmers Market & Brunch",
                date = "2026-09-12",
                startTime = "11:00",
                endTime = "13:00",
                category = "Personal",
                colorHex = "#FB7185",
                location = "Ferry Building",
                notes = "Pick up fresh sourdough and organic berries.",
                reminderMinutes = 0,
                calendarType = "Personal"
            ),
            CalendarEvent(
                title = "Product Keynote Prep",
                date = "2026-09-14",
                startTime = "14:00",
                endTime = "15:30",
                category = "Work",
                colorHex = "#6366F1",
                location = "Main Auditorium",
                notes = "Rehearse live demo and slide transitions.",
                reminderMinutes = 15,
                calendarType = "Work"
            ),
            CalendarEvent(
                title = "Dentist Checkup",
                date = "2026-09-15",
                startTime = "11:00",
                endTime = "12:00",
                category = "Personal",
                colorHex = "#38BDF8",
                location = "Downtown Dental Suite 402",
                notes = "Routine cleaning and annual dental imaging.",
                reminderMinutes = 60,
                calendarType = "Personal"
            ),
            CalendarEvent(
                title = "Team Dinner & Celebration",
                date = "2026-09-18",
                startTime = "19:00",
                endTime = "21:30",
                category = "Social",
                colorHex = "#F59E0B",
                location = "Benu Restaurant",
                notes = "Celebrating successful v2.6 milestone launch.",
                reminderMinutes = 30,
                calendarType = "Work"
            ),
            CalendarEvent(
                title = "Project Meeting",
                date = "2026-09-20",
                startTime = "15:00",
                endTime = "16:30",
                category = "Work",
                colorHex = "#6366F1",
                location = "Executive Boardroom",
                notes = "Cross-functional quarterly sync with engineering and product leads.",
                reminderMinutes = 15,
                calendarType = "Work"
            ),
            CalendarEvent(
                title = "Flight to San Francisco",
                date = "2026-09-24",
                startTime = "08:15",
                endTime = "11:30",
                category = "Personal",
                colorHex = "#06B6D4",
                location = "Terminal 2, Gate 42",
                notes = "Flight UA 1408. Carry-on only, mobile boarding pass saved.",
                reminderMinutes = 120,
                calendarType = "Personal"
            ),
            CalendarEvent(
                title = "Design Systems Summit",
                date = "2026-09-25",
                startTime = "09:30",
                endTime = "17:00",
                category = "Work",
                colorHex = "#A855F7",
                location = "Moscone Center",
                notes = "Keynotes on Liquid Glass and next-gen adaptive surfaces.",
                reminderMinutes = 30,
                calendarType = "Work"
            )
        )
    }

    private class LumaDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    database.eventDao().insertEvents(initialSampleEvents)
                }
            }
        }
    }
}
