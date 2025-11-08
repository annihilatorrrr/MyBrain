package com.mhss.app.presentation.integrations

import com.mhss.app.preferences.domain.model.AiProvider

sealed class IntegrationsEvent {
    data class ToggleAiProvider(val enabled: Boolean) : IntegrationsEvent()
    data class SelectProvider(val provider: AiProvider) : IntegrationsEvent()
    data class UpdateGeminiKey(val key: String) : IntegrationsEvent()
    data class UpdateGeminiModel(val model: String) : IntegrationsEvent()
    data class UpdateOpenAiKey(val key: String) : IntegrationsEvent()
    data class UpdateOpenAiModel(val model: String) : IntegrationsEvent()
    data class ToggleOpenAiCustomURL(val enabled: Boolean) : IntegrationsEvent()
    data class UpdateOpenAiCustomURL(val url: String) : IntegrationsEvent()

    data class SetExternalNotesEnabled(val enabled: Boolean) : IntegrationsEvent()

    data class SelectExternalNotesFolder(val folderUri: String) : IntegrationsEvent()
}