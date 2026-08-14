package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.repository.AssistantChatRepository
import org.koin.core.annotation.Factory

@Factory
class SaveAssistantMessageUseCase(
    private val repository: AssistantChatRepository
) {
    suspend operator fun invoke(threadId: String, message: AiMessage) {
        repository.saveMessage(threadId, message)
    }
}
