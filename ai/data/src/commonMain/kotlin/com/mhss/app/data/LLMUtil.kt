package com.mhss.app.data

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.MessagePart
import ai.koog.prompt.params.LLMParams
import com.mhss.app.domain.baseChatSystemMessage
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.toolsSystemMessage
import com.mhss.app.preferences.domain.model.AiProvider
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


fun List<AiMessage>.buildChatPrompt(systemMessage: String) = prompt("chat_prompt", LLMParams()) {
    system(systemMessage)
    forEach { message ->
        when (message) {
            is AiMessage.UserMessage -> user(message.content + message.attachmentsText)
            is AiMessage.AssistantMessage -> assistant(message.content)
            is AiMessage.ToolCall -> {
                assistant {
                    if (message.thoughtSignature != null) {
                        reasoning(
                            MessagePart.Reasoning(
                                content = emptyList(),
                                encrypted = message.thoughtSignature
                            )
                        )
                    }
                    toolCall(
                        MessagePart.Tool.Call(
                            id = message.id,
                            tool = message.name,
                            args = message.rawContent
                        )
                    )
                }
                toolResult(
                    MessagePart.Tool.Result(
                        id = message.id,
                        tool = message.name,
                        output = message.resultRawContent,
                        isError = message.isFailed
                    )
                )
            }
        }
    }
}


@OptIn(ExperimentalUuidApi::class)
fun MessagePart.Tool.Call.toAiMessage(
    toolCallResult: Result<AiMessage.ToolCall>,
    thoughtSignature: String? = null
): AiMessage {
    return toolCallResult.getOrNull()?.copy(thoughtSignature = thoughtSignature)
        ?: AiMessage.ToolCall(
            uuid = Uuid.generateV7().toString(),
            id = id,
            name = tool,
            rawContent = args,
            resultRawContent = toolCallResult.exceptionOrNull()
                ?.getRootCause()
                ?.toString()
                ?: "Error executing tool",
            time = nowMillis(),
            isFailed = true,
            thoughtSignature = thoughtSignature
        )
}

fun String.toLLModel(provider: AiProvider, withTools: Boolean): LLModel {
    val llmProvider = provider.toLLMProvider()
    return LLModel(
        provider = llmProvider,
        id = this,
        capabilities = buildList {
            // Koog internally throws if an Anthropic model doesn't declare tool capabilities
            if (withTools || provider == AiProvider.Anthropic) {
                add(LLMCapability.Tools)
                add(LLMCapability.ToolChoice)
            }
            if (provider == AiProvider.Gemini) {
                add(LLMCapability.Thinking)
            }
            add(LLMCapability.Completion)
            if (llmProvider == LLMProvider.OpenAI){
                add(LLMCapability.OpenAIEndpoint.Responses)
                add(LLMCapability.OpenAIEndpoint.Completions)
            }
        },
        contextLength = 128_000,
        maxOutputTokens = 32_000,
    )
}

fun AiProvider.toLLMProvider() = when (this) {
    AiProvider.OpenAI -> LLMProvider.OpenAI
    AiProvider.Gemini -> LLMProvider.Google
    AiProvider.Anthropic -> LLMProvider.Anthropic
    AiProvider.OpenRouter -> LLMProvider.OpenRouter
    AiProvider.Ollama -> LLMProvider.Ollama
    AiProvider.LmStudio -> LLMProvider.OpenAI
    AiProvider.GeminiNano -> LLMProvider.Google
    AiProvider.None -> LLMProvider.OpenAI // just a placeholder
}

@OptIn(ExperimentalUuidApi::class)
fun Message.Assistant.toNewAssistantMessage() = AiMessage.AssistantMessage(
    uuid = Uuid.generateV7().toString(),
    content = textContent(),
    time = nowMillis()
)

internal fun nowMillis() = Clock.System.now().toEpochMilliseconds()
private val currentTimeZone = TimeZone.currentSystemDefault()
internal fun currentLocalDateTime() = Clock.System.now().toLocalDateTime(currentTimeZone)
internal const val llmDateTimeFormatUnicode = "HH:mm dd-MM-yyyy"

internal val llmDateTimeWithDayNameFormat = LocalDateTime.Format {
    hour(); char(':'); minute();
    char(' ');
    dayOfWeek(DayOfWeekNames.ENGLISH_FULL)
    char(' ');
    day(); char('-'); monthNumber(); char('-'); year()
}
internal val llmDateTimeFormat = LocalDateTime.Format {
    byUnicodePattern(llmDateTimeFormatUnicode)
}
internal fun String.parseDateTimeFromLLM() = runCatching {
    LocalDateTime.parse(this, llmDateTimeFormat).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}.getOrNull()
internal fun Long.formatDateTimeForLLM() =
    Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(currentTimeZone)
        .format(llmDateTimeWithDayNameFormat)

fun buildChatSystemMessage(toolsEnabled: Boolean) = buildString {
    appendLine(baseChatSystemMessage)
    if (toolsEnabled) appendLine(toolsSystemMessage)
    append("Current date & time: ")
    appendLine(currentLocalDateTime().format(llmDateTimeWithDayNameFormat))
    append("Time zone: "); append(currentTimeZone)
}

fun Throwable.getRootCause(): Throwable {
    var rootCause: Throwable? = this
    while (rootCause?.cause != null) {
        rootCause = rootCause.cause
    }
    return rootCause ?: this
}

internal val json = Json { ignoreUnknownKeys = true }
