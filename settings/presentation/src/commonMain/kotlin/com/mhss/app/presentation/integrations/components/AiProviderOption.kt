package com.mhss.app.presentation.integrations.components

import com.mhss.app.preferences.domain.model.AiProvider
import com.mhss.app.ui.Res
import com.mhss.app.ui.anthropic
import com.mhss.app.ui.gemini
import com.mhss.app.ui.gemini_nano
import com.mhss.app.ui.ic_anthropic
import com.mhss.app.ui.ic_gemini
import com.mhss.app.ui.ic_gemini_nano
import com.mhss.app.ui.ic_lmstudio
import com.mhss.app.ui.ic_ollama
import com.mhss.app.ui.ic_openai
import com.mhss.app.ui.ic_openrouter
import com.mhss.app.ui.lm_studio
import com.mhss.app.ui.ollama
import com.mhss.app.ui.openai
import com.mhss.app.ui.openrouter
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class AiProviderOption(
    val provider: AiProvider,
    val labelRes: StringResource,
    val iconRes: DrawableResource
) {
    OpenAI(AiProvider.OpenAI, Res.string.openai, Res.drawable.ic_openai),
    Gemini(AiProvider.Gemini, Res.string.gemini, Res.drawable.ic_gemini),
    GeminiNano(AiProvider.GeminiNano, Res.string.gemini_nano, Res.drawable.ic_gemini_nano),
    Anthropic(AiProvider.Anthropic, Res.string.anthropic, Res.drawable.ic_anthropic),
    OpenRouter(AiProvider.OpenRouter, Res.string.openrouter, Res.drawable.ic_openrouter),
    LmStudio(AiProvider.LmStudio, Res.string.lm_studio, Res.drawable.ic_lmstudio),
    Ollama(AiProvider.Ollama, Res.string.ollama, Res.drawable.ic_ollama);
}