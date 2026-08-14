package com.mhss.app.data.nano

import com.google.mlkit.genai.common.GenAiException
import com.mhss.app.domain.model.AssistantResult

actual typealias GeminiNanoException = GenAiException

actual fun GeminiNanoException.toAssistantResult(): AssistantResult.Failure {
    return when(this.errorCode) {
        GenAiException.ErrorCode.NOT_AVAILABLE -> AssistantResult.GeminiNanoNotAvailable
        GenAiException.ErrorCode.BUSY -> AssistantResult.GeminiNanoBusy
        GenAiException.ErrorCode.REQUEST_TOO_LARGE -> AssistantResult.GeminiNanoRequestTooLarge
        GenAiException.ErrorCode.NEEDS_SYSTEM_UPDATE -> AssistantResult.GeminiNanoSystemUpdateNeeded
        GenAiException.ErrorCode.AICORE_INCOMPATIBLE -> AssistantResult.GeminiNanoIncompatible
        GenAiException.ErrorCode.NOT_ENOUGH_DISK_SPACE -> AssistantResult.GeminiNanoNoDiskSpace
        GenAiException.ErrorCode.BACKGROUND_USE_BLOCKED -> AssistantResult.GeminiNanoBackgroundBlocked
        GenAiException.ErrorCode.PER_APP_BATTERY_USE_QUOTA_EXCEEDED -> AssistantResult.GeminiNanoBatteryQuotaExceeded
        else -> AssistantResult.OtherError("Gemini Nano Error: ${this.message ?: "Unknown error"}")
    }
}