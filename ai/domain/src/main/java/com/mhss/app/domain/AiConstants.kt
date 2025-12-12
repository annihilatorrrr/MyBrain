package com.mhss.app.domain

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