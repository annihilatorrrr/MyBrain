package com.mhss.app.mybrain.presentation.bookmarks

import com.mhss.app.mybrain.domain.model.bookmarks.Bookmark
import com.mhss.app.mybrain.presentation.common.ItemView
import com.mhss.app.mybrain.domain.model.preferences.Order

sealed class BookmarkEvent {
    data class AddBookmark(val bookmark: Bookmark) : BookmarkEvent()
    data class GetBookmark(val bookmarkId: Int) : BookmarkEvent()
    data class SearchBookmarks(val query: String) : BookmarkEvent()
    data class UpdateOrder(val order: Order) : BookmarkEvent()
    data class UpdateView(val view: ItemView) : BookmarkEvent()
    data class UpdateBookmark(val bookmark: Bookmark) : BookmarkEvent()
    data class DeleteBookmark(val bookmark: Bookmark) : BookmarkEvent()
    object ErrorDisplayed: BookmarkEvent()
}
