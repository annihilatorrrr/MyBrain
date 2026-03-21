package com.mhss.app.domain.repository

interface BackupScheduler {
    suspend fun scheduleBackup(
        folderUri: String,
        frequency: com.mhss.app.domain.model.BackupFrequency,
        frequencyAmount: Int
    )
    suspend fun cancelBackup()
}

