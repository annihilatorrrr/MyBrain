package com.mhss.app.domain.model.backup

import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.domain.model.Mood
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

fun DiaryEntry.toBackupDiaryEntry() = BackupDiaryEntry(
    title = title,
    content = content,
    createdDate = createdDate,
    updatedDate = updatedDate,
    mood = mood.name,
    id = id
)

fun BackupDiaryEntry.toDiaryEntry() = DiaryEntry(
    title = title,
    content = content,
    createdDate = createdDate,
    updatedDate = updatedDate,
    mood = Mood.entries.firstOrNull { it.name == mood } ?: Mood.OKAY,
    id = id
)
