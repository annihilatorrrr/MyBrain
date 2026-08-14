package com.mhss.app.mybrain.sync.domain

data class DiscoveredDevice(
    val deviceId: String,
    val ipAddress: String
)

interface NetworkDiscoveryManager {
    fun registerService(deviceId: String, port: Int)
    fun startDiscovery(onDeviceDiscovered: (DiscoveredDevice) -> Unit)
}
