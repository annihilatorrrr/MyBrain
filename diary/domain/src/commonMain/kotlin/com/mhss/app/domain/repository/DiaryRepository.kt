package com.mhss.app.domain.repository

import com.mhss.app.domain.model.DiaryEntry
import kotlinx.coroutines.flow.Flow

interface DiaryRepository {

    fun getAllEntries(): Flow<List<DiaryEntry>>

    suspend fun getAllFullEntries(): List<DiaryEntry>

    suspend fun getEntry(id: String): DiaryEntry?

    suspend fun searchEntries(title: String): List<DiaryEntry>

    suspend fun addEntry(diary: DiaryEntry)

    suspend fun upsertEntries(entries: List<DiaryEntry>, notifyChange: Boolean = true)

    suspend fun updateEntry(diary: DiaryEntry)

    suspend fun deleteEntry(diary: DiaryEntry)

}