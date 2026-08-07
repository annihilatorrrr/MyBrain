package com.mhss.app.mybrain.sync.repository

import com.mhss.app.datetime.now
import com.mhss.app.mybrain.sync.util.PAIRING_INVITE_DURATION_MS
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single

data class PairingInvite(
    val id: String,
    val secret: String,
    val expiresAt: Long
)

@Single
class PairingInviteStore(
    private val deviceKeyStore: DeviceKeyStore
) {
    private val mutex = Mutex()
    private var pendingInvite: PairingInvite? = null

    suspend fun create(): PairingInvite = mutex.withLock {
        PairingInvite(
            id = deviceKeyStore.generateEncryptionKey(),
            secret = deviceKeyStore.generateEncryptionKey(),
            expiresAt = now() + PAIRING_INVITE_DURATION_MS
        ).also { pendingInvite = it }
    }

    suspend fun get(id: String): PairingInvite? = mutex.withLock {
        val invite = pendingInvite ?: return@withLock null
        if (now() > invite.expiresAt) {
            pendingInvite = null
            return@withLock null
        }
        invite.takeIf { it.id == id }
    }

    suspend fun consume(id: String): Boolean = mutex.withLock {
        val invite = pendingInvite ?: return@withLock false
        if (invite.id != id || now() > invite.expiresAt) {
            if (now() > invite.expiresAt) pendingInvite = null
            return@withLock false
        }
        pendingInvite = null
        true
    }

    suspend fun invalidate() = mutex.withLock {
        pendingInvite = null
    }
}
