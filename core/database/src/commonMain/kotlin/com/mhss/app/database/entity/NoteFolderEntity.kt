package com.mhss.app.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.mhss.app.domain.model.NoteFolder

@Entity(
    tableName = "note_folders",
)
data class NoteFolderEntity(
    val name: String = "",
    @PrimaryKey
    val id: String
)

fun NoteFolderEntity.toNoteFolder(): NoteFolder {
    return NoteFolder(
        name = name,
        id = id,
    )
}

fun NoteFolder.toNoteFolderEntity(): NoteFolderEntity {
    return NoteFolderEntity(
        name = name,
        id = id,
    )
}
