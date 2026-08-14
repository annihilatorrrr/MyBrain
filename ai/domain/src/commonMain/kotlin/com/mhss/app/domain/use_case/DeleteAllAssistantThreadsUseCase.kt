package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.AssistantChatRepository
import org.koin.core.annotation.Factory

@Factory
class DeleteAllAssistantThreadsUseCase(
    private val repository: AssistantChatRepository
) {
    suspend operator fun invoke() {
        repository.deleteAllThreads()
    }
}
