package com.mhss.app.domain.model.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BackupTask(
    @SerialName("title")
    val title: String = "",
    @SerialName("description")
    val description: String = "",
    @SerialName("isCompleted")
    val isCompleted: Boolean = false,
    @SerialName("priority")
    val priority: Int = 0,
    @SerialName("createdDate")
    val createdDate: Long = 0L,
    @SerialName("updatedDate")
    val updatedDate: Long = 0L,
    @SerialName("subTasks")
    val subTasks: List<BackupSubTask> = emptyList(),
    @SerialName("dueDate")
    val dueDate: Long = 0L,
    @SerialName("recurring")
    val recurring: Boolean = false,
    @SerialName("frequency")
    val frequency: Int = 2,
    @SerialName("frequencyAmount")
    val frequencyAmount: Int = 1,
    @SerialName("alarmId")
    val alarmId: Int? = null,
    @SerialName("id")
    @Serializable(BackupStringIdSerializer::class)
    val id: String = ""
)
