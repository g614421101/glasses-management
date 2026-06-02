package com.glasses.app.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

data class ServiceAddress(val ip: String, val port: Int)

object MdnsDiscovery {

    private const val TAG = "MdnsDiscovery"
    private const val SERVICE_TYPE = "_glasses._tcp."
    private const val DISCOVERY_TIMEOUT_MS = 10_000L

    suspend fun discover(context: Context): ServiceAddress? {
        return withTimeoutOrNull(DISCOVERY_TIMEOUT_MS) {
            withContext(Dispatchers.Main) {
                suspendCancellableCoroutine { cont ->
                    val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

                    val resolveListener = object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                            Log.e(TAG, "解析服务失败: $errorCode")
                        }

                        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                            val host = serviceInfo.host
                            val port = serviceInfo.port
                            val ip = host?.hostAddress
                            if (ip != null && ip.isNotEmpty()) {
                                Log.d(TAG, "服务解析完成: $ip:$port")
                                if (cont.isActive) {
                                    cont.resume(ServiceAddress(ip, port))
                                }
                            }
                        }
                    }

                    val discoveryListener = object : NsdManager.DiscoveryListener {
                        override fun onDiscoveryStarted(serviceType: String) {
                            Log.d(TAG, "开始搜索 mDNS 服务: $serviceType")
                        }

                        override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                            Log.d(TAG, "发现服务: ${serviceInfo.serviceName}, type: ${serviceInfo.serviceType}")
                            if (serviceInfo.serviceType.contains("_glasses._tcp")) {
                                try {
                                    nsdManager.resolveService(serviceInfo, resolveListener)
                                } catch (e: Exception) {
                                    Log.e(TAG, "请求解析服务失败", e)
                                }
                            }
                        }

                        override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                            Log.d(TAG, "服务丢失: ${serviceInfo.serviceName}")
                        }

                        override fun onDiscoveryStopped(serviceType: String) {
                            Log.d(TAG, "停止搜索: $serviceType")
                        }

                        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                            Log.e(TAG, "启动搜索失败: $errorCode")
                            if (cont.isActive) {
                                cont.resume(null)
                            }
                        }

                        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                            Log.e(TAG, "停止搜索失败: $errorCode")
                        }
                    }

                    cont.invokeOnCancellation {
                        try {
                            nsdManager.stopServiceDiscovery(discoveryListener)
                        } catch (e: Exception) {
                            // ignore
                        }
                    }

                    try {
                        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
                    } catch (e: Exception) {
                        Log.e(TAG, "mDNS 初始化失败", e)
                        if (cont.isActive) {
                            cont.resume(null)
                        }
                    }
                }
            }
        }
    }
}
