package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.repository.AiRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class SendAiMessageUseCase(private val aiRepository: AiRepository) {
    operator fun invoke(messages: List<AiMessage>): Flow<AiMessage> {
        return aiRepository.sendMessage(messages)
    }
}