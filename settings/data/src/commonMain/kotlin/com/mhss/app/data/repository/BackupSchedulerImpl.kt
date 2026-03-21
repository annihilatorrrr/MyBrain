package com.mhss.app.data.repository

import com.mhss.app.domain.model.BackupFrequency
import com.mhss.app.domain.repository.BackupScheduler

expect class BackupSchedulerImpl : BackupScheduler {
    override suspend fun scheduleBackup(
        folderUri: String,
        frequency: BackupFrequency,
        frequencyAmount: Int
    )

    override suspend fun cancelBackup()
}
