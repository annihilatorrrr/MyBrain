package com.mhss.app.data.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.mhss.app.data.nowMillis
import com.mhss.app.domain.model.Bookmark
import com.mhss.app.domain.use_case.AddBookmarkUseCase
import com.mhss.app.domain.use_case.SearchBookmarksUseCase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
@Factory
class BookmarkToolSet(
    private val addBookmark: AddBookmarkUseCase,
    private val searchBookmarksUseCase: SearchBookmarksUseCase
) : ToolSet {

    @Tool
    @LLMDescription("Create bookmark. Returns ID.")
    suspend fun createBookmark(
        url: String,
        title: String = "",
        description: String = ""
    ): String {
        val id = Uuid.random().toString()
        val bookmark = Bookmark(
            url = url,
            title = title,
            description = description,
            createdDate = nowMillis(),
            updatedDate = nowMillis(),
            id = id
        )
        addBookmark(bookmark)
        return id
    }

    @Tool
    @LLMDescription("Search bookmarks by title/description/URL (partial match).")
    suspend fun searchBookmarks(
        query: String
    ): List<Bookmark> = searchBookmarksUseCase(query)
}
