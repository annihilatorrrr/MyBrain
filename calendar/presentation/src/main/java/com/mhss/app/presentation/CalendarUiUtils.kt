package com.mhss.app.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.listSaver
import org.jetbrains.compose.resources.stringResource
import com.mhss.app.domain.model.CalendarEventFrequency
import com.mhss.app.datetime.LocalDateTimeFormatter
import com.mhss.app.ui.Res
import com.mhss.app.ui.day
import com.mhss.app.ui.days
import com.mhss.app.ui.do_not_repeat
import com.mhss.app.ui.every_day
import com.mhss.app.ui.every_month
import com.mhss.app.ui.every_week
import com.mhss.app.ui.every_week_on
import com.mhss.app.ui.every_year
import com.mhss.app.ui.month
import com.mhss.app.ui.months
import com.mhss.app.ui.repeat_every_interval
import com.mhss.app.ui.repeat_every_interval_on
import com.mhss.app.ui.week
import com.mhss.app.ui.weeks
import com.mhss.app.ui.year
import com.mhss.app.ui.years
import kotlinx.datetime.DayOfWeek

@Composable
fun CalendarEventFrequency.getCalendarFrequencyTitle(
    interval: Int = 1,
    weekDays: Set<DayOfWeek> = emptySet()
): String {
    val safeInterval = interval.coerceAtLeast(1)
    return when (this) {
        CalendarEventFrequency.DAILY -> {
            if (safeInterval == 1) {
                stringResource(Res.string.every_day)
            } else {
                stringResource(
                    Res.string.repeat_every_interval,
                    safeInterval,
                    getIntervalUnitTitle(safeInterval)
                )
            }
        }
        CalendarEventFrequency.WEEKLY -> {
            val formatter = LocalDateTimeFormatter.current
            val dayLabel = weekDays
                .sortedBy { it.toRecurringSortOrder() }
                .joinToString(", ") { formatter.getDisplayName(it) }
            if (dayLabel.isBlank() && safeInterval == 1) {
                stringResource(Res.string.every_week)
            } else if (dayLabel.isBlank()) {
                stringResource(
                    Res.string.repeat_every_interval,
                    safeInterval,
                    getIntervalUnitTitle(safeInterval)
                )
            } else if (safeInterval == 1) {
                stringResource(Res.string.every_week_on, dayLabel)
            } else {
                stringResource(
                    Res.string.repeat_every_interval_on,
                    safeInterval,
                    getIntervalUnitTitle(safeInterval),
                    dayLabel
                )
            }
        }
        CalendarEventFrequency.MONTHLY -> {
            if (safeInterval == 1) {
                stringResource(Res.string.every_month)
            } else {
                stringResource(
                    Res.string.repeat_every_interval,
                    safeInterval,
                    getIntervalUnitTitle(safeInterval)
                )
            }
        }
        CalendarEventFrequency.YEARLY -> {
            if (safeInterval == 1) {
                stringResource(Res.string.every_year)
            } else {
                stringResource(
                    Res.string.repeat_every_interval,
                    safeInterval,
                    getIntervalUnitTitle(safeInterval)
                )
            }
        }
        CalendarEventFrequency.NEVER -> stringResource(Res.string.do_not_repeat)
    }
}

@Composable
fun CalendarEventFrequency.getIntervalUnitTitle(interval: Int): String {
    val isSingular = interval.coerceAtLeast(1) == 1
    return when (this) {
        CalendarEventFrequency.DAILY -> stringResource(if (isSingular) Res.string.day else Res.string.days)
        CalendarEventFrequency.WEEKLY -> stringResource(if (isSingular) Res.string.week else Res.string.weeks)
        CalendarEventFrequency.MONTHLY -> stringResource(if (isSingular) Res.string.month else Res.string.months)
        CalendarEventFrequency.YEARLY -> stringResource(if (isSingular) Res.string.year else Res.string.years)
        CalendarEventFrequency.NEVER -> ""
    }
}

fun DayOfWeek.toRecurringSortOrder(): Int {
    return when (this) {
        DayOfWeek.SUNDAY -> 0
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
    }
}

val dayOfWeekSetSaver = listSaver(
    save = { selectedDays -> selectedDays.map(DayOfWeek::name) },
    restore = { savedDays ->
        savedDays.mapNotNull { dayName ->
            runCatching { DayOfWeek.valueOf(dayName) }.getOrNull()
        }.toSet()
    }
)
