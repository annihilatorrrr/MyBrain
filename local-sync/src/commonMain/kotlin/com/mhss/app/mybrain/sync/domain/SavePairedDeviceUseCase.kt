package com.mhss.app.mybrain.sync.domain

import com.mhss.app.mybrain.sync.model.PairedDevice
import com.mhss.app.mybrain.sync.repository.PairedDevicesRepository
import org.koin.core.annotation.Factory

@Factory
class SavePairedDeviceUseCase(
    private val repository: PairedDevicesRepository
) {
    suspend operator fun invoke(device: PairedDevice) = repository.addOrUpdatePairedDevice(device)
}