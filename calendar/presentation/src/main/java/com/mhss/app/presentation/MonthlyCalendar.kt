package com.mhss.app.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mhss.app.domain.MONTH_GRID_CELL_COUNT
import com.mhss.app.domain.model.CalendarDay
import com.mhss.app.presentation.model.CalendarMonth
import com.mhss.app.util.date.getDisplayName
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth

private val WEEK_DAYS = listOf(
    DayOfWeek.SUNDAY,
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthlyCalendar(
    modifier: Modifier = Modifier,
    loadedMonths: SnapshotStateMap<Int, CalendarMonth>,
    onLoadMonth: (YearMonth) -> Unit,
    selectedDate: CalendarDay,
    today: LocalDate,
    onDaySelected: (CalendarDay) -> Unit,
    onMonthChanged: (LocalDate) -> Unit
) {
    val initialPage = CALENDAR_START_PAGE
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { CALENDAR_TOTAL_PAGES }
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.settledPage) {
        val offset = pagerState.settledPage - initialPage
        val month = today.yearMonth.plus(offset, DateTimeUnit.MONTH)
        onLoadMonth(month)
    }

    LaunchedEffect(pagerState.currentPage) {
        val offset = pagerState.currentPage - initialPage
        val month = today.plus(offset, DateTimeUnit.MONTH)
        onMonthChanged(month)
    }

    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            WEEK_DAYS.forEach { day ->
                Text(
                    text = day.getDisplayName(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            val offset = page - initialPage
            val month = remember(offset) { today.yearMonth.plus(offset, DateTimeUnit.MONTH) }

            val monthData = loadedMonths[month.month.number]

            LaunchedEffect(monthData, pagerState.settledPage) {
                val data = monthData ?: return@LaunchedEffect
                if (pagerState.settledPage == page) {
                    if (data.monthNumber == today.month.number) {
                        data.days.firstOrNull { it.date.day == today.day }?.let { onDaySelected(it) }
                    } else {
                        data.days.firstOrNull { it.isCurrentMonth }?.let {
                            if (selectedDate.date.month != month.month) onDaySelected(it)
                        }
                    }
                }
            }

            if (monthData != null) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(WEEK_DAYS.size),
                    modifier = Modifier.fillMaxWidth(),
                    userScrollEnabled = false,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    contentPadding = PaddingValues(2.dp)
                ) {
                    items(
                        items = monthData.days,
                        key = { it.date.toEpochDays() }
                    ) { day ->
                        CalendarDayCell(
                            day = day,
                            isSelected = day.date == selectedDate.date,
                            isToday = day.date == today,
                            onDaySelected = {
                                onDaySelected(it)
                                if (!it.isCurrentMonth) {
                                    scope.launch {
                                        if (it.date.yearMonth < month) pagerState.animateScrollToPage(page - 1)
                                        else pagerState.animateScrollToPage(page + 1)
                                    }
                                }
                            }
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(WEEK_DAYS.size),
                    modifier = Modifier.fillMaxWidth(),
                    userScrollEnabled = false,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp)
                ) {
                    items(MONTH_GRID_CELL_COUNT) {
                        EmptyCalendarDayCell()
                    }
                }
            }
        }
    }
}
