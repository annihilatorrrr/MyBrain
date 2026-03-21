package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.TaskRepository
import org.koin.core.annotation.Factory

@Factory
class GetTaskByIdUseCase(
    private val tasksRepository: TaskRepository
) {
    suspend operator fun invoke(id: String) = tasksRepository.getTaskById(id)
}