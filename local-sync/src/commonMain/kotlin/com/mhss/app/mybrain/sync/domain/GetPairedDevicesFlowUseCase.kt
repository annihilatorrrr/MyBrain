package com.mhss.app.mybrain.sync.domain

import com.mhss.app.mybrain.sync.model.PairedDevice
import com.mhss.app.mybrain.sync.repository.PairedDevicesRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetPairedDevicesFlowUseCase(
    private val repository: PairedDevicesRepository
) {
    operator fun invoke(): Flow<List<PairedDevice>> = repository.getPairedDevicesFlow()
}