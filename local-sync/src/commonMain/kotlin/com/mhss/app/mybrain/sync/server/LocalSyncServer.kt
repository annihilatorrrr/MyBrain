package com.mhss.app.mybrain.sync.server

import com.mhss.app.mybrain.sync.repository.DeviceKeyStore
import com.mhss.app.mybrain.sync.util.DEFAULT_SYNC_PORT
import com.mhss.app.mybrain.sync.util.ROUTE_PING
import com.mhss.app.mybrain.sync.util.ROUTE_SYNC
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

@Single
class LocalSyncServer(
    private val deviceKeyStore: DeviceKeyStore,
    private val pingRouteHandler: PingRouteHandler,
    private val syncWebSocketHandler: SyncWebSocketHandler
) {
    private var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? = null
    private val serverMutex = Mutex()

    suspend fun start(port: Int = DEFAULT_SYNC_PORT) = withContext(Dispatchers.IO) {
        serverMutex.withLock {
            if (server != null) return@withLock

            val currentDeviceId = deviceKeyStore.getCurrentDeviceId()

            server = embeddedServer(CIO, configure = {
                reuseAddress = true
                connector {
                    this.port = port
                    this.host = "0.0.0.0"
                }
            }) {
                install(WebSockets) {
                    pingPeriodMillis = 10000L
                    timeoutMillis = 10000L
                }
                routing {
                    post(ROUTE_PING) {
                        pingRouteHandler.handle(call, currentDeviceId)
                    }
                    webSocket(ROUTE_SYNC) {
                        syncWebSocketHandler.handle(this)
                    }
                }
            }.apply {
                start(wait = false)
            }
        }
    }

    suspend fun stop() = withContext(Dispatchers.IO) {
        serverMutex.withLock {
            server?.stop(1000L, 2000L)
            server = null
        }
    }
}
