package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.AssistantThread
import com.mhss.app.domain.repository.AssistantChatRepository
import org.koin.core.annotation.Factory

@Factory
class SaveAssistantThreadUseCase(
    private val repository: AssistantChatRepository
) {
    suspend operator fun invoke(thread: AssistantThread) {
        repository.saveThread(thread)
    }
}
