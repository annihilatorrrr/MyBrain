package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.Note
import com.mhss.app.domain.repository.NoteRepository
import org.koin.core.annotation.Factory

@Factory
class UpsertNoteUseCase(
    private val notesRepository: NoteRepository
) {
    suspend operator fun invoke(note: Note, currentFolderId: String? = null) = notesRepository.upsertNote(note, currentFolderId)
}