package com.mhss.app.mybrain.sync.domain

import com.mhss.app.mybrain.sync.model.PairedDevice
import kotlinx.coroutines.flow.Flow

interface NetworkHelper {
    fun getAllLocalIpAddresses(): List<String>
    fun getPrimaryLocalIpAddress(): String?
    suspend fun tryConnect(ip: String, port: Int, timeoutMs: Long = 2000): Boolean
    suspend fun findReachableIp(primaryIp: String?, candidateIps: List<String>, port: Int): String?
    fun observeNetworkChanges(): Flow<List<String>>
}

suspend inline fun NetworkHelper.findReachableIp(peer: PairedDevice) = findReachableIp(
    peer.ipAddress,
    peer.candidateIpAddresses + listOfNotNull(peer.customIpAddress),
    peer.port
)