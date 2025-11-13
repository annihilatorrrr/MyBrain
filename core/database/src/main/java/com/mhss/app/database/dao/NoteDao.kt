package com.mhss.app.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.mhss.app.database.entity.NoteEntity
import com.mhss.app.database.entity.NoteFolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT title, SUBSTR(content, 1, 200) AS content, created_date, updated_date, pinned, folder_id, id FROM notes WHERE folder_id IS NULL")
    fun getAllFolderlessNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes")
    suspend fun getAllNotes(): List<NoteEntity>

    @Query("SELECT * FROM notes")
    suspend fun getAllFullNotes(): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNote(id: String): NoteEntity?

    @Query("SELECT title, SUBSTR(content, 1, 200) AS content, created_date, updated_date, pinned, folder_id, id FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    suspend fun getNotesByTitle(query: String): List<NoteEntity>

    @Query("SELECT title, SUBSTR(content, 1, 200) AS content, created_date, updated_date, pinned, folder_id, id FROM notes WHERE folder_id = :folderId")
    fun getNotesByFolder(folderId: String): Flow<List<NoteEntity>>

    @Upsert
    suspend fun upsertNote(note: NoteEntity)

    @Upsert
    suspend fun upsertNotes(notes: List<NoteEntity>)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteFolder(folder: NoteFolderEntity)

    @Upsert
    suspend fun upsertNoteFolders(folders: List<NoteFolderEntity>)

    @Update
    suspend fun updateNoteFolder(folder: NoteFolderEntity)

    @Delete
    suspend fun deleteNoteFolder(folder: NoteFolderEntity)

    @Query("SELECT * FROM note_folders")
    fun getAllNoteFolders(): Flow<List<NoteFolderEntity>>

    @Query("SELECT * FROM note_folders WHERE id = :folderId")
    fun getNoteFolder(folderId: String): NoteFolderEntity?
}