package com.mhss.app.presentation.backup

sealed class BackupResult {
    data object ExportSuccess : BackupResult()
    data object ExportFailed : BackupResult()
    data object ImportSuccess : BackupResult()
    data object ImportFailed : BackupResult()
    data object Loading : BackupResult()
    data object Idle : BackupResult()
}