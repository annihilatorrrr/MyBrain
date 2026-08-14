package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.AssistantChatRepository
import org.koin.core.annotation.Factory

@Factory
class DeleteAssistantMessageUseCase(
    private val repository: AssistantChatRepository
) {
    suspend operator fun invoke(messageId: String) {
        repository.deleteMessage(messageId)
    }
}
