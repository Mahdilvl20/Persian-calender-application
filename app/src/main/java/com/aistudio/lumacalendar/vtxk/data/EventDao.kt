package com.aistudio.lumacalendar.vtxk.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM calendar_events ORDER BY date ASC, startTime ASC")
    fun getAllEvents(): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE date = :date ORDER BY startTime ASC")
    fun getEventsForDate(date: String): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC, startTime ASC")
    fun getEventsBetweenDates(startDate: String, endDate: String): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE title LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%' ORDER BY date ASC, startTime ASC")
    fun searchEvents(query: String): Flow<List<CalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEvent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<CalendarEvent>)

    @Update
    suspend fun updateEvent(event: CalendarEvent)

    @Delete
    suspend fun deleteEvent(event: CalendarEvent)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM calendar_events WHERE title IN (:titles)")
    suspend fun deleteByTitles(titles: List<String>)

    @Query("DELETE FROM calendar_events")
    suspend fun clearAll()
}
