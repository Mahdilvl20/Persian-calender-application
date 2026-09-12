package com.aistudio.lumacalendar.vtxk.data

import kotlinx.coroutines.flow.Flow

class EventRepository(private val eventDao: EventDao) {
    val allEvents: Flow<List<CalendarEvent>> = eventDao.getAllEvents()

    suspend fun getAllEventsSnapshot(): List<CalendarEvent> =
        eventDao.getAllEventsSnapshot()

    suspend fun getEventById(id: Long): CalendarEvent? =
        eventDao.getEventById(id)

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

    suspend fun removeDemoDataIfPresent() {
        val demoTitles = listOf(
            "Design Review", "Lunch with Sarah", "Gym & Mobility",
            "Weekly Architecture Sync", "Podcast Recording", "Morning Trail Run",
            "Farmers Market & Brunch", "Product Keynote Prep", "Dentist Checkup",
            "Team Dinner & Celebration", "Project Meeting", "Flight to San Francisco",
            "Design Systems Summit"
        )
        eventDao.deleteByTitles(demoTitles)
    }

    suspend fun seedInitialData() {
        eventDao.clearAll()
    }

    suspend fun clearAll() =
        eventDao.clearAll()
}
