package com.mhss.app.domain.model.backup

import com.mhss.app.domain.model.Bookmark
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BackupBookmark(
    @SerialName("url")
    val url: String = "",
    @SerialName("title")
    val title: String = "",
    @SerialName("description")
    val description: String = "",
    @SerialName("createdDate")
    val createdDate: Long = 0L,
    @SerialName("updatedDate")
    val updatedDate: Long = 0L,
    @SerialName("id")
    @Serializable(BackupStringIdSerializer::class)
    val id: String = ""
)

fun Bookmark.toBackupBookmark() = BackupBookmark(
    url = url,
    title = title,
    description = description,
    createdDate = createdDate,
    updatedDate = updatedDate,
    id = id
)

fun BackupBookmark.toBookmark() = Bookmark(
    url = url,
    title = title,
    description = description,
    createdDate = createdDate,
    updatedDate = updatedDate,
    id = id
)
