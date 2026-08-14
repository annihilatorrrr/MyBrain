package com.mhss.app.domain.model

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Returns the event date range, using UTC for all-day events and making midnight end dates inclusive.
 */
@OptIn(ExperimentalTime::class)
fun CalendarEvent.effectiveDateRange(localTz: TimeZone): Pair<LocalDate, LocalDate> {
    val zone = if (allDay) TimeZone.UTC else localTz
    val startDate = Instant.fromEpochMilliseconds(start).toLocalDateTime(zone).date
    val endDateTime = Instant.fromEpochMilliseconds(end).toLocalDateTime(zone)
    val rawEndDate = endDateTime.date
    val endDate = if (rawEndDate > startDate && endDateTime.time == midnight)
        rawEndDate.minus(1, DateTimeUnit.DAY) else rawEndDate
    return startDate to endDate
}

private val midnight = LocalTime(0, 0)
