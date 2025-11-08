package com.mhss.app.presentation.backup

import com.mhss.app.domain.model.BackupFormat

sealed class BackupEvent {
    data class ImportData(
        val fileUri: String,
        val format: BackupFormat,
        val encrypted: Boolean,
        val password: String
    ) : BackupEvent()

    data class ExportData(
        val directoryUri: String,
        val exportNotes: Boolean,
        val exportTasks: Boolean,
        val exportDiary: Boolean,
        val exportBookmarks: Boolean,
        val format: BackupFormat,
        val encrypted: Boolean,
        val password: String
    ) : BackupEvent()

}