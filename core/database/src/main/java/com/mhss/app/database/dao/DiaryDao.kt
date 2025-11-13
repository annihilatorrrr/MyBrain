package com.mhss.app.database.dao

import androidx.room.*
import com.mhss.app.database.entity.DiaryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    @Query("SELECT title, SUBSTR(content, 1, 200) AS content, created_date, updated_date, mood, id FROM diary")
    fun getAllEntries(): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary")
    suspend fun getAllFullEntries(): List<DiaryEntryEntity>

    @Query("SELECT * FROM diary WHERE id = :id")
    suspend fun getEntry(id: String): DiaryEntryEntity

    @Query("SELECT title, SUBSTR(content, 1, 200) AS content, created_date, updated_date, mood, id FROM diary WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    suspend fun getEntriesByTitle(query: String): List<DiaryEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(diary: DiaryEntryEntity)

    @Upsert
    suspend fun upsertEntries(diary: List<DiaryEntryEntity>)

    @Update
    suspend fun updateEntry(diary: DiaryEntryEntity)

    @Delete
    suspend fun deleteEntry(diary: DiaryEntryEntity)

}