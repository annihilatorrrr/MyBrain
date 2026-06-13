package com.mhss.app.mybrain.sync.domain

import com.mhss.app.mybrain.sync.repository.DeviceKeyStore
import com.mhss.app.mybrain.sync.util.DEFAULT_SYNC_PORT
import com.mhss.app.mybrain.sync.util.PARAM_DEVICE_ID
import com.mhss.app.mybrain.sync.util.PARAM_ENC_KEY
import com.mhss.app.mybrain.sync.util.PARAM_IPS
import com.mhss.app.mybrain.sync.util.PARAM_PORT
import com.mhss.app.mybrain.sync.util.SYNC_DEEP_LINK_BASE_URI
import io.ktor.http.encodeURLParameter
import org.koin.core.annotation.Factory

@Factory
class GetOwnQrContentUseCase(
    private val deviceKeyStore: DeviceKeyStore,
    private val networkHelper: NetworkHelper
) {
    suspend operator fun invoke(): String {
        val deviceId = deviceKeyStore.getCurrentDeviceId()
        val encKey = deviceKeyStore.getCurrentDeviceEncKey()
        val ipsList = networkHelper.getAllLocalIpAddresses().joinToString(",")
        val encodedDeviceId = deviceId.encodeURLParameter()
        val encodedKey = encKey.encodeURLParameter()
        val encodedIps = ipsList.encodeURLParameter()
        return "$SYNC_DEEP_LINK_BASE_URI?$PARAM_DEVICE_ID=$encodedDeviceId&$PARAM_IPS=$encodedIps&$PARAM_PORT=$DEFAULT_SYNC_PORT&$PARAM_ENC_KEY=$encodedKey"
    }
}
