package com.mhss.app.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "deleted_entities",
    indices = [
        Index(value = ["sync_seq"]),
        Index(value = ["entity_type", "entity_id"], unique = true)
    ]
)
data class DeletedEntityEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "entity_id")
    val entityId: String,
    @ColumnInfo(name = "entity_type")
    val entityType: String,
    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long,
    @ColumnInfo(name = "sync_seq", defaultValue = "1")
    val syncSeq: Long = 1L
)

enum class DeletedEntityType(val key: String) {
    NOTE("note"),
    NOTE_FOLDER("note_folder"),
    TASK("task"),
    DIARY("diary"),
    BOOKMARK("bookmark"),
    THREAD("thread"),
    MESSAGE("message")
}
