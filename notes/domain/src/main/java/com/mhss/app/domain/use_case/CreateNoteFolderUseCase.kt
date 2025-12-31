package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.NoteRepository
import org.koin.core.annotation.Factory

@Factory
class CreateNoteFolderUseCase(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(folderName: String) = noteRepository.insertNoteFolder(folderName)
}