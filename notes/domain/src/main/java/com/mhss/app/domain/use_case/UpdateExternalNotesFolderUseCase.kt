package com.mhss.app.domain.use_case

import com.mhss.app.preferences.domain.use_case.SavePreferenceUseCase
import org.koin.core.annotation.Factory

@Factory
class UpdateExternalNotesFolderUseCase(
    private val savePreferenceUseCase: SavePreferenceUseCase,
//    private val fileUtilsRepository: FileUtilsRepository
) {
    suspend operator fun invoke(uri: String) {
        // will handle in next commit
//        savePreferenceUseCase(stringPreferencesKey(PrefsConstants.EXTERNAL_NOTES_FOLDER_URI), uri)
//        savePreferenceUseCase(stringPreferencesKey(
//            PrefsConstants.EXTERNAL_NOTES_FOLDER_PATH),
//            fileUtilsRepository.getPathFromUri(uri).orEmpty()
//        )
//        fileUtilsRepository.takePersistablePermission(uri)
    }
}