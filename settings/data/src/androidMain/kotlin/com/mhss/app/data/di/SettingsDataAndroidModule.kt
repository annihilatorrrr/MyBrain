package com.mhss.app.data.di

import com.mhss.app.data.repository.BackupSchedulerImpl
import com.mhss.app.domain.repository.BackupScheduler
import org.koin.dsl.module

val settingsDataAndroidModule = module {
    factory<BackupScheduler> { BackupSchedulerImpl(context = get()) }
}
