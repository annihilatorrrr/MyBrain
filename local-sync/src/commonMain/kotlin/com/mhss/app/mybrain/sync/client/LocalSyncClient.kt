package com.mhss.app.mybrain.sync.client

import com.mhss.app.mybrain.sync.domain.NetworkHelper
import com.mhss.app.mybrain.sync.model.PairedDevice
import com.mhss.app.mybrain.sync.model.PairRequest
import com.mhss.app.mybrain.sync.model.PairResponse
import com.mhss.app.mybrain.sync.model.PingPayload
import com.mhss.app.mybrain.sync.model.PingResponse
import com.mhss.app.mybrain.sync.repository.DeviceKeyStore
import com.mhss.app.mybrain.sync.repository.PairedDevicesRepository
import com.mhss.app.mybrain.sync.util.CompressionManager
import com.mhss.app.mybrain.sync.util.DEFAULT_SYNC_PORT
import com.mhss.app.mybrain.sync.util.EncryptionManager
import com.mhss.app.mybrain.sync.util.PARAM_DEVICE_ID
import com.mhss.app.mybrain.sync.util.PARAM_INVITE_ID
import com.mhss.app.mybrain.sync.util.ROUTE_PAIR
import com.mhss.app.mybrain.sync.util.ROUTE_PING
import com.mhss.app.mybrain.sync.util.ROUTE_SYNC
import com.mhss.app.mybrain.sync.util.postEncrypted
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.http.encodeURLParameter
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single
class LocalSyncClient(
    private val deviceKeyStore: DeviceKeyStore,
    private val pairedDevicesRepository: PairedDevicesRepository,
    private val client: HttpClient,
    private val networkHelper: NetworkHelper,
    private val compressor: CompressionManager,
    private val json: Json,
    private val encryptionManager: EncryptionManager,
) {
    suspend fun ping(
        ip: String,
        port: Int,
        targetDeviceId: String,
    ): Boolean {
        try {
            val currentDeviceId = deviceKeyStore.getCurrentDeviceId()
            val peer = pairedDevicesRepository.getPairedDevice(targetDeviceId) ?: return false
            val localIps = networkHelper.getAllLocalIpAddresses()

            val payload = PingPayload(
                sourceDeviceId = currentDeviceId,
                sourceDeviceName = deviceKeyStore.getCurrentDeviceName(),
                sourceIps = localIps,
                sourcePort = DEFAULT_SYNC_PORT,
                targetDeviceId = targetDeviceId,
                deviceVersion = deviceKeyStore.getCurrentDeviceVersion()
            )

            val response = client.postEncrypted<PingPayload, PingResponse>(
                url = "http://$ip:$port${ROUTE_PING}?${PARAM_DEVICE_ID}=${currentDeviceId.encodeURLParameter()}",
                body = payload,
                encryptionManager = encryptionManager,
                key = peer.encryptionKey,
                json = json,
                compressor = compressor
            )

            if (response.currentDeviceId == targetDeviceId) {
                val updatedPeer = peer.copy(
                    deviceName = response.currentDeviceName,
                    ipAddress = ip,
                    deviceVersion = response.deviceVersion,
                    isConnected = true,
                    candidateIpAddresses = response.responseIps
                )

                pairedDevicesRepository.addOrUpdatePairedDevice(updatedPeer)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    suspend fun pair(
        ip: String,
        port: Int,
        targetDeviceId: String,
        inviteId: String,
        inviteSecret: String
    ): Boolean {
        return try {
            val currentDeviceId = deviceKeyStore.getCurrentDeviceId()
            val sharedKey = deviceKeyStore.generateEncryptionKey()
            val request = PairRequest(
                sourceDeviceId = currentDeviceId,
                sourceDeviceName = deviceKeyStore.getCurrentDeviceName(),
                sourceIps = networkHelper.getAllLocalIpAddresses(),
                sourcePort = DEFAULT_SYNC_PORT,
                targetDeviceId = targetDeviceId,
                sharedKey = sharedKey,
                deviceVersion = deviceKeyStore.getCurrentDeviceVersion()
            )
            val response = client.postEncrypted<PairRequest, PairResponse>(
                url = "http://$ip:$port${ROUTE_PAIR}?${PARAM_INVITE_ID}=${inviteId.encodeURLParameter()}",
                body = request,
                encryptionManager = encryptionManager,
                key = inviteSecret,
                json = json,
                compressor = compressor
            )
            if (response.targetDeviceId != targetDeviceId) return false
            pairedDevicesRepository.addOrUpdatePairedDevice(
                PairedDevice(
                    deviceId = response.targetDeviceId,
                    deviceName = response.targetDeviceName,
                    ipAddress = ip,
                    port = port,
                    encryptionKey = sharedKey,
                    deviceVersion = response.deviceVersion,
                    isConnected = false,
                    candidateIpAddresses = response.targetIps
                )
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun connectWebSocket(
        ip: String,
        port: Int,
        currentDeviceId: String,
        block: suspend DefaultClientWebSocketSession.() -> Unit
    ) {
        client.webSocket(
            method = HttpMethod.Get,
            host = ip,
            port = port,
            path = "${ROUTE_SYNC}?${PARAM_DEVICE_ID}=${currentDeviceId.encodeURLParameter()}",
            block = block
        )
    }

}
