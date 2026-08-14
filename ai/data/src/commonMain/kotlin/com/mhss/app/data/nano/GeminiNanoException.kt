package com.mhss.app.data.nano

import com.mhss.app.domain.model.AssistantResult

expect class GeminiNanoException : Exception

expect fun GeminiNanoException.toAssistantResult(): AssistantResult.Failure