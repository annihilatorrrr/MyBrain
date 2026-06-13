package com.mhss.app.mybrain.sync.server

import com.mhss.app.mybrain.sync.domain.NetworkHelper
import com.mhss.app.mybrain.sync.model.PairedDevice
import com.mhss.app.mybrain.sync.model.PingPayload
import com.mhss.app.mybrain.sync.model.PingResponse
import com.mhss.app.mybrain.sync.repository.DeviceKeyStore
import com.mhss.app.mybrain.sync.repository.PairedDevicesRepository
import com.mhss.app.mybrain.sync.util.CompressionManager
import com.mhss.app.mybrain.sync.util.EncryptionManager
import com.mhss.app.mybrain.sync.util.receiveEncrypted
import com.mhss.app.mybrain.sync.util.respondEncrypted
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single
class PingRouteHandler(
    private val deviceKeyStore: DeviceKeyStore,
    private val pairedDevicesRepository: PairedDevicesRepository,
    private val compressor: CompressionManager,
    private val json: Json,
    private val networkHelper: NetworkHelper,
    private val encryptionManager: EncryptionManager
) {
    var onPingReceived: (suspend (peerDeviceId: String) -> Unit)? = null
    suspend fun handle(
        call: ApplicationCall,
        currentDeviceId: String,
        currentDeviceEncKey: String
    ) {
        try {
            val payload = call.receiveEncrypted<PingPayload>(
                encryptionManager,
                currentDeviceEncKey,
                json,
                compressor
            )

            if (payload.targetDeviceId != currentDeviceId) {
                call.respond(HttpStatusCode.BadRequest, "Target device ID mismatch")
                return
            }
            val existingPeer = pairedDevicesRepository.getPairedDevice(payload.sourceDeviceId)
            if (existingPeer == null && !payload.isPairing) {
                call.respond(HttpStatusCode.Unauthorized, "Device not paired")
                return
            }
            if (existingPeer != null && existingPeer.encryptionKey != payload.sourceEncKey) {
                call.respond(HttpStatusCode.Unauthorized, "Paired device key mismatch")
                return
            }

            val primaryIp = call.request.local.remoteHost
                .takeIf { it != "127.0.0.1" && it != "localhost" }
                ?: payload.sourceIps.firstOrNull()
                ?: ""

            val peerDevice = PairedDevice(
                deviceId = payload.sourceDeviceId,
                deviceName = payload.sourceDeviceName,
                ipAddress = primaryIp,
                port = payload.sourcePort,
                encryptionKey = payload.sourceEncKey,
                deviceVersion = payload.deviceVersion,
                isConnected = true,
                candidateIpAddresses = payload.sourceIps
            )
            pairedDevicesRepository.addOrUpdatePairedDevice(peerDevice)

            val response = PingResponse(
                currentDeviceId = currentDeviceId,
                currentDeviceName = deviceKeyStore.getCurrentDeviceName(),
                deviceVersion = deviceKeyStore.getCurrentDeviceVersion(),
                responseIps = networkHelper.getAllLocalIpAddresses()
            )
            call.respondEncrypted(
                response,
                encryptionManager,
                payload.sourceEncKey,
                json,
                compressor
            )

            onPingReceived?.invoke(payload.sourceDeviceId)
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(HttpStatusCode.Companion.InternalServerError, e.message ?: "Unknown error")
        }
    }
}
