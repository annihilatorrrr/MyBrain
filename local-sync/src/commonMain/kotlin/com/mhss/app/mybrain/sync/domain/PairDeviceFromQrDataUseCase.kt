package com.mhss.app.mybrain.sync.domain

import com.mhss.app.mybrain.sync.util.DEFAULT_SYNC_PORT
import com.mhss.app.mybrain.sync.util.PARAM_DEVICE_ID
import com.mhss.app.mybrain.sync.util.PARAM_INVITE_ID
import com.mhss.app.mybrain.sync.util.PARAM_INVITE_SECRET
import com.mhss.app.mybrain.sync.util.PARAM_IPS
import com.mhss.app.mybrain.sync.util.PARAM_PORT
import com.mhss.app.mybrain.sync.model.QrPayload
import com.mhss.app.mybrain.sync.util.SYNC_DEEP_LINK_BASE_URI
import io.ktor.http.parseQueryString
import org.koin.core.annotation.Factory

@Factory
class PairDeviceFromQrDataUseCase(
    private val pairDeviceUseCase: PairDeviceUseCase,
) {

    suspend operator fun invoke(qrText: String): PairResult {
        return try {
            val payload = parseQrPayload(qrText)
            pairDeviceUseCase(
                deviceId = payload.deviceId,
                ips = payload.ips,
                port = payload.port,
                inviteId = payload.inviteId,
                inviteSecret = payload.inviteSecret
            )
        } catch (e: Exception) {
            PairResult.Error(e.message ?: "Invalid QR format")
        }
    }

    private fun parseQrPayload(qrText: String): QrPayload {
        if (!qrText.startsWith(SYNC_DEEP_LINK_BASE_URI)) {
            throw IllegalArgumentException("Invalid QR format")
        }
        val queryString = qrText.substringAfter("?", "")
        if (queryString.isBlank()) {
            throw IllegalArgumentException("Missing query parameters")
        }
        val params = parseQueryString(queryString)

        val deviceId = params[PARAM_DEVICE_ID] ?: throw IllegalArgumentException("Missing deviceId")
        val ipsParam = params[PARAM_IPS] ?: throw IllegalArgumentException("Missing ips")
        val ipsList = ipsParam.split(",").filter { it.isNotBlank() }
        val inviteId = params[PARAM_INVITE_ID] ?: throw IllegalArgumentException("Missing inviteId")
        val inviteSecret = params[PARAM_INVITE_SECRET] ?: throw IllegalArgumentException("Missing inviteSecret")
        val port = params[PARAM_PORT]?.toIntOrNull() ?: DEFAULT_SYNC_PORT
        if (ipsList.isEmpty()) throw IllegalArgumentException("Missing ips")

        return QrPayload(
            deviceId = deviceId,
            ips = ipsList,
            port = port,
            inviteId = inviteId,
            inviteSecret = inviteSecret
        )
    }

}
