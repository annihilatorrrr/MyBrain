package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.Bookmark
import com.mhss.app.domain.repository.BookmarkRepository
import org.koin.core.annotation.Single

@Single
class AddBookmarkUseCase(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(bookmark: Bookmark) = bookmarkRepository.addBookmark(bookmark)
}