package com.mhss.app.mybrain.sync.server

import com.mhss.app.mybrain.sync.domain.NetworkHelper
import com.mhss.app.mybrain.sync.model.PairedDevice
import com.mhss.app.mybrain.sync.model.PairRequest
import com.mhss.app.mybrain.sync.model.PairResponse
import com.mhss.app.mybrain.sync.repository.DeviceKeyStore
import com.mhss.app.mybrain.sync.repository.PairedDevicesRepository
import com.mhss.app.mybrain.sync.repository.PairingInviteStore
import com.mhss.app.mybrain.sync.util.CompressionManager
import com.mhss.app.mybrain.sync.util.EncryptionManager
import com.mhss.app.mybrain.sync.util.PARAM_INVITE_ID
import com.mhss.app.mybrain.sync.util.receiveEncrypted
import com.mhss.app.mybrain.sync.util.respondEncrypted
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single
class PairRouteHandler(
    private val deviceKeyStore: DeviceKeyStore,
    private val pairedDevicesRepository: PairedDevicesRepository,
    private val pairingInviteStore: PairingInviteStore,
    private val networkHelper: NetworkHelper,
    private val compressor: CompressionManager,
    private val json: Json,
    private val encryptionManager: EncryptionManager
) {
    var onPairingAccepted: (suspend (peerDeviceId: String) -> Unit)? = null

    suspend fun handle(call: ApplicationCall, currentDeviceId: String) {
        val inviteId = call.request.queryParameters[PARAM_INVITE_ID]
        if (inviteId.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest)
            return
        }
        val invite = pairingInviteStore.get(inviteId)
        if (invite == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return
        }
        try {
            val request = call.receiveEncrypted<PairRequest>(
                encryptionManager,
                invite.secret,
                json,
                compressor
            )
            if (
                request.targetDeviceId != currentDeviceId ||
                request.sourceDeviceId.isBlank() ||
                request.sourceDeviceId == currentDeviceId
            ) {
                call.respond(HttpStatusCode.BadRequest)
                return
            }
            encryptionManager.encrypt(byteArrayOf(), request.sharedKey)
            if (!pairingInviteStore.consume(inviteId)) {
                call.respond(HttpStatusCode.Unauthorized)
                return
            }
            onPairingAccepted?.invoke(request.sourceDeviceId)
            val remoteHost = call.request.local.remoteHost
            val primaryIp = remoteHost.takeIf { it != "127.0.0.1" && it != "localhost" }
                ?: request.sourceIps.firstOrNull()
                ?: ""
            pairedDevicesRepository.addOrUpdatePairedDevice(
                PairedDevice(
                    deviceId = request.sourceDeviceId,
                    deviceName = request.sourceDeviceName,
                    ipAddress = primaryIp,
                    port = request.sourcePort,
                    encryptionKey = request.sharedKey,
                    deviceVersion = request.deviceVersion,
                    isConnected = false,
                    candidateIpAddresses = request.sourceIps
                )
            )
            call.respondEncrypted(
                PairResponse(
                    targetDeviceId = currentDeviceId,
                    targetDeviceName = deviceKeyStore.getCurrentDeviceName(),
                    targetIps = networkHelper.getAllLocalIpAddresses(),
                    deviceVersion = deviceKeyStore.getCurrentDeviceVersion()
                ),
                encryptionManager,
                invite.secret,
                json,
                compressor
            )
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(HttpStatusCode.Unauthorized)
        }
    }
}
