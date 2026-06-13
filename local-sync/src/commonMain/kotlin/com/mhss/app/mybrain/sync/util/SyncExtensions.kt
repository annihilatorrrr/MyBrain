package com.mhss.app.mybrain.sync.util

import com.mhss.app.mybrain.sync.model.SyncSocketMessage
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respondBytes
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readBytes
import kotlinx.serialization.json.Json

suspend inline fun <reified T : Any> ApplicationCall.receiveEncrypted(
    encryptionManager: EncryptionManager,
    key: String,
    json: Json,
    compressor: CompressionManager
): T {
    val encryptedBytes = receive<ByteArray>()
    val decrypted = encryptionManager.decrypt(encryptedBytes, key)
    val decompressed = compressor.decompress(decrypted)
    return json.decodeFromString<T>(decompressed.decodeToString())
}

suspend inline fun <reified T : Any> ApplicationCall.respondEncrypted(
    value: T,
    encryptionManager: EncryptionManager,
    key: String,
    json: Json,
    compressor: CompressionManager
) {
    val plainJson = json.encodeToString(value).encodeToByteArray()
    val compressed = compressor.compress(plainJson)
    val encrypted = encryptionManager.encrypt(compressed, key)
    respondBytes(encrypted, ContentType.Application.OctetStream)
}

suspend inline fun <reified Req : Any, reified Resp : Any> HttpClient.postEncrypted(
    url: String,
    body: Req,
    encryptionManager: EncryptionManager,
    sendKey: String,
    receiveKey: String,
    json: Json,
    compressor: CompressionManager
): Resp {
    val plainJson = json.encodeToString(body).encodeToByteArray()
    val compressed = compressor.compress(plainJson)
    val encryptedReq = encryptionManager.encrypt(compressed, sendKey)

    val responseBytes = post(url) {
        setBody(encryptedReq)
    }.bodyAsBytes()

    val decrypted = encryptionManager.decrypt(responseBytes, receiveKey)
    val decompressed = compressor.decompress(decrypted)
    return json.decodeFromString<Resp>(decompressed.decodeToString())
}


suspend fun WebSocketSession.sendEncrypted(
    message: SyncSocketMessage,
    encryptionManager: EncryptionManager,
    key: String,
    compressor: CompressionManager,
    json: Json
) {
    val plainJson = json.encodeToString(message).encodeToByteArray()
    val compressed = compressor.compress(plainJson)
    val encrypted = encryptionManager.encrypt(compressed, key)
    send(Frame.Binary(true, encrypted))
}

suspend fun WebSocketSession.receiveEncrypted(
    encryptionManager: EncryptionManager,
    key: String,
    compressor: CompressionManager,
    json: Json
): SyncSocketMessage {
    val frame = incoming.receive()
    if (frame !is Frame.Binary) {
        throw IllegalArgumentException("Expected Frame.Binary for encrypted WebSocket message")
    }
    val decrypted = encryptionManager.decrypt(frame.readBytes(), key)
    val decompressed = compressor.decompress(decrypted)
    return json.decodeFromString(SyncSocketMessage.serializer(), decompressed.decodeToString())
}
