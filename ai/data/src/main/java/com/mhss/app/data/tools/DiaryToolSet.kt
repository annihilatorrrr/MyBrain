package com.mhss.app.data.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.mhss.app.data.nowMillis
import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.domain.model.Mood
import com.mhss.app.domain.use_case.AddDiaryEntryUseCase
import com.mhss.app.domain.use_case.GetDiaryEntryUseCase
import com.mhss.app.domain.use_case.SearchEntriesUseCase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
@Factory
class DiaryToolSet(
    private val addDiaryEntry: AddDiaryEntryUseCase,
    private val searchEntries: SearchEntriesUseCase,
    private val getDiaryEntry: GetDiaryEntryUseCase
) : ToolSet {

    @Tool
    @LLMDescription("Create diary entry. Returns ID.")
    suspend fun createDiaryEntry(
        title: String,
        content: String,
        mood: Mood
    ): String {
        val id = Uuid.random().toString()
        val entry = DiaryEntry(
            title = title,
            content = content,
            createdDate = nowMillis(),
            updatedDate = nowMillis(),
            mood = mood,
            id = id
        )
        addDiaryEntry(entry)
        return id
    }

    @Tool
    @LLMDescription("Search diary entries by title/content (partial match, content truncated to 100 chars).")
    suspend fun searchDiaryEntries(
        query: String
    ): List<DiaryEntry> = searchEntries(query)

    @Tool
    @LLMDescription("Get diary entry by ID.")
    suspend fun getDiaryEntry(
        id: String
    ): DiaryEntry? = getDiaryEntry.invoke(id)
}
