package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.NoteFolder
import com.mhss.app.domain.repository.NoteRepository
import org.koin.core.annotation.Factory

@Factory
class SearchNoteFoldersByNameUseCase(
    private val notesRepository: NoteRepository
) {
    suspend operator fun invoke(name: String): List<NoteFolder> = notesRepository.searchFoldersByName(name)
}
