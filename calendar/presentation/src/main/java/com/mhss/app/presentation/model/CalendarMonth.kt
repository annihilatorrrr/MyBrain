package com.mhss.app.presentation.model

import com.mhss.app.domain.model.CalendarDay

data class CalendarMonth(
    val monthNumber: Int,
    val days: List<CalendarDay>
)

