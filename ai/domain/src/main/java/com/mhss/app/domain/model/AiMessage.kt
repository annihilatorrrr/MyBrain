package com.mhss.app.domain.model

sealed interface AiMessage {
    data class UserMessage(
        val uuid: String,
        val content: String,
        val time: Long,
        val attachments: List<AiMessageAttachment> = emptyList(),
        val attachmentsText: String = ""
    ) : AiMessage

    data class AssistantMessage(val content: String, val time: Long, val uuid: String) : AiMessage

    data class ToolCall(
        val uuid: String,
        val id: String?,
        val name: String,
        val rawContent: String,
        val resultRawContent: String,
        val time: Long,
        val isFailed: Boolean = false
    ): AiMessage
}

// old ai message
//data class AiMessage(
//    val content: String,
//    val type: AiMessageType,
//    val time: Long,
//    val attachments: List<AiMessageAttachment> = emptyList(),
//    val attachmentsText: String = ""
//)

sealed interface AiMessageAttachment {
    data class Note(val note: com.mhss.app.domain.model.Note) : AiMessageAttachment
    data class Task(val task: com.mhss.app.domain.model.Task) : AiMessageAttachment
    data object CalenderEvents : AiMessageAttachment
}

// old, now replaced with sealed interface
//enum class AiMessageType {
//    USER,
//    MODEL
//}
