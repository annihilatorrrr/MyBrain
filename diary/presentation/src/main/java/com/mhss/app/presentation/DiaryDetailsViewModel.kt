package com.mhss.app.presentation

import androidx.compose.material3.SnackbarHostState
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (entryId.isNotBlank()) {
                val entry = getEntry(entryId)
                if (entry == null) {
                    uiState.value.snackbarHostState.showSnackbar(R.string.error_item_not_found)
                }
                _uiState.update {
                    it.copy(
                        entry = entry,
                        readingMode = entry != null
                    )
                }
            }
        }
    }

    fun onEvent(event: DiaryDetailsEvent) {
        when (event) {
            is DiaryDetailsEvent.DeleteEntry -> viewModelScope.launch {
                deleteEntry(uiState.value.entry!!)
                _uiState.update { it.copy(navigateUp = true) }
            }

            is DiaryDetailsEvent.ToggleReadingMode -> {
                _uiState.update { it.copy(readingMode = !it.readingMode) }
            }
            // Using applicationScope to avoid cancelling when the user exits the screen
            // and the view model is cleared before the job finishes
            is DiaryDetailsEvent.ScreenOnStop -> applicationScope.launch {
                if (!uiState.value.navigateUp) {
                    if (uiState.value.entry == null) {
                        if (event.currentEntry.title.isNotBlank() || event.currentEntry.content.isNotBlank()) {
                            val entry = event.currentEntry.copy(
                                updatedDate = now()
                            )
                            addEntry(entry)
                            _uiState.update { it.copy(entry = entry) }
                        }
                    } else if (entryChanged(uiState.value.entry!!, event.currentEntry)) {
                        val newEntry = uiState.value.entry!!.copy(
                            title = event.currentEntry.title,
                            content = event.currentEntry.content,
                            mood = event.currentEntry.mood,
                            createdDate = event.currentEntry.createdDate,
                            updatedDate = now()
                        )
                        updateEntry(newEntry)
                        _uiState.update { it.copy(entry = newEntry) }
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