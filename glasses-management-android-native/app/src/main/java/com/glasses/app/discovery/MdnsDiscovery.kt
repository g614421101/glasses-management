package com.glasses.app.discovery

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener
import kotlin.coroutines.resume

data class ServiceAddress(val ip: String, val port: Int)

object MdnsDiscovery {

    private const val TAG = "MdnsDiscovery"
    private const val SERVICE_TYPE = "_glasses._tcp.local."
    private const val DISCOVERY_TIMEOUT_MS = 10_000L

    suspend fun discover(): ServiceAddress? {
        return withTimeoutOrNull(DISCOVERY_TIMEOUT_MS) {
            withContext(Dispatchers.IO) {
                suspendCancellableCoroutine { cont ->
                    var jmdns: JmDNS? = null
                    val listener = object : ServiceListener {
                        override fun serviceAdded(event: ServiceEvent) {
                            Log.d(TAG, "服务发现: ${event.name}")
                            jmdns?.requestServiceInfo(event.type, event.name, true)
                        }

                        override fun serviceRemoved(event: ServiceEvent) {}

                        override fun serviceResolved(event: ServiceEvent) {
                            val info = event.info
                            val ip = info.hostAddresses.firstOrNull()
                            val port = info.port
                            if (ip != null && ip.isNotEmpty()) {
                                Log.d(TAG, "服务解析完成: $ip:$port")
                                if (cont.isActive) {
                                    cont.resume(ServiceAddress(ip, port))
                                }
                            }
                        }
                    }

                    cont.invokeOnCancellation {
                        try {
                            jmdns?.removeServiceListener(SERVICE_TYPE, listener)
                            jmdns?.close()
                        } catch (_: Exception) {}
                    }

                    try {
                        jmdns = JmDNS.create("glasses-native-app")
                        jmdns!!.addServiceListener(SERVICE_TYPE, listener)
                        Log.d(TAG, "开始监�?mDNS 服务: $SERVICE_TYPE")
                    } catch (e: Exception) {
                        Log.e(TAG, "mDNS 初始化失�?, e)
                        if (cont.isActive) cont.resume(null)
                    }
                }
            }
        }
    }
}
