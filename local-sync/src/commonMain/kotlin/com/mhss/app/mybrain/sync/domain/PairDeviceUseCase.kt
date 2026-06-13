package com.mhss.app.mybrain.sync.domain

import com.mhss.app.mybrain.sync.client.LocalSyncClient
import com.mhss.app.mybrain.sync.model.PairedDevice
import com.mhss.app.mybrain.sync.SyncOrchestrator
import com.mhss.app.mybrain.sync.repository.PairedDevicesRepository
import org.koin.core.annotation.Factory

@Factory
class PairDeviceUseCase(
    private val networkHelper: NetworkHelper,
    private val client: LocalSyncClient,
    private val orchestrator: SyncOrchestrator,
    private val pairedDevicesRepository: PairedDevicesRepository,
) {
    suspend operator fun invoke(
        deviceId: String,
        ips: List<String>,
        port: Int,
        encKey: String
    ): PairResult {
        return try {
            val reachableIp =
                networkHelper.findReachableIp(primaryIp = null, candidateIps = ips, port = port)
            val success = if (reachableIp != null) {
                client.ping(
                    ip = reachableIp,
                    port = port,
                    targetDeviceId = deviceId,
                    targetDeviceKey = encKey,
                    isPairing = true,
                )
            } else false

            if (success) {
                orchestrator.connectWebSocket(deviceId)
                orchestrator.syncDevice(deviceId)
                PairResult.Success
            } else {
                val fallbackIp = reachableIp ?: ips.firstOrNull() ?: ""
                val offlineDevice = PairedDevice(
                    deviceId = deviceId,
                    deviceName = "",
                    ipAddress = fallbackIp,
                    port = port,
                    encryptionKey = encKey,
                    deviceVersion = 1,
                    isConnected = false,
                    candidateIpAddresses = ips
                )
                pairedDevicesRepository.addOrUpdatePairedDevice(offlineDevice)
                orchestrator.connectWebSocket(deviceId)
                PairResult.OfflineSuccess
            }
        } catch (e: Exception) {
            e.printStackTrace()
            PairResult.Error(e.message ?: "Unknown error")
        }
    }
}
