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
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
@Factory
class DiaryToolSet(
    private val addDiaryEntry: AddDiaryEntryUseCase,
    private val searchEntries: SearchEntriesUseCase,
    private val getDiaryEntry: GetDiaryEntryUseCase
) : ToolSet {

    @Tool(CREATE_DIARY_ENTRY_TOOL)
    @LLMDescription("Create diary entry. Returns ID.")
    suspend fun createDiaryEntry(
        title: String,
        content: String,
        mood: Mood
    ): DiaryEntryIdResult {
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
        return DiaryEntryIdResult(createdDiaryEntryId = id)
    }

    @Tool(SEARCH_DIARY_ENTRIES_TOOL)
    @LLMDescription("Search diary entries by title/content (partial match, content truncated to 100 chars). If the user asks about the date of an entry, use $FORMAT_DATE_TOOL to get accurate dates from the result.")
    suspend fun searchDiaryEntries(
        query: String
    ): SearchDiaryEntriesResult = SearchDiaryEntriesResult(searchEntries(query))

    @Tool(GET_DIARY_ENTRY_TOOL)
    @LLMDescription("Get diary entry by ID. If the user asks about the date of an entry, use $FORMAT_DATE_TOOL to get accurate dates from the result.")
    suspend fun getDiaryEntry(
        id: String
    ): DiaryEntryResult = DiaryEntryResult(getDiaryEntry.invoke(id))
}

@Serializable
data class DiaryEntryIdResult(val createdDiaryEntryId: String)

@Serializable
data class SearchDiaryEntriesResult(val entries: List<DiaryEntry>)

@Serializable
data class DiaryEntryResult(val entry: DiaryEntry?)
