package com.mhss.app.presentation

import com.mhss.app.domain.model.Calendar
import com.mhss.app.domain.model.CalendarEvent
import kotlinx.datetime.LocalDate

sealed class CalendarViewModelEvent {
    data class IncludeCalendar(val calendar: Calendar) : CalendarViewModelEvent()
    data class ReadPermissionChanged(val hasPermission: Boolean) : CalendarViewModelEvent()
    data class EditEvent(val event: CalendarEvent) : CalendarViewModelEvent()
    data class DeleteEvent(val event: CalendarEvent) : CalendarViewModelEvent()
    data class AddEvent(val event: CalendarEvent) : CalendarViewModelEvent()
    data class ViewModeChanged(val isMonthView: Boolean) : CalendarViewModelEvent()
    data class MonthChanged(val newMonth: LocalDate) : CalendarViewModelEvent()
    object ErrorDisplayed : CalendarViewModelEvent()
}
