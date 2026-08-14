package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.Bookmark
import com.mhss.app.domain.repository.BookmarkRepository
import org.koin.core.annotation.Factory

@Factory
class UpsertBookmarksUseCase(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(bookmarks: List<Bookmark>, notifySyncChanges: Boolean = true) =
        bookmarkRepository.upsertBookmarks(bookmarks, notifyChange = notifySyncChanges)
}
