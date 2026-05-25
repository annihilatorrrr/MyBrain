package com.mhss.app.domain.gemininano

import com.mhss.app.domain.model.AiMessage
import kotlinx.coroutines.flow.StateFlow

enum class GeminiNanoMode(val value: String) {
    Fast("fast"),
    Full("full")
}

fun String.toGeminiNanoMode(): GeminiNanoMode {
    return GeminiNanoMode.entries.firstOrNull { it.value == this } ?: GeminiNanoMode.Full
}

sealed interface GeminiNanoStatus {
    data object Checking : GeminiNanoStatus
    data object Ready : GeminiNanoStatus
    data class Downloadable(val progress: Float = 0f, val isDownloading: Boolean = false) : GeminiNanoStatus
    data class Unsupported(val reason: String? = null) : GeminiNanoStatus
    data class Error(val message: String) : GeminiNanoStatus
}

interface GeminiNanoService {
    val status: StateFlow<GeminiNanoStatus>

    suspend fun warmup(mode: GeminiNanoMode)

    suspend fun refreshStatus(mode: GeminiNanoMode)

    fun download(mode: GeminiNanoMode)

    suspend fun sendMessage(
        messages: List<AiMessage>,
        systemMessage: String,
        mode: GeminiNanoMode
    ): String

    suspend fun sendPrompt(
        prompt: String,
        mode: GeminiNanoMode
    ): String
}
