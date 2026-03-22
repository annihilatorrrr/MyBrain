package com.mhss.app.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.domain.model.Mood

@Entity(tableName = "diary")
data class DiaryEntryEntity(
    val title: String = "",
    val content: String = "",
    @ColumnInfo(name = "created_date")
    val createdDate: Long = 0L,
    @ColumnInfo(name = "updated_date")
    val updatedDate: Long = 0L,
    val mood: Mood,
    @PrimaryKey
    val id: String
)

fun DiaryEntryEntity.toDiaryEntry() = DiaryEntry(
    title = title,
    content = content,
    createdDate = createdDate,
    updatedDate = updatedDate,
    mood = mood,
    id = id
)

fun DiaryEntry.toDiaryEntryEntity() = DiaryEntryEntity(
    title = title,
    content = content,
    createdDate = createdDate,
    updatedDate = updatedDate,
    mood = mood,
    id = id
)
