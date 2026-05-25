package com.mhss.app.presentation

import androidx.compose.runtime.Composable
import com.mhss.app.domain.model.AssistantResult
import com.mhss.app.ui.Res
import com.mhss.app.ui.gemini_nano_error_background_blocked
import com.mhss.app.ui.gemini_nano_error_battery_quota_exceeded
import com.mhss.app.ui.gemini_nano_error_busy
import com.mhss.app.ui.gemini_nano_error_incompatible
import com.mhss.app.ui.gemini_nano_error_no_disk_space
import com.mhss.app.ui.gemini_nano_error_not_available
import com.mhss.app.ui.gemini_nano_error_request_too_large
import com.mhss.app.ui.gemini_nano_error_system_update_needed
import com.mhss.app.ui.invalid_api_key
import com.mhss.app.ui.no_internet_connection
import com.mhss.app.ui.tool_call_limit_exceeded
import com.mhss.app.ui.unexpected_error
import org.jetbrains.compose.resources.stringResource

@Composable
fun AssistantResult.Failure.toUserMessage(): String {
    return when (this) {
        AssistantResult.InvalidKey -> stringResource(Res.string.invalid_api_key)
        AssistantResult.InternetError -> stringResource(Res.string.no_internet_connection)
        AssistantResult.ToolCallLimitExceeded -> stringResource(Res.string.tool_call_limit_exceeded)
        
        AssistantResult.GeminiNanoNotAvailable -> stringResource(Res.string.gemini_nano_error_not_available)
        AssistantResult.GeminiNanoBusy -> stringResource(Res.string.gemini_nano_error_busy)
        AssistantResult.GeminiNanoRequestTooLarge -> stringResource(Res.string.gemini_nano_error_request_too_large)
        AssistantResult.GeminiNanoSystemUpdateNeeded -> stringResource(Res.string.gemini_nano_error_system_update_needed)
        AssistantResult.GeminiNanoIncompatible -> stringResource(Res.string.gemini_nano_error_incompatible)
        AssistantResult.GeminiNanoNoDiskSpace -> stringResource(Res.string.gemini_nano_error_no_disk_space)
        AssistantResult.GeminiNanoBackgroundBlocked -> stringResource(Res.string.gemini_nano_error_background_blocked)
        AssistantResult.GeminiNanoBatteryQuotaExceeded -> stringResource(Res.string.gemini_nano_error_battery_quota_exceeded)
        
        is AssistantResult.OtherError -> message.orEmpty().ifBlank { stringResource(Res.string.unexpected_error) }
    }
}