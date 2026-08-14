package com.mhss.app.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.mhss.app.domain.model.NoteFolder

@Entity(
    tableName = "note_folders",
    indices = [Index(value = ["sync_seq"])]
)
data class NoteFolderEntity(
    val name: String = "",
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "sync_seq", defaultValue = "1")
    val syncSeq: Long = 1L,
    @ColumnInfo(name = "updated_date", defaultValue = "0")
    val updatedDate: Long = 0L,
)

fun NoteFolderEntity.toNoteFolder(): NoteFolder {
    return NoteFolder(
        name = name,
        id = id,
        updatedDate = updatedDate
    )
}

fun NoteFolder.toNoteFolderEntity(id: String = this.id, syncSeq: Long = 0L): NoteFolderEntity {
    return NoteFolderEntity(
        name = name,
        id = id,
        syncSeq = syncSeq,
        updatedDate = updatedDate
    )
}
