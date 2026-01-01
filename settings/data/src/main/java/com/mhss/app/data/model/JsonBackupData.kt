package com.mhss.app.data.model

import com.mhss.app.database.entity.BookmarkEntity
import com.mhss.app.database.entity.DiaryEntryEntity
import com.mhss.app.database.entity.NoteEntity
import com.mhss.app.database.entity.NoteFolderEntity
import com.mhss.app.database.entity.TaskEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JsonBackupData(
    @SerialName("notes") val notes: List<NoteEntity> = emptyList(),
    @SerialName("noteFolders") val noteFolders: List<NoteFolderEntity> = emptyList(),
    @SerialName("tasks") val tasks: List<TaskEntity> = emptyList(),
    @SerialName("diary") val diary: List<DiaryEntryEntity> = emptyList(),
    @SerialName("bookmarks") val bookmarks: List<BookmarkEntity> = emptyList()
)