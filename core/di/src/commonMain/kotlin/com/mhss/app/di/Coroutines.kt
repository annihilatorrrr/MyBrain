package com.mhss.app.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module
class CoroutinesModule {
    
    @Single
    @Named("defaultDispatcher")
    fun defaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
    
    @Single
    @Named("ioDispatcher")
    fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO
    
    @Single
    @Named("applicationScope")
    fun applicationScope(): CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
}