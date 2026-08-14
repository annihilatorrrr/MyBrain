package com.mhss.app.mybrain.sync.server

import com.mhss.app.mybrain.sync.model.SyncSocketMessage
import com.mhss.app.mybrain.sync.repository.DeviceKeyStore
import com.mhss.app.mybrain.sync.util.CompressionManager
import com.mhss.app.mybrain.sync.util.EncryptionManager
import com.mhss.app.mybrain.sync.util.PARAM_DEVICE_ID
import com.mhss.app.mybrain.sync.util.SYNC_TRIGGER_MESSAGE
import com.mhss.app.mybrain.sync.util.concurrentMutableMap
import com.mhss.app.mybrain.sync.util.receiveEncrypted
import com.mhss.app.mybrain.sync.util.sendEncrypted
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.CloseReason
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single
class SyncWebSocketHandler(
    private val deviceKeyStore: DeviceKeyStore,
    private val compressor: CompressionManager,
    private val json: Json,
    private val encryptionManager: EncryptionManager
) {
    private val activeIncomingSessions =
        concurrentMutableMap<String, DefaultWebSocketServerSession>()
    private val sessionsMutex = Mutex()

    var onMessageReceived: (suspend (peerDeviceId: String, message: SyncSocketMessage, session: WebSocketSession) -> Unit)? =
        null
    var onPeerConnected: (suspend (peerDeviceId: String, session: DefaultWebSocketServerSession) -> Unit)? =
        null
    var onPeerDisconnected: (suspend (peerDeviceId: String, session: DefaultWebSocketServerSession) -> Unit)? =
        null

    fun isPeerConnected(peerDeviceId: String): Boolean {
        return activeIncomingSessions.containsKey(peerDeviceId)
    }

    suspend fun handle(session: DefaultWebSocketServerSession) {
        val peerDeviceId = session.call.parameters[PARAM_DEVICE_ID]
        if (peerDeviceId == null) {
            session.close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Missing device ID"))
            return
        }
        val pairKey = deviceKeyStore.getDeviceKey(peerDeviceId)
        if (pairKey == null) {
            session.close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Device not paired"))
            return
        }

        try {
            val firstMessage = session.receiveEncrypted(encryptionManager, pairKey, compressor, json)
            val replacedSession = sessionsMutex.withLock {
                activeIncomingSessions.put(peerDeviceId, session)
            }
            replacedSession?.close()
            onPeerConnected?.invoke(peerDeviceId, session)
            onMessageReceived?.invoke(peerDeviceId, firstMessage, session)
            while (true) {
                val message = session.receiveEncrypted(encryptionManager, pairKey, compressor, json)
                onMessageReceived?.invoke(peerDeviceId, message, session)
            }
        } finally {
            val removed = sessionsMutex.withLock {
                if (activeIncomingSessions[peerDeviceId] === session) {
                    activeIncomingSessions.remove(peerDeviceId)
                    true
                } else {
                    false
                }
            }
            if (removed) {
                onPeerDisconnected?.invoke(peerDeviceId, session)
            }
        }
    }

    suspend fun broadcastSyncTrigger(peerDeviceId: String) = runCatching {
        val session = activeIncomingSessions[peerDeviceId] ?: return@runCatching
        val peerKey = deviceKeyStore.getDeviceKey(peerDeviceId) ?: return@runCatching
        session.sendEncrypted(SYNC_TRIGGER_MESSAGE, encryptionManager, peerKey, compressor, json)
    }

    suspend fun broadcastToAll(excludeDeviceIds: Set<String>) {
        activeIncomingSessions.keys.forEach { peerDeviceId ->
            if (peerDeviceId !in excludeDeviceIds) {
                broadcastSyncTrigger(peerDeviceId)
            }
        }
    }

    suspend fun sendToPeer(peerDeviceId: String, message: SyncSocketMessage): Boolean {
        val session = getSession(peerDeviceId) ?: return false
        val peerKey = deviceKeyStore.getDeviceKey(peerDeviceId) ?: return false
        session.sendEncrypted(message, encryptionManager, peerKey, compressor, json)
        return true
    }

    suspend fun closeSession(peerDeviceId: String) {
        val session = sessionsMutex.withLock {
            activeIncomingSessions.remove(peerDeviceId)
        }
        runCatching { session?.close() }
    }

    suspend fun closeSession(
        peerDeviceId: String,
        expectedSession: DefaultWebSocketServerSession
    ): Boolean {
        val removed = sessionsMutex.withLock {
            if (activeIncomingSessions[peerDeviceId] === expectedSession) {
                activeIncomingSessions.remove(peerDeviceId)
                true
            } else {
                false
            }
        }
        if (removed) {
            runCatching { expectedSession.close() }
        }
        return removed
    }

    suspend fun getSession(peerDeviceId: String): DefaultWebSocketServerSession? {
        return sessionsMutex.withLock {
            activeIncomingSessions[peerDeviceId]
        }
    }

    suspend fun isCurrentSession(
        peerDeviceId: String,
        expectedSession: DefaultWebSocketServerSession
    ): Boolean {
        return sessionsMutex.withLock {
            activeIncomingSessions[peerDeviceId] === expectedSession
        }
    }
}
