package com.mhss.app.data.storage

import com.mhss.app.domain.model.Note
import com.mhss.app.domain.model.NoteFolder
import kotlinx.coroutines.flow.Flow

interface MarkdownFileManager {
    fun getFolderNotesFlow(folderId: String): Flow<List<Note>>

    fun getAllNotesFlow(rootId: String): Flow<List<Note>>

    suspend fun getNote(noteId: String): Note

    suspend fun searchNotes(query: String, rootId: String): List<Note>

    suspend fun upsertNote(note: Note, currentFolderId: String?, rootId: String): String

    suspend fun deleteNote(note: Note, rootId: String)

    suspend fun createFolder(folderName: String, parentId: String): String

    suspend fun updateFolder(folderId: String, newName: String, rootId: String)

    suspend fun deleteFolder(folderId: String, rootId: String)

    fun getFolderFoldersFlow(folderId: String): Flow<List<NoteFolder>>

    suspend fun getFolder(folderId: String): NoteFolder

    suspend fun searchFolderByName(name: String, rootId: String): List<NoteFolder>
}
