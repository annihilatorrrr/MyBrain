@file:Suppress("UnsafeOptInUsageError")
package com.mhss.app.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "assistant_messages",
    indices = [
        Index(value = ["thread_id"]),
        Index(value = ["sync_seq"])
    ]
)
data class AssistantMessageEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "thread_id")
    val threadId: String,
    val type: Int,
    val content: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    val metadata: AssistantMessageMetadata? = null,
    @ColumnInfo(name = "sync_seq", defaultValue = "1")
    val syncSeq: Long = 1L
)

@Serializable
data class AssistantMessageMetadata(
    @SerialName("attachmentsText") val attachmentsText: String? = null,
    @SerialName("toolCall") val toolCall: ToolCallMetadata? = null,
    @SerialName("attachments") val attachments: List<AssistantAttachmentDto>? = null
)

@Serializable
sealed interface AssistantAttachmentDto {
    @Serializable
    @SerialName("note")
    data class Note(val note: com.mhss.app.domain.model.Note) : AssistantAttachmentDto


    @Serializable
    @SerialName("task")
    data class Task(val task: com.mhss.app.domain.model.Task) : AssistantAttachmentDto

    @Serializable
    @SerialName("calendar_events")
    data object CalendarEvents : AssistantAttachmentDto
}

@Serializable
data class ToolCallMetadata(
    @SerialName("id") val id: String?,
    @SerialName("name") val name: String,
    @SerialName("rawContent") val rawContent: String = "",
    @SerialName("resultRawContent") val resultRawContent: String = "",
    @SerialName("isFailed") val isFailed: Boolean = false,
    @SerialName("thoughtSignature") val thoughtSignature: String? = null
)
