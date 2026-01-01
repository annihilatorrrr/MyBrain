package com.mhss.app.presentation

import com.mhss.app.preferences.domain.model.Order
import com.mhss.app.ui.ItemView

sealed class NoteEvent {
    data class SearchNotes(val query: String) : NoteEvent()
    data class UpdateOrder(val order: Order) : NoteEvent()
    data class UpdateView(val view: ItemView) : NoteEvent()
    data class ShowAllNotes(val showAll: Boolean) : NoteEvent()
    data class CreateFolder(val name: String): NoteEvent()
}