package com.mhss.app.data.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.mhss.app.data.nowMillis
import com.mhss.app.domain.model.Bookmark
import com.mhss.app.domain.use_case.AddBookmarkUseCase
import com.mhss.app.domain.use_case.SearchBookmarksUseCase
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
@Factory
class BookmarkToolSet(
    private val addBookmark: AddBookmarkUseCase,
    private val searchBookmarksUseCase: SearchBookmarksUseCase
) : ToolSet {

    @Tool(CREATE_BOOKMARK_TOOL)
    @LLMDescription("Create bookmark. Returns ID.")
    suspend fun createBookmark(
        url: String,
        title: String = "",
        description: String = ""
    ): BookmarkIdResult {
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
        return BookmarkIdResult(createdBookmarkId = id)
    }

    @Tool(SEARCH_BOOKMARKS_TOOL)
    @LLMDescription("Search bookmarks by title/description/URL (partial match).")
    suspend fun searchBookmarks(
        query: String
    ): SearchBookmarksResult = SearchBookmarksResult(searchBookmarksUseCase(query))
}

@Serializable
data class BookmarkIdResult(val createdBookmarkId: String)

@Serializable
data class SearchBookmarksResult(val bookmarks: List<Bookmark>)
