package com.mhss.app.data.repository

import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.annotations.InternalAgentToolsApi
import ai.koog.agents.core.tools.reflect.tools
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.LLMClientException
import ai.koog.prompt.executor.clients.anthropic.AnthropicClientSettings
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openrouter.OpenRouterLLMClient
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.Message
import ai.koog.prompt.params.LLMParams
import com.mhss.app.data.EmptyAiClient
import com.mhss.app.data.buildChatPrompt
import com.mhss.app.data.buildChatSystemMessage
import com.mhss.app.data.getRootCause
import com.mhss.app.data.nano.GeminiNanoException
import com.mhss.app.data.nano.toAssistantResult
import com.mhss.app.data.nowMillis
import com.mhss.app.data.toAiMessage
import com.mhss.app.data.toLLModel
import com.mhss.app.data.toNewAssistantMessage
import com.mhss.app.data.tools.BookmarkToolSet
import com.mhss.app.data.tools.CalendarToolSet
import com.mhss.app.data.tools.DiaryToolSet
import com.mhss.app.data.tools.NoteToolSet
import com.mhss.app.data.tools.TaskToolSet
import com.mhss.app.data.tools.UtilToolSet
import com.mhss.app.domain.MAX_CONSECUTIVE_TOOL_CALLS
import com.mhss.app.domain.gemininano.GeminiNanoService
import com.mhss.app.domain.gemininano.toGeminiNanoMode
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.model.AiRepositoryException
import com.mhss.app.domain.model.AssistantResult
import com.mhss.app.domain.repository.AiRepository
import com.mhss.app.preferences.PrefsConstants.AI_PROVIDER_KEY
import com.mhss.app.preferences.PrefsConstants.AI_TOOLS_ENABLED_KEY
import com.mhss.app.preferences.domain.model.AiProvider
import com.mhss.app.preferences.domain.model.booleanPreferencesKey
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.model.stringPreferencesKey
import com.mhss.app.preferences.domain.model.toAiProvider
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Factory
class AiRepositoryImpl(
    private val getPreferenceUseCase: GetPreferenceUseCase,
    @Named("applicationScope") private val applicationScope: CoroutineScope,
    private val noteToolSet: NoteToolSet,
    private val taskToolSet: TaskToolSet,
    private val calendarToolSet: CalendarToolSet,
    private val diaryToolSet: DiaryToolSet,
    private val bookmarkToolSet: BookmarkToolSet,
    private val utilToolSet: UtilToolSet,
    private val toolExecutor: AiToolExecutor,
    private val geminiNanoService: GeminiNanoService
) : AiRepository {
    private val toolRegistry = ToolRegistry {
        tools(noteToolSet)
        tools(taskToolSet)
        tools(calendarToolSet)
        tools(diaryToolSet)
        tools(bookmarkToolSet)
        tools(utilToolSet)
    }
    private val toolDescriptors = toolRegistry.tools.map { it.descriptor }
    private var chatSystemMessage = ""
    private var llmExecutor: PromptExecutor? = null
    private var llModel: LLModel? = null
    private var toolsEnabled: Boolean = false
    private var selectedProvider: AiProvider = AiProvider.None
    private var geminiNanoModel: String = AiProvider.GeminiNano.defaultModel.orEmpty()

    init {
        applicationScope.launch {
            configureAiClient()
        }
    }

    override suspend fun sendPrompt(prompt: String): AssistantResult<String> = withContext(Dispatchers.IO) {
        if (selectedProvider == AiProvider.GeminiNano) {
            return@withContext try {
                val result = geminiNanoService.sendPrompt(
                    prompt = prompt,
                    mode = geminiNanoModel.toGeminiNanoMode()
                )
                AssistantResult.Success(result)
            } catch (e: GeminiNanoException) {
                e.toAssistantResult()
            } catch (e: Exception) {
                e.printStackTrace()
                AssistantResult.OtherError(e.getRootCause().message ?: e.message)
            }
        }

        val client = llmExecutor ?: return@withContext AssistantResult.OtherError()
        val model = llModel ?: return@withContext AssistantResult.OtherError()

        val llmPrompt = prompt("user_prompt", LLMParams()) {
            user(prompt)
        }

        return@withContext try {
            val result = client.execute(prompt = llmPrompt, model = model)
            AssistantResult.Success(result.first().content)
        } catch (e: LLMClientException) {
            AssistantResult.OtherError(e.message)
        } catch (e: IOException) {
            e.printStackTrace()
            AssistantResult.InternetError
        } catch (e: Exception) {
            e.printStackTrace()
            AssistantResult.OtherError(e.getRootCause().message ?: e.message)
        }
    }

    @OptIn(InternalAgentToolsApi::class, ExperimentalUuidApi::class)
    override fun sendMessage(messages: List<AiMessage>): Flow<AiMessage> = flow {
        if (selectedProvider == AiProvider.GeminiNano) {
            try {
                val result = geminiNanoService.sendMessage(
                    messages = messages,
                    systemMessage = chatSystemMessage.ifBlank { buildChatSystemMessage(false) },
                    mode = geminiNanoModel.toGeminiNanoMode()
                )
                emit(
                    AiMessage.AssistantMessage(
                        uuid = Uuid.generateV7().toString(),
                        content = result,
                        time = nowMillis()
                    )
                )
            } catch (e: GeminiNanoException) {
                throw AiRepositoryException(e.toAssistantResult())
            } catch (e: Exception) {
                e.printStackTrace()
                val message = e.getRootCause().message ?: e.message
                throw AiRepositoryException(AssistantResult.OtherError(message))
            }
            return@flow
        }

        val model =
            llModel ?: throw AiRepositoryException(AssistantResult.OtherError("Model not selected"))
        val executor = llmExecutor
            ?: throw AiRepositoryException(AssistantResult.OtherError("AI Client not initialized"))

        var currentMessages = messages
        var consecutiveToolCalls = 0

        try {
            do {
                if (consecutiveToolCalls >= MAX_CONSECUTIVE_TOOL_CALLS) {
                    throw AiRepositoryException(AssistantResult.ToolCallLimitExceeded)
                }

                val result = executor.execute(
                    prompt = currentMessages.buildChatPrompt(chatSystemMessage),
                    model = model,
                    tools = if (toolsEnabled) toolDescriptors else emptyList()
                )

                val toolCalls = result.filterIsInstance<Message.Tool.Call>()
                val assistantMessage =
                    result.filterIsInstance<Message.Assistant>().firstOrNull()
                        ?.toNewAssistantMessage()

                if (toolCalls.isEmpty()) {
                    assistantMessage?.let { emit(it) }
                    break
                }

                consecutiveToolCalls++

                val thoughtSignatures = toolExecutor.extractThoughtSignatures(result)
                val toolCallMessages = toolCalls.map { toolCall ->
                    val toolCallMessageResult = toolExecutor.executeToolCall(toolCall, toolRegistry)
                    toolCall.toAiMessage(toolCallMessageResult, thoughtSignatures[toolCall]).also {
                        emit(it)
                    }
                }

                currentMessages = currentMessages + toolCallMessages

                if (assistantMessage != null) {
                    emit(assistantMessage)
                    currentMessages = currentMessages + assistantMessage
                }

            } while (toolCalls.isNotEmpty())
        } catch (e: AiRepositoryException) {
            throw e
        } catch (e: LLMClientException) {
            throw AiRepositoryException(AssistantResult.OtherError(e.message))
        } catch (e: IOException) {
            e.printStackTrace()
            throw AiRepositoryException(AssistantResult.InternetError)
        } catch (e: Exception) {
            e.printStackTrace()
            val message = e.getRootCause().message ?: e.message
            throw AiRepositoryException(AssistantResult.OtherError(message))
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun configureAiClient() {
        val aiProvider = getPreferenceUseCase(
            intPreferencesKey(AI_PROVIDER_KEY),
            AiProvider.None.id
        ).first().toAiProvider()

        if (aiProvider == AiProvider.None) {
            llmExecutor = null
            llModel = null
            chatSystemMessage = ""
            return
        }

        val toolsEnabledPreferenceValue = getPreferenceUseCase(
            booleanPreferencesKey(AI_TOOLS_ENABLED_KEY),
            false
        ).first()

        selectedProvider = aiProvider
        toolsEnabled = toolsEnabledPreferenceValue && aiProvider != AiProvider.GeminiNano

        val model = getPreferenceUseCase(
            stringPreferencesKey(aiProvider.modelPref ?: ""),
            aiProvider.defaultModel.orEmpty()
        ).first()

        if (aiProvider == AiProvider.GeminiNano) {
            geminiNanoModel = model
            llmExecutor = null
            llModel = null
            chatSystemMessage = buildChatSystemMessage(false)
            geminiNanoService.warmup(geminiNanoModel.toGeminiNanoMode())
            return
        }

        val key = getPreferenceUseCase(
            stringPreferencesKey(aiProvider.keyPref ?: "none"),
            ""
        ).first()

        val customUrlPref = aiProvider.customUrlPref
        val customUrl = if (aiProvider.supportsCustomUrl && customUrlPref != null) {
            getPreferenceUseCase(
                stringPreferencesKey(customUrlPref),
                ""
            ).first()
        } else {
            ""
        }

        llModel = if (model.isNotBlank()) {
            model.toLLModel(aiProvider, withTools = toolsEnabled)
        } else {
            null
        }

        llmExecutor = llModel?.let {
            aiProvider.getExecutor(key, customUrl, it)
        }

        chatSystemMessage = buildChatSystemMessage(toolsEnabled)
    }

}

private fun AiProvider.getExecutor(key: String, customUrl: String, llModel: LLModel): PromptExecutor {
    val client = when (this) {
        AiProvider.OpenAI -> OpenAILLMClient(
            apiKey = key,
            settings = if (customUrl.isBlank()) OpenAIClientSettings() else OpenAIClientSettings(baseUrl = customUrl)
        )
        AiProvider.Gemini -> GoogleLLMClient(apiKey = key)
        AiProvider.Anthropic -> AnthropicLLMClient(
            apiKey = key,
            settings = AnthropicClientSettings(
                modelVersionsMap = mapOf(llModel to llModel.id)
            )
        )
        AiProvider.OpenRouter -> OpenRouterLLMClient(apiKey = key)
        AiProvider.Ollama -> if (customUrl.isBlank()) OllamaClient() else OllamaClient(customUrl)
        AiProvider.LmStudio -> OpenAILLMClient(
            apiKey = "",
            settings = OpenAIClientSettings(baseUrl = customUrl)
        )
        AiProvider.GeminiNano -> EmptyAiClient
        AiProvider.None -> EmptyAiClient
    }
    return SingleLLMPromptExecutor(client)
}
