package com.mhss.app.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.domain.use_case.AddDiaryEntryUseCase
import com.mhss.app.domain.use_case.DeleteDiaryEntryUseCase
import com.mhss.app.domain.use_case.GetDiaryEntryUseCase
import com.mhss.app.domain.use_case.UpdateDiaryEntryUseCase
import com.mhss.app.ui.R
import com.mhss.app.ui.snackbar.showSnackbar
import com.mhss.app.util.date.now
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Named

@KoinViewModel
class DiaryDetailsViewModel(
    private val getEntry: GetDiaryEntryUseCase,
    private val addEntry: AddDiaryEntryUseCase,
    private val updateEntry: UpdateDiaryEntryUseCase,
    private val deleteEntry: DeleteDiaryEntryUseCase,
    @Named("applicationScope") private val applicationScope: CoroutineScope,
    entryId: String
) : ViewModel() {

    var uiState by mutableStateOf(UiState())
        private set

    init {
        viewModelScope.launch {
            if (entryId.isNotBlank()) {
                val entry = getEntry(entryId)
                if (entry == null) {
                    uiState.snackbarHostState.showSnackbar(R.string.error_item_not_found)
                }
                uiState = uiState.copy(
                    entry = entry,
                    readingMode = entry != null
                )
            }
        }
    }

    fun onEvent(event: DiaryDetailsEvent) {
        when (event) {
            is DiaryDetailsEvent.DeleteEntry -> viewModelScope.launch {
                deleteEntry(uiState.entry!!)
                uiState = uiState.copy(navigateUp = true)
            }

            is DiaryDetailsEvent.ToggleReadingMode -> {
                uiState = uiState.copy(readingMode = !uiState.readingMode)
            }
            // Using applicationScope to avoid cancelling when the user exits the screen
            // and the view model is cleared before the job finishes
            is DiaryDetailsEvent.ScreenOnStop -> applicationScope.launch {
                if (!uiState.navigateUp) {
                    if (uiState.entry == null) {
                        if (event.currentEntry.title.isNotBlank() || event.currentEntry.content.isNotBlank()) {
                            val entry = event.currentEntry.copy(
                                updatedDate = now()
                            )
                            addEntry(entry)
                            uiState = uiState.copy(entry = entry)
                        }
                    } else if (entryChanged(uiState.entry!!, event.currentEntry)) {
                        val newEntry = uiState.entry!!.copy(
                            title = event.currentEntry.title,
                            content = event.currentEntry.content,
                            mood = event.currentEntry.mood,
                            createdDate = event.currentEntry.createdDate,
                            updatedDate = now()
                        )
                        updateEntry(newEntry)
                        uiState = uiState.copy(entry = newEntry)
                    }
                }
            }
        }
    }

    private fun entryChanged(entry: DiaryEntry, newEntry: DiaryEntry): Boolean {
        return entry.title != newEntry.title ||
                entry.content != newEntry.content ||
                entry.mood != newEntry.mood ||
                entry.createdDate != newEntry.createdDate
    }

    data class UiState(
        val entry: DiaryEntry? = null,
        val navigateUp: Boolean = false,
        val readingMode: Boolean = false,
        val snackbarHostState: SnackbarHostState = SnackbarHostState()
    )
}