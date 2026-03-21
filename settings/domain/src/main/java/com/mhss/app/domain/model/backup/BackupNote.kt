package com.mhss.app.domain.model.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BackupNote(
    @SerialName("title")
    val title: String = "",
    @SerialName("content")
    val content: String = "",
    @SerialName("createdDate")
    val createdDate: Long = 0L,
    @SerialName("updatedDate")
    val updatedDate: Long = 0L,
    @SerialName("pinned")
    val pinned: Boolean = false,
    @SerialName("folderId")
    @Serializable(BackupNullableStringIdSerializer::class)
    val folderId: String? = null,
    @SerialName("id")
    @Serializable(BackupStringIdSerializer::class)
    val id: String = ""
)

@Serializable
data class BackupNoteFolder(
    @SerialName("name")
    val name: String = "",
    @SerialName("id")
    @Serializable(BackupStringIdSerializer::class)
    val id: String = ""
)
