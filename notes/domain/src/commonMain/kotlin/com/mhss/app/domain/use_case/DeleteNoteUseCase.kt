package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.Note
import com.mhss.app.domain.repository.NoteRepository
import org.koin.core.annotation.Factory

@Factory
class DeleteNoteUseCase(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) = repository.deleteNote(note)
}