package com.mhss.app.domain.use_case

import com.mhss.app.domain.MONTH_GRID_CELL_COUNT
import com.mhss.app.domain.model.CalendarDay
import com.mhss.app.domain.repository.CalendarRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L

@Single
class GetMonthEventsUseCase(
    private val calendarRepository: CalendarRepository,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(
        month: YearMonth,
        excludedCalendars: List<Int>
    ): List<CalendarDay> {
        return withContext(defaultDispatcher) {
            val firstOfMonth = month.firstDay
            val startOffset = firstOfMonth.dayOfWeek.dayNumber % 7
            val startDate = firstOfMonth.minus(startOffset, DateTimeUnit.DAY)

            val gridDays = MONTH_GRID_CELL_COUNT.toLong()
            val endDate = startDate.plus(gridDays, DateTimeUnit.DAY)

            val startMillis = startDate.atTime(hour = 0, minute = 0).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            val endMillis = endDate.atTime(hour = 23, minute = 59).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

            val events = calendarRepository.getEvents(startMillis, endMillis, excludedCalendars)

            val eventsByDayIndex = events.groupBy { event ->
                ((event.start - startMillis) / MILLIS_PER_DAY).toInt()
            }

            (0 until gridDays.toInt()).map { index ->
                val dayDate = startDate.plus(index, DateTimeUnit.DAY)
                CalendarDay(
                    date = dayDate,
                    isCurrentMonth = dayDate.month == month.month && dayDate.year == month.year,
                    events = eventsByDayIndex[index].orEmpty()
                )
            }
        }
    }
}

val DayOfWeek.dayNumber: Int
    get() = when (this) {
        DayOfWeek.SUNDAY -> 1
        DayOfWeek.MONDAY -> 2
        DayOfWeek.TUESDAY -> 3
        DayOfWeek.WEDNESDAY -> 4
        DayOfWeek.THURSDAY -> 5
        DayOfWeek.FRIDAY -> 6
        DayOfWeek.SATURDAY -> 7
    }

