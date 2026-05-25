package com.mhss.app.presentation.integrations.components

import com.mhss.app.domain.gemininano.GeminiNanoMode
import com.mhss.app.ui.Res
import com.mhss.app.ui.gemini_nano_fast
import com.mhss.app.ui.gemini_nano_fast_description
import com.mhss.app.ui.gemini_nano_full
import com.mhss.app.ui.gemini_nano_full_description
import com.mhss.app.ui.ic_bolt
import com.mhss.app.ui.ic_brain
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class GeminiNanoModeUi(
    val titleRes: StringResource,
    val descriptionRes: StringResource,
    val iconRes: DrawableResource,
    val mode: GeminiNanoMode,
) {
    Fast(Res.string.gemini_nano_fast, Res.string.gemini_nano_fast_description, Res.drawable.ic_bolt, GeminiNanoMode.Fast),
    Full(Res.string.gemini_nano_full, Res.string.gemini_nano_full_description, Res.drawable.ic_brain, GeminiNanoMode.Full)
}