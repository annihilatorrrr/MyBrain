package com.mhss.app.data.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.mhss.app.data.nowMillis
import com.mhss.app.domain.model.Note
import com.mhss.app.domain.use_case.GetNoteFolderUseCase
import com.mhss.app.domain.use_case.GetNoteUseCase
import com.mhss.app.domain.use_case.SearchNoteFoldersByNameUseCase
import com.mhss.app.domain.use_case.SearchNotesUseCase
import com.mhss.app.domain.use_case.UpsertNoteUseCase
import com.mhss.app.domain.use_case.UpsertNotesUseCase
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Factory

@Factory
class NoteToolSet(
    private val upsertNote: UpsertNoteUseCase,
    private val upsertNotes: UpsertNotesUseCase,
    private val searchNotesByName: SearchNotesUseCase,
    private val getNote: GetNoteUseCase,
    private val searchNoteFoldersByName: SearchNoteFoldersByNameUseCase,
    private val getNoteFolder: GetNoteFolderUseCase
) : ToolSet {

    @Tool
    @LLMDescription("Search folders by name (partial match). Returns folder IDs.")
    suspend fun searchFolders(
        @LLMDescription("Folder name query") name: String
    ) = searchNoteFoldersByName(name)

    @Tool
    @LLMDescription("Search notes by title/content (partial match, content truncated to 100 chars).")
    suspend fun searchNotes(
        @LLMDescription("Search query") query: String
    ): List<Note> = searchNotesByName(query)

    @Tool
    @LLMDescription("Create a note. Returns ID.")
    suspend fun createNote(
        title: String,
        content: String,
        @LLMDescription("Folder ID (null = root). Use searchFolders to find ID.") folderId: String? = null,
        pinned: Boolean = false
    ): String {
        if (folderId != null) {
           getNoteFolder(folderId) ?: throw IllegalArgumentException("No folder found with ID: '$folderId'. folderId must be a valid ID. If you only have the folder name, use the searchFolders tool to find the folder's ID first. The note was not created.")
        }
        val note = Note(
            title = title,
            content = content,
            folderId = folderId,
            pinned = pinned,
            createdDate = nowMillis(),
            updatedDate = nowMillis()
        )
        return upsertNote(note)
    }

    @Tool
    @LLMDescription("Create multiple notes. Returns IDs.")
    suspend fun createMultipleNotes(
        notes: List<NoteInput>
    ): List<String> {
        notes.forEach { input ->
            if (input.folderId != null) {
                getNoteFolder(input.folderId)
                    ?: throw IllegalArgumentException("No folder found with ID: '${input.folderId}'. folderId must be a valid ID. If you only have the folder name, use the searchFolders tool to find the folder's ID first. The notes were not created.")
            }
        }
        val noteModels = notes.map { input ->
            Note(
                title = input.title,
                content = input.content,
                folderId = input.folderId,
                pinned = input.pinned,
                createdDate = nowMillis(),
                updatedDate = nowMillis()
            )
        }
        return upsertNotes(noteModels)
    }

    @Tool
    @LLMDescription("Get full note by ID.")
    suspend fun getNoteById(
        id: String
    ): Note {
        return getNote(id) ?: throw IllegalArgumentException("No note found with ID: '$id'. id must be a valid ID. If you only have the note title, use the searchNotes tool to find the note's ID first. The operation did not proceed.")
    }
}

@Serializable
data class NoteInput(
    val title: String,
    val content: String,
    @param:LLMDescription("Folder ID (null = root)") val folderId: String? = null,
    val pinned: Boolean = false
)
