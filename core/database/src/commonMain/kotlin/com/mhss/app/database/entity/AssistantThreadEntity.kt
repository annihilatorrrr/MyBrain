package com.mhss.app.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "assistant_threads",
    indices = [Index(value = ["sync_seq"])]
)
data class AssistantThreadEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "sync_seq", defaultValue = "1")
    val syncSeq: Long = 1L
)
