package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.AiRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class SendAiPromptUseCase(private val aiRepository: AiRepository) {
    operator fun invoke(prompt: String): Flow<String> {
        return aiRepository.sendPrompt(prompt)
    }
}