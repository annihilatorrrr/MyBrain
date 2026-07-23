package com.mhss.app.data.tools

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolBase
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.serialization.typeToken
import com.mhss.app.data.llmDateTimeFormatUnicode
import com.mhss.app.data.parseDateTimeFromLLM
import com.mhss.app.domain.model.Calendar
import com.mhss.app.domain.model.CalendarEvent
import com.mhss.app.domain.model.CalendarEventFrequency
import com.mhss.app.domain.use_case.AddCalendarEventUseCase
import com.mhss.app.domain.use_case.GetAllCalendarsUseCase
import com.mhss.app.domain.use_case.GetEventsWithinRangeUseCase
import com.mhss.app.domain.use_case.SearchEventsByTitleWithinRangeUseCase
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.stringSetPreferencesKey
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Factory
import java.util.Locale

@Factory
class CalendarToolSet(
    private val getEventsWithinRangeUseCase: GetEventsWithinRangeUseCase,
    private val searchEventsByTitleWithinRangeUseCase: SearchEventsByTitleWithinRangeUseCase,
    private val addCalendarEvent: AddCalendarEventUseCase,
    private val getAllCalendarsUseCase: GetAllCalendarsUseCase,
    private val getPreference: GetPreferenceUseCase
) {
    private val getEventsWithinRangeTool = object : Tool<GetEventsWithinRangeArgs, GetEventsResult>(
        argsType = typeToken<GetEventsWithinRangeArgs>(),
        resultType = typeToken<GetEventsResult>(),
        name = GET_EVENTS_WITHIN_RANGE_TOOL,
        description = "Get events within date range. If the user asks about the date/time of an event, use $FORMAT_DATE_TOOL to get accurate dates from the result."
    ) {
        override suspend fun execute(args: GetEventsWithinRangeArgs): GetEventsResult {
            val startMillis = args.startDateTime.parseDateTimeFromLLM()
                ?: throw IllegalArgumentException("Invalid start date format. The operation did not proceed.")
            val endMillis = args.endDateTime.parseDateTimeFromLLM()
                ?: throw IllegalArgumentException("Invalid end date format. The operation did not proceed.")
            return GetEventsResult(getEventsWithinRangeUseCase(startMillis, endMillis, getExcludedCalendars()))
        }
    }

    private val searchEventsByNameWithinRangeTool =
        object : Tool<SearchEventsByNameWithinRangeArgs, SearchEventsResult>(
            argsType = typeToken<SearchEventsByNameWithinRangeArgs>(),
            resultType = typeToken<SearchEventsResult>(),
            name = SEARCH_EVENTS_BY_NAME_WITHIN_RANGE_TOOL,
            description = "Search for an event name within a date range. Useful for finding an event while using a large range comfortably (e.g 3 months) without needing to call getEventsWithinRange and polluting the results with unnecessary unrelated events. If the user asks about the date/time of an event, use $FORMAT_DATE_TOOL to get accurate dates from the result."
        ) {
            override suspend fun execute(args: SearchEventsByNameWithinRangeArgs): SearchEventsResult {
                val query = args.eventName.trim()
                if (query.isBlank()) throw IllegalArgumentException("Invalid event name. The operation did not proceed.")

                val startMillis = args.startDateTime.parseDateTimeFromLLM()
                    ?: throw IllegalArgumentException("Invalid start date format. The operation did not proceed.")
                val endMillis = args.endDateTime.parseDateTimeFromLLM()
                    ?: throw IllegalArgumentException("Invalid end date format. The operation did not proceed.")

                return SearchEventsResult(
                    searchEventsByTitleWithinRangeUseCase(
                        startMillis = startMillis,
                        endMillis = endMillis,
                        titleQuery = query,
                        excludedCalendars = getExcludedCalendars()
                    )
                )
            }
        }

    private val createEventTool = object : Tool<CreateEventArgs, CalendarEventIdResult>(
        argsType = typeToken<CreateEventArgs>(),
        resultType = typeToken<CalendarEventIdResult>(),
        name = CREATE_EVENT_TOOL,
        description = "Create event. Returns ID."
    ) {
        override suspend fun execute(args: CreateEventArgs): CalendarEventIdResult {
            val startMillis = args.start.parseDateTimeFromLLM()
                ?: throw IllegalArgumentException("Invalid start date format. The event was not created.")
            val endMillis = args.end.parseDateTimeFromLLM()
                ?: throw IllegalArgumentException("Invalid end date format. The event was not created.")
            val event = CalendarEvent(
                id = 0,
                title = args.title,
                description = args.description,
                start = startMillis,
                end = endMillis,
                location = args.location,
                allDay = args.allDay,
                calendarId = args.calendarId,
                recurring = args.recurring,
                frequency = args.frequency,
                interval = args.interval.coerceAtLeast(1),
                weekDays = args.weekDays.mapNotNull { it.toDayOfWeekOrNull() }.toHashSet()
            )
            return CalendarEventIdResult(createdEventId = addCalendarEvent(event))
        }
    }

    private val createEventsTool = object : Tool<CreateEventsArgs, CalendarEventIdsResult>(
        argsType = typeToken<CreateEventsArgs>(),
        resultType = typeToken<CalendarEventIdsResult>(),
        name = CREATE_EVENTS_TOOL,
        description = "Create multiple events. Returns IDs."
    ) {
        override suspend fun execute(args: CreateEventsArgs): CalendarEventIdsResult {
            val ids = args.events.map { input ->
                val startMillis = input.start.parseDateTimeFromLLM()
                    ?: throw IllegalArgumentException("Invalid start date format for event: ${input.title}. The events were not created.")
                val endMillis = input.end.parseDateTimeFromLLM()
                    ?: throw IllegalArgumentException("Invalid end date format for event: ${input.title}. The events were not created.")
                val event = CalendarEvent(
                    id = 0,
                    title = input.title,
                    description = input.description,
                    start = startMillis,
                    end = endMillis,
                    location = input.location,
                    allDay = input.allDay,
                    calendarId = input.calendarId,
                    recurring = input.recurring,
                    frequency = input.frequency,
                    interval = input.interval.coerceAtLeast(1),
                    weekDays = input.weekDays.mapNotNull { it.toDayOfWeekOrNull() }.toHashSet()
                )
                addCalendarEvent(event)
            }
            return CalendarEventIdsResult(createdEventIds = ids)
        }
    }

    private val getAllCalendarsTool = object : Tool<GetAllCalendarsArgs, GetCalendarsResult>(
        argsType = typeToken<GetAllCalendarsArgs>(),
        resultType = typeToken<GetCalendarsResult>(),
        name = GET_ALL_CALENDARS_TOOL,
        description = "Get all calendars (grouped by account)."
    ) {
        override suspend fun execute(args: GetAllCalendarsArgs): GetCalendarsResult =
            GetCalendarsResult(getAllCalendarsUseCase(getExcludedCalendars()))
    }

    val tools: List<ToolBase<*, *>> = listOf(
        getEventsWithinRangeTool,
        searchEventsByNameWithinRangeTool,
        createEventTool,
        createEventsTool,
        getAllCalendarsTool
    )

    private suspend fun getExcludedCalendars(): List<Int> {
        return getPreference(
            stringSetPreferencesKey(PrefsConstants.EXCLUDED_CALENDARS_KEY),
            emptySet()
        ).firstOrNull().orEmpty().mapNotNull { it.toIntOrNull() }
    }
}

@Serializable
data class GetEventsWithinRangeArgs(
    @property:LLMDescription("Format: $llmDateTimeFormatUnicode") val startDateTime: String,
    @property:LLMDescription("Format: $llmDateTimeFormatUnicode") val endDateTime: String
)

@Serializable
data class SearchEventsByNameWithinRangeArgs(
    @property:LLMDescription("Event name (or partial name) to search for.") val eventName: String,
    @property:LLMDescription("Format: $llmDateTimeFormatUnicode") val startDateTime: String,
    @property:LLMDescription("Format: $llmDateTimeFormatUnicode") val endDateTime: String
)

@Serializable
data class CreateEventArgs(
    val title: String,
    @property:LLMDescription("Format: $llmDateTimeFormatUnicode") val start: String,
    @property:LLMDescription("Format: $llmDateTimeFormatUnicode") val end: String,
    @property:LLMDescription("Use getAllCalendars to get ID") val calendarId: Long,
    val description: String? = null,
    val location: String? = null,
    val allDay: Boolean = false,
    val recurring: Boolean = false,
    val frequency: CalendarEventFrequency = CalendarEventFrequency.NEVER,
    @property:LLMDescription("Repeat interval. Minimum value is 1.") val interval: Int = 1,
    @property:LLMDescription("Only for weekly repeats. Use weekday names or RFC codes such as MONDAY or MO.") val weekDays: List<String> = emptyList()
)

@Serializable
data class CreateEventsArgs(val events: List<CalendarEventInput>)

@Serializable
class GetAllCalendarsArgs

private fun String.toDayOfWeekOrNull(): DayOfWeek? {
    return when (trim().uppercase(Locale.US)) {
        "MONDAY", "MON", "MO" -> DayOfWeek.MONDAY
        "TUESDAY", "TUE", "TU" -> DayOfWeek.TUESDAY
        "WEDNESDAY", "WED", "WE" -> DayOfWeek.WEDNESDAY
        "THURSDAY", "THU", "TH" -> DayOfWeek.THURSDAY
        "FRIDAY", "FRI", "FR" -> DayOfWeek.FRIDAY
        "SATURDAY", "SAT", "SA" -> DayOfWeek.SATURDAY
        "SUNDAY", "SUN", "SU" -> DayOfWeek.SUNDAY
        else -> null
    }
}

@Serializable
data class CalendarEventInput(
    val title: String,
    @property:LLMDescription("Format: $llmDateTimeFormatUnicode") val start: String,
    @property:LLMDescription("Format: $llmDateTimeFormatUnicode") val end: String,
    val calendarId: Long,
    val description: String? = null,
    val location: String? = null,
    val allDay: Boolean = false,
    val recurring: Boolean = false,
    val frequency: CalendarEventFrequency = CalendarEventFrequency.NEVER,
    val interval: Int = 1,
    val weekDays: List<String> = emptyList()
)

@Serializable
data class GetEventsResult(val events: List<CalendarEvent>)

@Serializable
data class SearchEventsResult(val events: List<CalendarEvent>)

@Serializable
data class GetCalendarsResult(val calendars: Map<String, List<Calendar>>)

@Serializable
data class CalendarEventIdResult(val createdEventId: Long?)

@Serializable
data class CalendarEventIdsResult(val createdEventIds: List<Long?>)
