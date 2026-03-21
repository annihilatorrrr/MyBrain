package com.mhss.app.domain.repository

import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.model.AssistantResult
import kotlinx.coroutines.flow.Flow

interface AiRepository {

    suspend fun sendPrompt(prompt: String): AssistantResult<String>

    fun sendMessage(messages: List<AiMessage>): Flow<AiMessage>
}