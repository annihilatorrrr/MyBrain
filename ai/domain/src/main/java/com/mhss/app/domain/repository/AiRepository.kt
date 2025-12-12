package com.mhss.app.domain.repository

import com.mhss.app.domain.model.AiMessage
import com.mhss.app.network.NetworkResult

interface AiRepository {

    suspend fun sendPrompt(prompt: String): NetworkResult<String>

    suspend fun sendMessage(messages: List<AiMessage>): NetworkResult<AiMessage>
}