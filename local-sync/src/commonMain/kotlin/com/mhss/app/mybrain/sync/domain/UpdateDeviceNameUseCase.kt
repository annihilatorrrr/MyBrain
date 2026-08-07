package com.mhss.app.mybrain.sync.domain

import com.mhss.app.mybrain.sync.client.LocalSyncClient
import com.mhss.app.mybrain.sync.model.PairedDevice
import com.mhss.app.mybrain.sync.repository.DeviceKeyStore
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory

@Factory
class UpdateDeviceNameUseCase(
    private val deviceKeyStore: DeviceKeyStore,
    private val client: LocalSyncClient
) {
    suspend operator fun invoke(name: String, pairedDevices: List<PairedDevice>) = coroutineScope {
        deviceKeyStore.updateCurrentDeviceName(name)
        pairedDevices.forEach { device ->
            launch {
                try {
                    client.ping(
                        ip = device.ipAddress,
                        port = device.port,
                        targetDeviceId = device.deviceId
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
