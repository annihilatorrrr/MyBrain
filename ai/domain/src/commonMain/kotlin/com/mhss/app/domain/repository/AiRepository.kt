package com.mhss.app.domain.repository

import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.model.AssistantResult
import kotlinx.coroutines.flow.Flow

interface AiRepository {

    fun sendPrompt(prompt: String): Flow<String>

    fun sendMessage(messages: List<AiMessage>): Flow<AiMessage>
}