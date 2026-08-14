package com.mhss.app.domain.model

sealed interface AssistantResult<out T> {
    data class Success<T>(val data: T) : AssistantResult<T>
    data object InvalidKey : Failure
    data object InternetError : Failure
    data object ToolCallLimitExceeded : Failure
    
    // Gemini Nano failures
    data object GeminiNanoNotAvailable : Failure
    data object GeminiNanoBusy : Failure
    data object GeminiNanoRequestTooLarge : Failure
    data object GeminiNanoSystemUpdateNeeded : Failure
    data object GeminiNanoIncompatible : Failure
    data object GeminiNanoNoDiskSpace : Failure
    data object GeminiNanoBackgroundBlocked : Failure
    data object GeminiNanoBatteryQuotaExceeded : Failure

    data class OtherError(val message: String? = null): Failure

    sealed interface Failure: AssistantResult<Nothing>
}