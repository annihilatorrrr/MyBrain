package com.mhss.app.data.repository

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mhss.app.data.worker.BackupWorker
import com.mhss.app.domain.model.BackupFrequency
import com.mhss.app.domain.repository.BackupScheduler
import org.koin.core.annotation.Factory
import java.util.concurrent.TimeUnit

@Factory
class BackupSchedulerImpl(
    private val context: Context
) : BackupScheduler {

    private val workManager: WorkManager by lazy { WorkManager.getInstance(context) }

    override suspend fun scheduleBackup(
        folderUri: String,
        frequency: BackupFrequency,
        frequencyAmount: Int
    ) {

        val workRequest = PeriodicWorkRequestBuilder<BackupWorker>(
            repeatInterval = frequencyAmount.coerceAtLeast(1).toLong() * frequency.hours,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
            flexTimeInterval = PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS,
            flexTimeIntervalUnit = TimeUnit.MILLISECONDS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            BackupWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    override suspend fun cancelBackup() {
        workManager.cancelUniqueWork(BackupWorker.WORK_NAME)
    }
}

