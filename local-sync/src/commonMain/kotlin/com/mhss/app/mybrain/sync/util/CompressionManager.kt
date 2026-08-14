package com.mhss.app.mybrain.sync.util

expect class CompressionManager {
    fun compress(input: ByteArray): ByteArray
    fun decompress(input: ByteArray): ByteArray
}