package com.mhss.app.mybrain.di

import com.mhss.app.preferences.di.PreferencesModule
import com.mhss.app.domain.di.TasksDomainModule
import com.mhss.app.domain.di.DiaryDomainModule
import com.mhss.app.domain.di.CalendarDomainModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(
    includes = [
        PreferencesModule::class,
        TasksDomainModule::class,
        DiaryDomainModule::class,
        CalendarDomainModule::class
    ]
)
@ComponentScan("com.mhss.app.mybrain")
class MainPresentationModule