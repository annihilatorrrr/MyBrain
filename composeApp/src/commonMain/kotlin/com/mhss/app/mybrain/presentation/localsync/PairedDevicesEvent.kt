package com.mhss.app.mybrain.presentation.localsync

import com.mhss.app.mybrain.sync.model.PairedDevice

sealed interface PairedDevicesEvent {
    data class PairDirectly(
        val deviceId: String,
        val ips: List<String>,
        val port: Int,
        val encKey: String
    ) : PairedDevicesEvent

    data class DecodeAndPair(val bitmap: KmpBitmap) : PairedDevicesEvent

    data class PingDevice(val device: PairedDevice) : PairedDevicesEvent

    data class DeleteDevice(val deviceId: String) : PairedDevicesEvent

    data class UpdateDeviceName(val name: String) : PairedDevicesEvent

    data class SetCustomIp(val deviceId: String, val ipAddress: String?) : PairedDevicesEvent

    data class PairFromClipboard(val pairingLink: String?) : PairedDevicesEvent

    data object ResetEncryptionKey : PairedDevicesEvent
}
