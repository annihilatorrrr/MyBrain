package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.NoteFileUtilsRepository
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.stringPreferencesKey
import com.mhss.app.preferences.domain.use_case.SavePreferenceUseCase
import org.koin.core.annotation.Factory

@Factory
class UpdateExternalNotesFolderUseCase(
    private val savePreferenceUseCase: SavePreferenceUseCase,
    private val noteFileUtilsRepository: NoteFileUtilsRepository
) {
    suspend operator fun invoke(uri: String) {
        savePreferenceUseCase(stringPreferencesKey(PrefsConstants.EXTERNAL_NOTES_FOLDER_URI), uri)
        savePreferenceUseCase(stringPreferencesKey(
            PrefsConstants.EXTERNAL_NOTES_FOLDER_PATH),
            noteFileUtilsRepository.getPathFromUri(uri).orEmpty()
        )
        noteFileUtilsRepository.takePersistablePermission(uri)
    }
}