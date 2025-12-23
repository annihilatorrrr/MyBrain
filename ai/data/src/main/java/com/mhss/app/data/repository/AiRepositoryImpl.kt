package com.mhss.app.data.repository

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.annotations.InternalAgentToolsApi
import ai.koog.agents.core.tools.reflect.tools
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.LLMClientException
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
import com.mhss.app.data.nowMillis
import com.mhss.app.data.toAiMessage
import com.mhss.app.data.toLLModel
import com.mhss.app.data.toNewAssistantMessage
import com.mhss.app.data.tools.BookmarkToolSet
import com.mhss.app.data.tools.CalendarToolSet
import com.mhss.app.data.tools.DiaryToolSet
import com.mhss.app.data.tools.NoteToolSet
import com.mhss.app.data.tools.TaskToolSet
import com.mhss.app.domain.MAX_CONSECUTIVE_TOOL_CALLS
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named
import kotlin.uuid.Uuid

@Factory
class AiRepositoryImpl(
    private val getPreferenceUseCase: GetPreferenceUseCase,
    @Named("applicationScope") private val applicationScope: CoroutineScope,
    private val noteToolSet: NoteToolSet,
    private val taskToolSet: TaskToolSet,
    private val calendarToolSet: CalendarToolSet,
    private val diaryToolSet: DiaryToolSet,
    private val bookmarkToolSet: BookmarkToolSet
) : AiRepository {

    private val toolRegistry = ToolRegistry {
        tools(noteToolSet)
        tools(taskToolSet)
        tools(calendarToolSet)
        tools(diaryToolSet)
        tools(bookmarkToolSet)
    }
    private val toolDescriptors = toolRegistry.tools.map { it.descriptor }
    private var chatSystemMessage = ""
    private var llmExecutor: PromptExecutor? = null
    private var llModel: LLModel? = null
    private var toolsEnabled: Boolean = false

    init {
        applicationScope.launch {
            val aiProvider = getPreferenceUseCase(
                intPreferencesKey(AI_PROVIDER_KEY),
                AiProvider.None.id
            ).first().toAiProvider()

            val toolsEnabledPreferenceValue = getPreferenceUseCase(
                booleanPreferencesKey(AI_TOOLS_ENABLED_KEY),
                false
            ).first()

            toolsEnabled = toolsEnabledPreferenceValue

            if (aiProvider == AiProvider.None) {
                llmExecutor = null
                llModel = null
                chatSystemMessage = ""
                return@launch
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

            llmExecutor = aiProvider.getExecutor(key, customUrl)

            val model = getPreferenceUseCase(
                stringPreferencesKey(aiProvider.modelPref ?: ""),
                ""
            ).first()

            if (model.isNotBlank()) {
                llModel = model.toLLModel(aiProvider, withTools = toolsEnabledPreferenceValue)
            }

            chatSystemMessage = buildChatSystemMessage(toolsEnabledPreferenceValue)
        }
    }

    override suspend fun sendPrompt(prompt: String): AssistantResult<String> {
        val client = llmExecutor ?: return AssistantResult.OtherError()
        val model = llModel ?: return AssistantResult.OtherError()

        val llmPrompt = prompt("user_prompt", LLMParams()) {
            user(prompt)
        }

        return try {
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

    @OptIn(InternalAgentToolsApi::class)
    override fun sendMessage(messages: List<AiMessage>): Flow<AiMessage> = flow {
        val executor = llmExecutor
            ?: throw AiRepositoryException(AssistantResult.OtherError("AI Client not initialized"))
        val model =
            llModel ?: throw AiRepositoryException(AssistantResult.OtherError("Model not selected"))

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

                val toolCallMessages = toolCalls.map { toolCall ->
                    val toolCallMessageResult = executeToolCall(toolCall)
                    toolCall.toAiMessage(toolCallMessageResult).also {
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
    }

    private suspend fun executeToolCall(
        toolCall: Message.Tool.Call
    ): Result<AiMessage.ToolCall> = runCatching {
        val tool = toolRegistry.getTool(toolCall.tool)
        val args = tool.decodeArgs(toolCall.contentJson)
        val toolResult = (tool as Tool<Any?, Any?>).execute(args)
        val resultJson = tool.encodeResult(toolResult).toString()
        AiMessage.ToolCall(
            uuid = Uuid.random().toString(),
            id = toolCall.id,
            name = tool.name,
            rawContent = toolCall.content,
            resultRawContent = resultJson,
            time = nowMillis()
        )
    }
}

private fun AiProvider.getExecutor(key: String, customUrl: String): PromptExecutor {
    val client = when (this) {
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
    return SingleLLMPromptExecutor(client)
}