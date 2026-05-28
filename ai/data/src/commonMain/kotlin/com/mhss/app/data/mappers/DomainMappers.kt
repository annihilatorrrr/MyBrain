package com.mhss.app.data.mappers

import com.mhss.app.data.repository.AiToolExecutor
import com.mhss.app.database.entity.AssistantAttachmentDto
import com.mhss.app.database.entity.AssistantMessageEntity
import com.mhss.app.database.entity.AssistantMessageMetadata
import com.mhss.app.database.entity.ToolCallMetadata
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.model.AiMessageAttachment
import com.mhss.app.domain.model.AiMessageType

fun AiMessage.toAssistantMessageEntity(threadId: String): AssistantMessageEntity {
        return when (this) {
            is AiMessage.UserMessage -> {
                AssistantMessageEntity(
                    id = uuid,
                    threadId = threadId,
                    type = AiMessageType.USER.key,
                    content = content,
                    createdAt = time,
                    metadata = if (attachments.isNotEmpty()) AssistantMessageMetadata(
                        attachmentsText = attachmentsText,
                        attachments = attachments.map { it.toDto() }
                    ) else null
                )
            }

            is AiMessage.AssistantMessage -> {
                AssistantMessageEntity(
                    id = uuid,
                    threadId = threadId,
                    type = AiMessageType.ASSISTANT.key,
                    content = content,
                    createdAt = time
                )
            }

            is AiMessage.ToolCall -> {
                AssistantMessageEntity(
                    id = uuid,
                    threadId = threadId,
                    type = AiMessageType.TOOL_CALL.key,
                    content = "",
                    createdAt = time,
                    metadata = AssistantMessageMetadata(
                        toolCall = ToolCallMetadata(
                            id = id,
                            name = name,
                            rawContent = rawContent,
                            resultRawContent = resultRawContent,
                            isFailed = isFailed,
                            thoughtSignature = thoughtSignature
                        )
                    )
                )
            }
        }
    }

suspend fun AssistantMessageEntity.toAiMessage(toolExecutor: AiToolExecutor): AiMessage? =
    when (type) {
        AiMessageType.USER.key -> {
            AiMessage.UserMessage(
                uuid = id,
                content = content,
                attachmentsText = metadata?.attachmentsText.orEmpty(),
                attachments = metadata?.attachments?.map { it.toDomain() }.orEmpty(),
                time = createdAt
            )
        }

        AiMessageType.ASSISTANT.key -> {
            AiMessage.AssistantMessage(
                uuid = id,
                content = content,
                time = createdAt
            )
        }

        AiMessageType.TOOL_CALL.key -> {
            val tc = metadata?.toolCall ?: return null
            val resultObject =
                toolExecutor.extractResultObject(tc.name, tc.resultRawContent)
            AiMessage.ToolCall(
                uuid = id,
                id = tc.id,
                name = tc.name,
                rawContent = tc.rawContent,
                resultRawContent = tc.resultRawContent,
                time = createdAt,
                isFailed = tc.isFailed,
                thoughtSignature = tc.thoughtSignature,
                resultObject = resultObject
            )
        }

        else -> null
    }


private fun AiMessageAttachment.toDto(): AssistantAttachmentDto = when (this) {
    is AiMessageAttachment.Note -> AssistantAttachmentDto.Note(note)
    is AiMessageAttachment.Task -> AssistantAttachmentDto.Task(task)
    AiMessageAttachment.CalenderEvents -> AssistantAttachmentDto.CalendarEvents
}

private fun AssistantAttachmentDto.toDomain(): AiMessageAttachment = when (this) {
    is AssistantAttachmentDto.Note -> AiMessageAttachment.Note(note)
    is AssistantAttachmentDto.Task -> AiMessageAttachment.Task(task)
    AssistantAttachmentDto.CalendarEvents -> AiMessageAttachment.CalenderEvents
}
