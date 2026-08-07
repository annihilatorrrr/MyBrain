package com.mhss.app.mybrain.sync.domain

import com.mhss.app.mybrain.sync.repository.DeviceKeyStore
import com.mhss.app.mybrain.sync.repository.PairingInviteStore
import com.mhss.app.mybrain.sync.util.DEFAULT_SYNC_PORT
import com.mhss.app.mybrain.sync.util.PARAM_DEVICE_ID
import com.mhss.app.mybrain.sync.util.PARAM_INVITE_ID
import com.mhss.app.mybrain.sync.util.PARAM_INVITE_SECRET
import com.mhss.app.mybrain.sync.util.PARAM_IPS
import com.mhss.app.mybrain.sync.util.PARAM_PORT
import com.mhss.app.mybrain.sync.util.SYNC_DEEP_LINK_BASE_URI
import io.ktor.http.encodeURLParameter
import org.koin.core.annotation.Factory

@Factory
class GetOwnQrContentUseCase(
    private val deviceKeyStore: DeviceKeyStore,
    private val pairingInviteStore: PairingInviteStore,
    private val networkHelper: NetworkHelper
) {
    suspend operator fun invoke(): String {
        val deviceId = deviceKeyStore.getCurrentDeviceId()
        val invite = pairingInviteStore.create()
        val ipsList = networkHelper.getAllLocalIpAddresses().joinToString(",")
        val encodedDeviceId = deviceId.encodeURLParameter()
        val encodedIps = ipsList.encodeURLParameter()
        val encodedInviteId = invite.id.encodeURLParameter()
        val encodedInviteSecret = invite.secret.encodeURLParameter()
        return "$SYNC_DEEP_LINK_BASE_URI?$PARAM_DEVICE_ID=$encodedDeviceId&$PARAM_IPS=$encodedIps&$PARAM_PORT=$DEFAULT_SYNC_PORT&$PARAM_INVITE_ID=$encodedInviteId&$PARAM_INVITE_SECRET=$encodedInviteSecret"
    }
}
