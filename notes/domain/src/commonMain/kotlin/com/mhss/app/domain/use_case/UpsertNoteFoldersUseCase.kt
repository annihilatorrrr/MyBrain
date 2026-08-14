package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.NoteFolder
import com.mhss.app.domain.repository.NoteRepository
import org.koin.core.annotation.Factory

@Factory
class UpsertNoteFoldersUseCase(
    private val notesRepository: NoteRepository
) {
    suspend operator fun invoke(folders: List<NoteFolder>, notifySyncChanges: Boolean = true) =
        notesRepository.upsertNoteFolders(folders, notifyChange = notifySyncChanges)
}
