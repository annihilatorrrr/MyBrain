package com.mhss.app.data.repository

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.LLMClient
import ai.koog.prompt.executor.clients.LLMClientException
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openrouter.OpenRouterLLMClient
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.params.LLMParams
import com.mhss.app.data.EmptyAiClient
import com.mhss.app.data.toAssistantAiMessage
import com.mhss.app.data.toLLModel
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.model.AiMessageType
import com.mhss.app.domain.repository.AiRepository
import com.mhss.app.domain.systemMessage
import com.mhss.app.network.NetworkResult
import com.mhss.app.preferences.PrefsConstants.AI_PROVIDER_KEY
import com.mhss.app.preferences.domain.model.AiProvider
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.model.stringPreferencesKey
import com.mhss.app.preferences.domain.model.toAiProvider
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class AiRepositoryImpl(
    private val getPreferenceUseCase: GetPreferenceUseCase,
    @Named("applicationScope") private val applicationScope: CoroutineScope,
) : AiRepository {

    init {
        applicationScope.launch {
            getPreferenceUseCase(
                intPreferencesKey(AI_PROVIDER_KEY),
                AiProvider.None.id
            ).collectLatest {
                val provider = it.toAiProvider()
                val customUrlPref = provider.customUrlPref
                if (provider != AiProvider.None) {
                    coroutineScope {
                        launch {
                            combine(
                                getPreferenceUseCase(
                                    stringPreferencesKey(provider.keyPref ?: "none"),
                                    ""
                                ),
                                if (provider.supportsCustomUrl && customUrlPref != null) getPreferenceUseCase(
                                    stringPreferencesKey(customUrlPref),
                                    ""
                                ) else flowOf("")
                            ) { (key, customUrl) ->
                                try {
                                    llmClient?.close()
                                } finally {
                                    llmClient = provider.getClient(key, customUrl)
                                }
                            }.collect()
                        }
                        launch {
                            getPreferenceUseCase(
                                stringPreferencesKey(provider.modelPref ?: return@launch),
                                ""
                            ).collect { model ->
                                if (model.isNotBlank()) {
                                    llModel = model.toLLModel(provider, withTools = false)
                                }
                            }
                        }
                    }
                } else {
                    llmClient?.close()
                }
            }
        }
    }

    private var llmClient: LLMClient? = null
    private var llModel: LLModel? = null

    override suspend fun sendPrompt(prompt: String): NetworkResult<String> {
        val client = llmClient ?: return NetworkResult.OtherError()
        val model = llModel ?: return NetworkResult.OtherError()

        val llmPrompt = prompt("user_prompt", LLMParams()) {
            user(prompt)
        }

        return try {
            val result = client.execute(prompt = llmPrompt, model = model)
            NetworkResult.Success(result.first().content)
        } catch (e: LLMClientException) {
            NetworkResult.OtherError(e.message)
        } catch (e: IOException) {
            e.printStackTrace()
            NetworkResult.InternetError
        } catch (e: Exception) {
            e.printStackTrace()
            NetworkResult.OtherError()
        }
    }

    override suspend fun sendMessage(messages: List<AiMessage>): NetworkResult<AiMessage> {
        val client = llmClient ?: return NetworkResult.OtherError()
        val model = llModel ?: return NetworkResult.OtherError()

        val llmPrompt = prompt("user_prompt", LLMParams()) {
            system(systemMessage)

            messages.forEach { message ->
                if (message.type == AiMessageType.USER) user(message.content)
                else if (message.type == AiMessageType.MODEL) assistant(message.content)
            }
        }

        return try {
            val result = client.execute(prompt = llmPrompt, model = model)
            NetworkResult.Success(result.first().content.toAssistantAiMessage())
        } catch (e: LLMClientException) {
            NetworkResult.OtherError(e.message)
        } catch (e: IOException) {
            e.printStackTrace()
            NetworkResult.InternetError
        } catch (e: Exception) {
            e.printStackTrace()
            NetworkResult.OtherError(e.message)
        }
    }
}

private fun AiProvider.getClient(key: String, customUrl: String): LLMClient {
    return when (this) {
        AiProvider.OpenAI -> OpenAILLMClient(
            apiKey = key,
            settings = if (customUrl.isBlank()) OpenAIClientSettings() else OpenAIClientSettings(
                baseUrl = customUrl
            )
        )

        AiProvider.Gemini -> GoogleLLMClient(apiKey = key)
        AiProvider.Anthropic -> AnthropicLLMClient(apiKey = key)
        AiProvider.OpenRouter -> OpenRouterLLMClient(apiKey = key)
        AiProvider.Ollama -> if (customUrl.isBlank()) OllamaClient() else OllamaClient(customUrl)
        AiProvider.None -> EmptyAiClient
    }
}