package com.mhss.app.domain.model.backup

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

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
