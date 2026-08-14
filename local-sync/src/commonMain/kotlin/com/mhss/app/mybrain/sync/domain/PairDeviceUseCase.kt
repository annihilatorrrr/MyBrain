package com.mhss.app.mybrain.sync.domain

import com.mhss.app.mybrain.sync.client.LocalSyncClient
import com.mhss.app.mybrain.sync.SyncOrchestrator
import org.koin.core.annotation.Factory

@Factory
class PairDeviceUseCase(
    private val networkHelper: NetworkHelper,
    private val client: LocalSyncClient,
    private val orchestrator: SyncOrchestrator,
) {
    suspend operator fun invoke(
        deviceId: String,
        ips: List<String>,
        port: Int,
        inviteId: String,
        inviteSecret: String
    ): PairResult {
        return try {
            val reachableIp =
                networkHelper.findReachableIp(primaryIp = null, candidateIps = ips, port = port)
                ?: return PairResult.Error("Device is unavailable")
            val success = client.pair(
                ip = reachableIp,
                port = port,
                targetDeviceId = deviceId,
                inviteId = inviteId,
                inviteSecret = inviteSecret
            )

            if (success) {
                orchestrator.connectWebSocket(deviceId)
                orchestrator.syncDevice(deviceId)
                PairResult.Success
            } else {
                PairResult.Error("Pairing failed or the invitation expired")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            PairResult.Error(e.message ?: "Unknown error")
        }
    }
}
