package com.mhss.app.data

import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.model.AiMessageType
import com.mhss.app.preferences.domain.model.AiProvider
import kotlin.time.Clock

fun String.toLLModel(provider: AiProvider, withTools: Boolean) = LLModel(
    provider = provider.toLLMProvider(),
    id = this,
    capabilities = buildList {
        if (withTools) {
            add(LLMCapability.Tools)
            add(LLMCapability.ToolChoice)
        }
        add(LLMCapability.Completion)
        if (provider == AiProvider.OpenAI){
            add(LLMCapability.OpenAIEndpoint.Responses)
            add(LLMCapability.OpenAIEndpoint.Completions)
        }
    },
    contextLength = 128_000,
    maxOutputTokens = 32_000,
)

fun AiProvider.toLLMProvider() = when (this) {
    AiProvider.OpenAI -> LLMProvider.OpenAI
    AiProvider.Gemini -> LLMProvider.Google
    AiProvider.Anthropic -> LLMProvider.Anthropic
    AiProvider.OpenRouter -> LLMProvider.OpenRouter
    AiProvider.Ollama -> LLMProvider.Ollama
    AiProvider.None -> LLMProvider.OpenAI // just a placeholder
}

fun String.toAssistantAiMessage() = AiMessage(
    content = this,
    type = AiMessageType.MODEL,
    time = Clock.System.now().toEpochMilliseconds()
)