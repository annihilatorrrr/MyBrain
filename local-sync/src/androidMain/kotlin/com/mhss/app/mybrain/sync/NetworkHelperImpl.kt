package com.mhss.app.mybrain.sync

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import com.mhss.app.mybrain.sync.domain.NetworkHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket

private const val LINK_LOCAL_IPV4_PREFIX = "169.254"

@Single
class NetworkHelperImpl(
    private val context: Context
) : NetworkHelper {

    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun getAllLocalIpAddresses(): List<String> {
        val result = mutableListOf<String>()
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (networkInterface in interfaces) {
                // Ignore interfaces that are down or are loopback adapters
                if (!networkInterface.isUp || networkInterface.isLoopback) continue
                
                val name = networkInterface.name.lowercase()
                // Exclude cellular network interfaces (modems)
                if (name.startsWith("rmnet") || name.startsWith("ccmni") ||
                    name.startsWith("wwan") || name.startsWith("ppp")) continue
                
                for (address in networkInterface.inetAddresses) {
                    if (!address.isLoopbackAddress) {
                        val host = address.hostAddress ?: continue
                        val isIPv4 = host.indexOf(':') < 0
                        if (isIPv4) {
                            // Exclude link-local IPv4 addresses (169.254.x.x) as they are self-assigned
                            // and generally not routable on the local network.
                            if (!host.startsWith(LINK_LOCAL_IPV4_PREFIX)) {
                                result.add(host)
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return result.distinct().sortedBy { it.startsWith("100.") }
    }

    override fun getPrimaryLocalIpAddress(): String? {
        return getAllLocalIpAddresses().firstOrNull()
    }

    override suspend fun tryConnect(ip: String, port: Int, timeoutMs: Long): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), timeoutMs.toInt())
                true
            }
        }.getOrDefault(false)
    }

    override suspend fun findReachableIp(
        primaryIp: String?,
        candidateIps: List<String>,
        port: Int
    ): String? = withContext(Dispatchers.IO) {
        if (!primaryIp.isNullOrBlank()) {
            if (tryConnect(primaryIp, port)) return@withContext primaryIp
        }

        for (ip in candidateIps) {
            if (ip.isBlank() || ip == primaryIp) continue
            if (tryConnect(ip, port)) return@withContext ip
        }

        null
    }

    @SuppressLint("MissingPermission")
    override fun observeNetworkChanges(): Flow<List<String>> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(getAllLocalIpAddresses())
            }
            override fun onLost(network: Network) {
                trySend(getAllLocalIpAddresses())
            }
            override fun onLinkPropertiesChanged(network: Network, linkProperties: android.net.LinkProperties) {
                trySend(getAllLocalIpAddresses())
            }
        }

        try {
            connectivityManager.registerNetworkCallback(
                NetworkRequest.Builder().build(),
                callback
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        trySend(getAllLocalIpAddresses())
        
        awaitClose {
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
