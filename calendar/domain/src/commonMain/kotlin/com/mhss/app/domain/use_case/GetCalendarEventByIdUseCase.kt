package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.CalendarEvent
import com.mhss.app.domain.repository.CalendarRepository
import org.koin.core.annotation.Single

@Single
class GetCalendarEventByIdUseCase(
    private val calendarRepository: CalendarRepository
) {
    suspend operator fun invoke(id: Long): CalendarEvent? {
        return calendarRepository.getEventById(id)
    }
}
