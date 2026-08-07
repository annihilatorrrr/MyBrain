package com.mhss.app.mybrain.sync.repository

interface DeviceKeyStore {
    suspend fun getCurrentDeviceId(): String
    suspend fun getCurrentDeviceName(): String
    suspend fun updateCurrentDeviceName(name: String)
    fun generateEncryptionKey(): String
    suspend fun getDeviceKey(deviceId: String): String?
    suspend fun getCurrentDeviceVersion(): Int
}
