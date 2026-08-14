package com.mhss.app.mybrain.sync.util

expect class EncryptionManager() {
    fun encrypt(input: ByteArray, secretKey: String): ByteArray
    fun decrypt(input: ByteArray, secretKey: String): ByteArray
    fun removeKey(secretKey: String)
}
