package com.mhss.app.domain.model.backup

import com.mhss.app.domain.model.SubTask
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import kotlin.uuid.Uuid

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class BackupSubTask(
    @SerialName("title")
    @JsonNames("title", "a")
    val title: String = "",
    @SerialName("isCompleted")
    @JsonNames("isCompleted", "b")
    val isCompleted: Boolean = false,
    @SerialName("id")
    @JsonNames("id", "c")
    @Serializable(BackupStringIdSerializer::class)
    val id: String = ""
)

fun SubTask.toBackupSubTask() = BackupSubTask(
    title = title,
    isCompleted = isCompleted,
    id = id.toString()
)

fun BackupSubTask.toSubTask() = SubTask(
    title = title,
    isCompleted = isCompleted,
    id = runCatching { Uuid.parse(id) }.getOrElse { Uuid.generateV7() }
)
