package com.mhss.app.domain.repository

import com.mhss.app.domain.model.Calendar
import com.mhss.app.domain.model.CalendarEvent

interface CalendarRepository {

    suspend fun getEvents(excludedCalendars: List<Int> = emptyList(), until: Long? = null): List<CalendarEvent>

    suspend fun getEvents(start: Long, end: Long, excludedCalendars: List<Int> = emptyList()): List<CalendarEvent>

    suspend fun getCalendars(): List<Calendar>

    suspend fun addEvent(event: CalendarEvent)

    suspend fun deleteEvent(event: CalendarEvent)

    suspend fun updateEvent(event: CalendarEvent)

    suspend fun createCalendar()
}