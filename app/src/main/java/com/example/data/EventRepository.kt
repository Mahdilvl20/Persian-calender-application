package com.example.data

import kotlinx.coroutines.flow.Flow

class EventRepository(private val eventDao: EventDao) {
    val allEvents: Flow<List<CalendarEvent>> = eventDao.getAllEvents()

    fun getEventsForDate(date: String): Flow<List<CalendarEvent>> =
        eventDao.getEventsForDate(date)

    fun getEventsBetweenDates(startDate: String, endDate: String): Flow<List<CalendarEvent>> =
        eventDao.getEventsBetweenDates(startDate, endDate)

    fun searchEvents(query: String): Flow<List<CalendarEvent>> =
        eventDao.searchEvents(query)

    suspend fun insertEvent(event: CalendarEvent): Long =
        eventDao.insertEvent(event)

    suspend fun updateEvent(event: CalendarEvent) =
        eventDao.updateEvent(event)

    suspend fun deleteEvent(event: CalendarEvent) =
        eventDao.deleteEvent(event)

    suspend fun deleteById(id: Long) =
        eventDao.deleteById(id)

    suspend fun seedInitialData() {
        eventDao.clearAll()
        eventDao.insertEvents(LumaDatabase.initialSampleEvents)
    }

    suspend fun clearAll() =
        eventDao.clearAll()
}
