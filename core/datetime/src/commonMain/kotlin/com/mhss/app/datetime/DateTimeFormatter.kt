package com.mhss.app.datetime

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

interface DateTimeFormatter {
    fun formatEventsDayName(date: LocalDate): String
    fun formatDateDependingOnDay(timestamp: Long): String
    fun fullDate(timestamp: Long): String
    fun formatDateForMapping(timestamp: Long): String
    fun formatDateForMapping(date: LocalDate): String
    fun formatTime(timestamp: Long): String
    fun formatDate(timestamp: Long, forceShowYear: Boolean = false): String
    fun monthName(timestamp: Long): String
    fun getDisplayName(dayOfWeek: DayOfWeek): String
    fun monthName(date: LocalDate): String
    val is24HourFormat: Boolean

    fun formatEventStartEnd(
        start: Long,
        end: Long,
        allDayString: String,
        eventTimeAt: String,
        eventTime: String,
        location: String?,
        allDay: Boolean
    ): String
}
