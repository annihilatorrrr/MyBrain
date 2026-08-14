package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.repository.AssistantChatRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetThreadMessagesUseCase(
    private val repository: AssistantChatRepository
) {
    operator fun invoke(threadId: String): Flow<List<AiMessage>> = repository.getMessages(threadId)
}
