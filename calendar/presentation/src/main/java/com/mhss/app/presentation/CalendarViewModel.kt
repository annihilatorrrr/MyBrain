package com.mhss.app.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.domain.model.Calendar
import com.mhss.app.domain.model.CalendarEvent
import com.mhss.app.domain.use_case.GetAllCalendarsUseCase
import com.mhss.app.domain.use_case.GetAllEventsUseCase
import com.mhss.app.domain.use_case.GetMonthEventsUseCase
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.booleanPreferencesKey
import com.mhss.app.preferences.domain.model.stringSetPreferencesKey
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.preferences.domain.use_case.SavePreferenceUseCase
import com.mhss.app.presentation.model.CalendarMonth
import com.mhss.app.ui.toIntList
import com.mhss.app.util.date.currentLocalDate
import com.mhss.app.util.date.formatDateForMapping
import com.mhss.app.util.date.monthName
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.minusMonth
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.plusMonth
import kotlinx.datetime.yearMonth
import org.koin.android.annotation.KoinViewModel


const val CALENDAR_START_PAGE = 24000
const val CALENDAR_TOTAL_PAGES = 48000

@KoinViewModel
class CalendarViewModel(
    private val getAllEventsUseCase: GetAllEventsUseCase,
    private val getMonthEventsUseCase: GetMonthEventsUseCase,
    private val getAllCalendarsUseCase: GetAllCalendarsUseCase,
    private val savePreference: SavePreferenceUseCase,
    private val getPreference: GetPreferenceUseCase
) : ViewModel() {

    var uiState by mutableStateOf(UiState())
        private set

    private val loadMutex = Mutex()

    private var updateEventsJob: Job? = null
    private var viewModeJob: Job? = null

    fun loadMonth(month: YearMonth) {
        val loadedMonthValue = month.month.number
        val loadedMonths = uiState.loadedMonths
        viewModelScope.launch {
            loadMutex.withLock {
                val monthData = async {
                    if (loadedMonths.containsKey(loadedMonthValue)) null
                    else {
                        val days = getMonthEventsUseCase(month, uiState.excludedCalendars)
                        CalendarMonth(month.month.number, days)
                    }
                }
                val prevMonthData = async {
                    val prevMonth = month.minusMonth()
                    if (loadedMonths.containsKey(prevMonth.month.number)) null
                    else {
                        val prevMonth = month.minus(1, DateTimeUnit.MONTH)
                        val days =
                            getMonthEventsUseCase(prevMonth, uiState.excludedCalendars)
                        CalendarMonth(prevMonth.month.number, days)
                    }
                }
                val nextMonthData = async {
                    val nextMonth = month.plusMonth()
                    if (loadedMonths.containsKey(nextMonth.month.number)) null
                    else {
                        val days =
                            getMonthEventsUseCase(nextMonth, uiState.excludedCalendars)
                        CalendarMonth(nextMonth.month.number, days)
                    }
                }

                val map = uiState.loadedMonths

                // if we guarantee that the map will have at most 4 items, we won't have key collisions from different years.
                monthData.await()?.let { map[it.monthNumber] = it }
                prevMonthData.await()?.let { map[it.monthNumber] = it }
                nextMonthData.await()?.let { map[it.monthNumber] = it }

                // keeping max of 4 months in memory
                if (map.size > 4) {
                    map.remove(month.minus(3, DateTimeUnit.MONTH).month.number)
                    map.remove(month.plus(3, DateTimeUnit.MONTH).month.number)
                }
            }
        }
    }

    init {
        uiState = uiState.copy(currentMonth = currentLocalDate())
        collectViewMode()
    }

    fun onEvent(event: CalendarViewModelEvent) {
        when (event) {
            is CalendarViewModelEvent.IncludeCalendar -> updateExcludedCalendars(
                event.calendar.id.toInt(),
                event.calendar.included
            )

            is CalendarViewModelEvent.ReadPermissionChanged -> {
                if (event.hasPermission) collectSettings()
                else updateEventsJob?.cancel()
            }

            is CalendarViewModelEvent.MonthChanged -> {
                uiState = uiState.copy(currentMonth = event.newMonth)
            }

            is CalendarViewModelEvent.ViewModeChanged -> {
                viewModelScope.launch {
                    savePreference(
                        booleanPreferencesKey(PrefsConstants.CALENDAR_VIEW_MODE_KEY),
                        event.isMonthView
                    )
                }
                uiState = uiState.copy(isMonthView = event.isMonthView)
            }
        }
    }

    private fun updateExcludedCalendars(id: Int, add: Boolean) {
        viewModelScope.launch {
            savePreference(
                stringSetPreferencesKey(PrefsConstants.EXCLUDED_CALENDARS_KEY),
                if (add) uiState.excludedCalendars.addAndToStringSet(id)
                else uiState.excludedCalendars.removeAndToStringSet(id)
            )
        }
    }

    private fun collectSettings() {
        updateEventsJob = getPreference(
            stringSetPreferencesKey(PrefsConstants.EXCLUDED_CALENDARS_KEY),
            emptySet()
        ).onEach { calendarsSet ->
            val calendars = getAllCalendarsUseCase(calendarsSet.toIntList())
            uiState = uiState.copy(
                excludedCalendars = calendarsSet.map { it.toInt() }.toMutableList(),
                calendars = calendars
            )
            loadEvents()
        }.launchIn(viewModelScope)
    }

    private fun collectViewMode() {
        viewModeJob?.cancel()
        viewModeJob = getPreference(
            booleanPreferencesKey(PrefsConstants.CALENDAR_VIEW_MODE_KEY),
            false
        ).onEach { isMonthView ->
            uiState = uiState.copy(isMonthView = isMonthView)
            loadEvents()
        }.launchIn(viewModelScope)
    }

    private fun loadEvents() {
        if (uiState.isMonthView) {
            loadMonth(uiState.currentMonth.yearMonth)
            uiState = uiState.copy(events = emptyMap())
        } else {
            loadListEvents()
            uiState.loadedMonths.clear()
        }
    }

    private fun loadListEvents() {
        viewModelScope.launch {
            val events = getAllEventsUseCase(uiState.excludedCalendars) {
                it.start.formatDateForMapping()
            }
            val months = events.map {
                it.value.first().start.monthName()
            }.distinct()
            uiState = uiState.copy(events = events, months = months)
        }
    }

    data class UiState(
        val events: Map<String, List<CalendarEvent>> = emptyMap(),
        val calendars: Map<String, List<Calendar>> = emptyMap(),
        val excludedCalendars: MutableList<Int> = mutableListOf(),
        val months: List<String> = emptyList(),
        val isMonthView: Boolean = false,
        val currentMonth: LocalDate = currentLocalDate(),
        val loadedMonths: SnapshotStateMap<Int, CalendarMonth> = mutableStateMapOf()
    )

    private fun MutableList<Int>.addAndToStringSet(id: Int) =
        apply { add(id) }.map { it.toString() }.toSet()

    private fun MutableList<Int>.removeAndToStringSet(id: Int) =
        apply { remove(id) }.map { it.toString() }.toSet()
}
