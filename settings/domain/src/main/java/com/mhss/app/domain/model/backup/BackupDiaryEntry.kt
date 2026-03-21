package com.mhss.app.domain.model.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BackupDiaryEntry(
    @SerialName("title")
    val title: String = "",
    @SerialName("content")
    val content: String = "",
    @SerialName("createdDate")
    val createdDate: Long = 0L,
    @SerialName("updatedDate")
    val updatedDate: Long = 0L,
    @SerialName("mood")
    val mood: String = "OKAY",
    @SerialName("id")
    @Serializable(BackupStringIdSerializer::class)
    val id: String = ""
)
