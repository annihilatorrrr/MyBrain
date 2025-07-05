package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.TaskRepository
import org.koin.core.annotation.Factory

@Factory
class GetTaskByAlarmUseCase(
    private val tasksRepository: TaskRepository
) {
    suspend operator fun invoke(alarmId: Int) = tasksRepository.getTaskByAlarm(alarmId)
}