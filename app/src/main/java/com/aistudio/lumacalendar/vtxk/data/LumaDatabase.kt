package com.aistudio.lumacalendar.vtxk.data

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

        val initialSampleEvents: List<CalendarEvent> = emptyList()
    }

    private class LumaDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // No fake sample events: Calendar starts with only real calendar data and official holidays.
        }
    }
}
