package com.mhss.app.presentation.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.domain.model.BackupFormat
import com.mhss.app.domain.use_case.ExportDataUseCase
import com.mhss.app.domain.use_case.ImportDataUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class BackupViewModel(
    private val exportData: ExportDataUseCase,
    private val importData: ImportDataUseCase,
) : ViewModel() {

    private val _backupResult = MutableStateFlow<BackupResult>(BackupResult.Idle)
    val backupResult: StateFlow<BackupResult> = _backupResult

    fun onEvent(event: BackupEvent) {
        when (event) {
            is BackupEvent.ImportData -> importDatabase(
                event.fileUri,
                event.format,
                event.encrypted,
                event.password
            )
            is BackupEvent.ExportData -> exportDatabase(
                uri = event.directoryUri,
                exportNotes = event.exportNotes,
                exportTasks = event.exportTasks,
                exportDiary = event.exportDiary,
                exportBookmarks = event.exportBookmarks,
                format = event.format,
                encrypted = event.encrypted,
                password = event.password
            )
        }
    }

    private fun importDatabase(
        uri: String,
        format: BackupFormat,
        encrypted: Boolean,
        password: String
    ) {
        viewModelScope.launch {
            _backupResult.update { BackupResult.Loading }
            val importSuccess = importData(
                fileUri = uri,
                format = format,
                encrypted = encrypted,
                password = password
            )
            if (importSuccess) {
                _backupResult.update { BackupResult.ImportSuccess }
            } else {
                _backupResult.update { BackupResult.ImportFailed }
            }
        }
    }

    private fun exportDatabase(
        uri: String,
        exportNotes: Boolean,
        exportTasks: Boolean,
        exportDiary: Boolean,
        exportBookmarks: Boolean,
        format: BackupFormat,
        encrypted: Boolean,
        password: String
    ) {
        viewModelScope.launch {
            _backupResult.update { BackupResult.Loading }
            val result = exportData(
                directoryUri = uri,
                exportNotes = exportNotes,
                exportTasks = exportTasks,
                exportDiary = exportDiary,
                exportBookmarks = exportBookmarks,
                format = format,
                encrypted = encrypted,
                password = password
            )
            if (result) {
                _backupResult.update { BackupResult.ExportSuccess }
            } else {
                _backupResult.update { BackupResult.ExportFailed }
            }
        }
    }


}