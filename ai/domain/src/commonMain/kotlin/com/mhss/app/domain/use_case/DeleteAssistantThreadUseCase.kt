package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.AssistantChatRepository
import org.koin.core.annotation.Factory

@Factory
class DeleteAssistantThreadUseCase(
    private val repository: AssistantChatRepository
) {
    suspend operator fun invoke(threadId: String) {
        repository.deleteThread(threadId)
    }
}
