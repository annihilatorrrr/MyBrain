package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.Note
import com.mhss.app.domain.repository.NoteRepository
import org.koin.core.annotation.Factory

@Factory
class UpsertNotesUseCase(
    private val notesRepository: NoteRepository
) {
    suspend operator fun invoke(notes: List<Note>, notifySyncChanges: Boolean = true) =
        notesRepository.upsertNotes(notes, notifyChange = notifySyncChanges)
}
