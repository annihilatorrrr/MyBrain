package com.mhss.app.mybrain.sync.client

import com.mhss.app.mybrain.sync.domain.NetworkHelper
import com.mhss.app.mybrain.sync.model.PairedDevice
import com.mhss.app.mybrain.sync.model.PingPayload
import com.mhss.app.mybrain.sync.model.PingResponse
import com.mhss.app.mybrain.sync.repository.DeviceKeyStore
import com.mhss.app.mybrain.sync.repository.PairedDevicesRepository
import com.mhss.app.mybrain.sync.util.CompressionManager
import com.mhss.app.mybrain.sync.util.DEFAULT_SYNC_PORT
import com.mhss.app.mybrain.sync.util.EncryptionManager
import com.mhss.app.mybrain.sync.util.PARAM_DEVICE_ID
import com.mhss.app.mybrain.sync.util.ROUTE_PING
import com.mhss.app.mybrain.sync.util.ROUTE_SYNC
import com.mhss.app.mybrain.sync.util.postEncrypted
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
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
        targetDeviceKey: String,
        isPairing: Boolean,
    ): Boolean {
        try {
            val currentDeviceId = deviceKeyStore.getCurrentDeviceId()
            val currentDeviceEncKey = deviceKeyStore.getCurrentDeviceEncKey()
            val localIps = networkHelper.getAllLocalIpAddresses()

            val payload = PingPayload(
                sourceDeviceId = currentDeviceId,
                sourceDeviceName = deviceKeyStore.getCurrentDeviceName(),
                sourceIps = localIps,
                sourcePort = DEFAULT_SYNC_PORT,
                sourceEncKey = currentDeviceEncKey,
                targetDeviceId = targetDeviceId,
                isPairing = isPairing,
                deviceVersion = deviceKeyStore.getCurrentDeviceVersion()
            )

            val response = client.postEncrypted<PingPayload, PingResponse>(
                url = "http://$ip:$port${ROUTE_PING}",
                body = payload,
                encryptionManager = encryptionManager,
                sendKey = targetDeviceKey,
                receiveKey = currentDeviceEncKey,
                json = json,
                compressor = compressor
            )

            if (response.currentDeviceId == targetDeviceId) {
                val peer = PairedDevice(
                    deviceId = targetDeviceId,
                    deviceName = response.currentDeviceName,
                    ipAddress = ip,
                    port = port,
                    encryptionKey = targetDeviceKey,
                    deviceVersion = response.deviceVersion,
                    isConnected = true,
                    candidateIpAddresses = response.responseIps
                )

                pairedDevicesRepository.addOrUpdatePairedDevice(peer)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
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
            path = "${ROUTE_SYNC}?${PARAM_DEVICE_ID}=$currentDeviceId",
            block = block
        )
    }

}
