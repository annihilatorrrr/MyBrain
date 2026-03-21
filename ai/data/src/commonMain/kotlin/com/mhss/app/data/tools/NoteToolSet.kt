package com.mhss.app.data.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.mhss.app.data.nowMillis
import com.mhss.app.domain.model.Note
import com.mhss.app.domain.model.NoteFolder
import com.mhss.app.domain.use_case.CreateNoteFolderUseCase
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
    private val createFolderUseCase: CreateNoteFolderUseCase,
    private val searchNoteFoldersByName: SearchNoteFoldersByNameUseCase,
    private val getNoteFolder: GetNoteFolderUseCase
) : ToolSet {

    @Tool(SEARCH_NOTES_TOOL)
    @LLMDescription("Search notes by title/content (partial match, content truncated to 100 chars). If the query is empty, returns all notes.")
    suspend fun searchNotes(
        @LLMDescription("Search query") query: String
    ): SearchNotesResult = SearchNotesResult(searchNotesByName(query))

    @Tool(CREATE_NOTE_TOOL)
    @LLMDescription("Create a note. Returns ID.")
    suspend fun createNote(
        title: String,
        content: String,
        @LLMDescription("Optional Folder ID. If null, the note will be in the root folder. Use $SEARCH_NOTE_FOLDERS_TOOL to find an ID. Keep null if user didn't ask for specific folder.") folderId: String? = null,
        pinned: Boolean = false
    ): NoteIdResult {
        if (folderId != null) {
           runCatching { getNoteFolder(folderId) }.getOrNull() ?: throw IllegalArgumentException("No folder found with ID: '$folderId'. The note was not created.. folderId must be a valid ID. If you only have folder name, use the $SEARCH_NOTE_FOLDERS_TOOL tool to find the folder's ID or keep the folderId null to put in the root folder.")
        }
        val note = Note(
            title = title,
            content = content,
            folderId = folderId,
            pinned = pinned,
            createdDate = nowMillis(),
            updatedDate = nowMillis()
        )
        return NoteIdResult(createdNoteId = upsertNote(note))
    }

    @Tool(CREATE_MULTIPLE_NOTES_TOOL)
    @LLMDescription("Create multiple notes. Returns IDs.")
    suspend fun createMultipleNotes(
        notes: List<NoteInput>
    ): NoteIdsResult {
        notes.forEach { input ->
            if (input.folderId != null) {
                runCatching { getNoteFolder(input.folderId) }.getOrNull()
                    ?: throw IllegalArgumentException("No folder found with ID: '${input.folderId}'. folderId must be a valid ID. If you only have the folder name, use the $SEARCH_NOTE_FOLDERS_TOOL tool to find the folder's ID first. The notes were not created.")
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
        return NoteIdsResult(createdNoteIds = upsertNotes(noteModels))
    }

    @Tool(GET_NOTE_BY_ID_TOOL)
    @LLMDescription("Get full note by ID.")
    suspend fun getNoteById(
        id: String
    ): NoteResult {
        return NoteResult(getNote(id) ?: throw IllegalArgumentException("No note found with ID: '$id'. id must be a valid ID. If you only have the note title, use the $SEARCH_NOTES_TOOL tool to find the note's ID first. The operation did not proceed."))
    }

    @Tool(SEARCH_NOTE_FOLDERS_TOOL)
    @LLMDescription("Search folders by name (partial match). Returns folder IDs.")
    suspend fun searchFolders(
        @LLMDescription("Folder name query") name: String
    ) = SearchNoteFoldersResult(searchNoteFoldersByName(name))

    @Tool(CREATE_NOTE_FOLDER_TOOL)
    @LLMDescription("Create a note folder. Returns ID.")
    suspend fun createFolder(
        @LLMDescription("Folder name") name: String
    ): CreateNoteFolderResult {
        return CreateNoteFolderResult(
            folderId = createFolderUseCase(folderName = name)
        )
    }

}

@Serializable
data class NoteInput(
    val title: String,
    val content: String,
    @param:LLMDescription("Folder ID (null = root), Keep null if user didn't ask for specific folder.") val folderId: String? = null,
    val pinned: Boolean = false
)

@Serializable
data class SearchNoteFoldersResult(val folders: List<NoteFolder>)

@Serializable
data class SearchNotesResult(val notes: List<Note>)

@Serializable
data class NoteIdResult(val createdNoteId: String)

@Serializable
data class NoteIdsResult(val createdNoteIds: List<String>)

@Serializable
data class NoteResult(val note: Note)

@Serializable
data class CreateNoteFolderResult(val folderId: String)
