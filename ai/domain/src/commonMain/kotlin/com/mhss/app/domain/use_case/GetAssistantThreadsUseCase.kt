package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.AssistantThread
import com.mhss.app.domain.repository.AssistantChatRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetAssistantThreadsUseCase(
    private val repository: AssistantChatRepository
) {
    operator fun invoke(): Flow<List<AssistantThread>> = repository.getAllThreads()
}
