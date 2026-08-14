package com.mhss.app.mybrain.sync.domain

import com.mhss.app.mybrain.sync.repository.PairedDevicesRepository
import org.koin.core.annotation.Factory

@Factory
class DeletePairedDeviceUseCase(
    private val repository: PairedDevicesRepository
) {
    suspend operator fun invoke(deviceId: String) = repository.deletePairedDevice(deviceId)
}