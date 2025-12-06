package com.mhss.app.domain

object AiConstants {
    const val OPENAI_BASE_URL = "https://api.openai.com/v1"
    const val ANTHROPIC_BASE_URL = "https://api.anthropic.com"
    const val OPEN_ROUTER_BASE_URL = "https://openrouter.ai/api/v1"
    const val OLLAMA_BASE_URL = "http://localhost:11434"
    const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta"

    const val OPENAI_DEFAULT_MODEL = "gpt-5.1"
    const val ANTHROPIC_DEFAULT_MODEL = "claude-opus-4-5"
    const val OPEN_ROUTER_DEFAULT_MODEL = "openrouter/auto"
    const val OLLAMA_DEFAULT_MODEL = "gpt-oss:latest"
    const val GEMINI_DEFAULT_MODEL = "gemini-3-pro-preview"

    const val GEMINI_KEY_INFO_URL = "https://ai.google.dev/gemini-api/docs/api-key"
    const val OPENAI_KEY_INFO_URL = "https://platform.openai.com/api-keys"
    const val ANTHROPIC_KEY_INFO_URL = "https://console.anthropic.com/settings/keys"
    const val OPEN_ROUTER_KEY_INFO_URL = "https://openrouter.ai/keys"
    const val OLLAMA_KEY_INFO_URL = "https://ollama.com/download"

    const val GEMINI_MODELS_INFO_URL = "http://ai.google.dev/gemini-api/docs/models"
    const val OPENAI_MODELS_INFO_URL = "https://platform.openai.com/docs/models"
    const val ANTHROPIC_MODELS_INFO_URL = "https://docs.anthropic.com/claude/docs/models-overview"
    const val OPEN_ROUTER_MODELS_INFO_URL = "https://openrouter.ai/models"
    const val OLLAMA_MODELS_INFO_URL = "https://ollama.com/library"
}

val systemMessage = """
    You are a personal AI assistant.
    You help users with their requests and provide detailed explanations if needed.
    Users might attach notes, tasks, or calendar events. Use this attached data as a context for your response.
""".trimIndent()


val String.summarizeNotePrompt: String
    get() = """
        Summarize this note in bullet points.
        Respond with the summary only and don't say anything else.
        Use Markdown for formatting.
        Respond using the same language as the original note language.
        Note content:
        $this
        Summary:
    """.trimIndent()

val String.autoFormatNotePrompt: String
    get() = """
        Format this note in a more readable way.
        Include headings, bullet points, and other formatting.
        Respond with the formatted note only and don't say anything else.
        Use Markdown for formatting.
        Respond using the same language as the original note language.
        Note content:
        $this
        Formatted note:
    """.trimIndent()

val String.correctSpellingNotePrompt: String
    get() = """
        Correct the spelling and grammar errors in this note.
        Respond with the corrected note only and don't say anything else.
        Respond using the same language as the original note language.
        Note content:
        $this
        Corrected note:
    """.trimIndent()