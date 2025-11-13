package com.mhss.app.presentation.integrations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.data.noteMarkdownModule
import com.mhss.app.data.noteRoomModule
import com.mhss.app.domain.AiConstants
import com.mhss.app.domain.repository.FileUtilsRepository
import com.mhss.app.domain.use_case.UpdateExternalNotesFolderUseCase
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.AiProvider
import com.mhss.app.preferences.domain.model.PrefsKey
import com.mhss.app.preferences.domain.model.PrefsKey.BooleanKey
import com.mhss.app.preferences.domain.model.PrefsKey.IntKey
import com.mhss.app.preferences.domain.model.PrefsKey.StringKey
import com.mhss.app.preferences.domain.model.stringPreferencesKey
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.preferences.domain.use_case.SavePreferenceUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.context.GlobalContext.loadKoinModules
import org.koin.core.context.GlobalContext.unloadKoinModules

@KoinViewModel
class IntegrationsViewModel(
    private val savePreference: SavePreferenceUseCase,
    private val getPreference: GetPreferenceUseCase,
    private val updateExternalNotesFolder: UpdateExternalNotesFolderUseCase,
    private val fileUtilsRepository: FileUtilsRepository
) : ViewModel() {

    fun <T> getSettings(key: PrefsKey<T>, defaultValue: T): Flow<T> {
        return getPreference(key, defaultValue)
    }

    fun getAiProvider(): Flow<AiProvider> = getPreference(
        PrefsKey.IntKey(PrefsConstants.AI_PROVIDER_KEY),
        AiProvider.None.ordinal
    ).map { AiProvider.entries.first { entry -> entry.id == it } }

    fun getExternalNotesFolderPath(): Flow<String?> {
        return getPreference(
            stringPreferencesKey(PrefsConstants.EXTERNAL_NOTES_FOLDER_URI),
            ""
        ).map { uri ->
            if (uri.isBlank()) null
            else fileUtilsRepository.getPathFromUri(uri)
        }
    }

    fun onEvent(event: IntegrationsEvent) {
        when (event) {
            is IntegrationsEvent.ToggleAiProvider -> {
                saveSettings(
                    IntKey(PrefsConstants.AI_PROVIDER_KEY),
                    if (event.enabled) AiProvider.Gemini.id else AiProvider.None.id
                )
            }

            is IntegrationsEvent.SelectProvider -> {
                saveSettings(
                    IntKey(PrefsConstants.AI_PROVIDER_KEY),
                    event.provider.id
                )
            }

            is IntegrationsEvent.UpdateGeminiKey -> {
                saveSettings(
                    stringPreferencesKey(PrefsConstants.GEMINI_KEY),
                    event.key
                )
            }

            is IntegrationsEvent.UpdateGeminiModel -> {
                saveSettings(
                    stringPreferencesKey(PrefsConstants.GEMINI_MODEL_KEY),
                    event.model
                )
            }

            is IntegrationsEvent.UpdateOpenAiKey -> {
                saveSettings(
                    stringPreferencesKey(PrefsConstants.OPENAI_KEY),
                    event.key
                )
            }

            is IntegrationsEvent.UpdateOpenAiModel -> {
                saveSettings(
                    stringPreferencesKey(PrefsConstants.OPENAI_MODEL_KEY),
                    event.model
                )
            }

            is IntegrationsEvent.ToggleOpenAiCustomURL -> {
                saveSettings(
                    BooleanKey(PrefsConstants.OPENAI_USE_URL_KEY),
                    event.enabled
                )
                if (!event.enabled) {
                    saveSettings(
                        StringKey(PrefsConstants.OPENAI_URL_KEY),
                        AiConstants.OPENAI_BASE_URL
                    )
                }
            }

            is IntegrationsEvent.UpdateOpenAiCustomURL -> {
                saveSettings(
                    StringKey(PrefsConstants.OPENAI_URL_KEY),
                    event.url
                )
            }

            is IntegrationsEvent.SelectExternalNotesFolder -> {
                viewModelScope.launch {
                    updateExternalNotesFolder(event.folderUri)
                    unloadKoinModules(noteRoomModule)
                    loadKoinModules(noteMarkdownModule(event.folderUri))
                }
            }

            is IntegrationsEvent.SetExternalNotesEnabled -> {
                saveSettings(
                    BooleanKey(PrefsConstants.EXTERNAL_NOTES_ENABLED),
                    event.enabled
                )
                viewModelScope.launch {
                    if (event.enabled) {
                        val rootUri = getPreference(
                            stringPreferencesKey(PrefsConstants.EXTERNAL_NOTES_FOLDER_URI),
                            ""
                        ).first()
                        if (rootUri.isNotBlank()) {
                            loadKoinModules(noteMarkdownModule(rootUri))
                        }
                    } else {
                        loadKoinModules(noteRoomModule)
                    }
                }
            }
        }
    }

    private fun <T> saveSettings(key: PrefsKey<T>, value: T) {
        viewModelScope.launch {
            savePreference(key, value)
        }
    }
}

