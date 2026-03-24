package com.mhss.app.presentation

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import com.mhss.app.domain.model.AssistantResult
import com.mhss.app.ui.Res
import com.mhss.app.ui.invalid_api_key
import com.mhss.app.ui.no_internet_connection
import com.mhss.app.ui.tool_call_limit_exceeded
import com.mhss.app.ui.unexpected_error

@Composable
fun AssistantResult.Failure.toUserMessage(): String {
    return when (this) {
        AssistantResult.InvalidKey -> stringResource(Res.string.invalid_api_key)
        AssistantResult.InternetError -> stringResource(Res.string.no_internet_connection)
        AssistantResult.ToolCallLimitExceeded -> stringResource(Res.string.tool_call_limit_exceeded)
        is AssistantResult.OtherError -> message.orEmpty().ifBlank { stringResource(Res.string.unexpected_error) }
    }
}