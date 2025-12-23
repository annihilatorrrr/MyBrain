package com.mhss.app.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mhss.app.domain.model.AssistantResult
import com.mhss.app.ui.R

@Composable
fun AssistantResult.Failure.toUserMessage(): String {
    return when (this) {
        AssistantResult.InvalidKey -> stringResource(R.string.invalid_api_key)
        AssistantResult.InternetError -> stringResource(R.string.no_internet_connection)
        AssistantResult.ToolCallLimitExceeded -> stringResource(R.string.tool_call_limit_exceeded)
        is AssistantResult.OtherError -> message.orEmpty().ifBlank { stringResource(R.string.unexpected_error) }
    }
}