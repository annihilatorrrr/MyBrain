package com.mhss.app.data.impl

import com.mhss.app.data.storage.MarkdownFileManager
import com.mhss.app.domain.model.Note
import com.mhss.app.domain.model.NoteFolder
import com.mhss.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class MarkdownNoteRepositoryImpl(
    private val markdownFileManager: MarkdownFileManager,
    private val rootId: String,
) : NoteRepository {

    override fun getAllFolderlessNotes(): Flow<List<Note>> {
        return markdownFileManager.getFolderNotesFlow(rootId)
    }

    override fun getAllNotes(): Flow<List<Note>> {
        return markdownFileManager.getAllNotesFlow(rootId)
    }

    override suspend fun getAllFullNotes(): List<Note> {
        return getAllNotes().first()
    }

    override suspend fun getNote(id: String): Note {
        return markdownFileManager.getNote(id)
    }

    override suspend fun searchNotes(query: String): List<Note> {
        return markdownFileManager.searchNotes(query, rootId)
    }

    override fun getNotesByFolder(folderId: String): Flow<List<Note>> {
        return markdownFileManager.getFolderNotesFlow(folderId)
    }

    override suspend fun upsertNote(note: Note, currentFolderId: String?): String {
        return markdownFileManager.upsertNote(note, currentFolderId, rootId)
    }

    override suspend fun upsertNotes(notes: List<Note>): List<String> {
        return notes.map {
            upsertNote(it, null)
        }
    }

    override suspend fun deleteNote(note: Note) {
        markdownFileManager.deleteNote(note, rootId)
    }

    override suspend fun upsertNoteFolders(folders: List<NoteFolder>) {
        folders.forEach {
            markdownFileManager.createFolder(it.name, rootId)
        }
    }

    override suspend fun insertNoteFolder(folderName: String): String {
        return markdownFileManager.createFolder(folderName, rootId)
    }

    override suspend fun updateNoteFolder(folder: NoteFolder) {
        markdownFileManager.updateFolder(folder.id, folder.name.trim(), rootId)
    }

    override suspend fun deleteNoteFolder(folder: NoteFolder) {
        markdownFileManager.deleteFolder(folder.id, rootId)
    }

    override fun getAllNoteFolders(): Flow<List<NoteFolder>> {
        return markdownFileManager.getFolderFoldersFlow(rootId)
    }

    override suspend fun getNoteFolder(folderId: String): NoteFolder? {
        if (folderId == rootId) return null
        return markdownFileManager.getFolder(folderId)
    }

    override suspend fun searchFoldersByName(name: String): List<NoteFolder> {
        return markdownFileManager.searchFolderByName(name, rootId)
    }
}
