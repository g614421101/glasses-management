package com.glasses.app.discovery

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

sealed class ConnectionState {
    data object Idle : ConnectionState()
    data object Searching : ConnectionState()
    data class Connecting(val ip: String, val port: Int) : ConnectionState()
    data class Connected(val url: String) : ConnectionState()
    data object ManualInput : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

class ConnectionManager(private val context: Context) {

    companion object {
        private const val TAG = "ConnectionManager"
        private const val PREFS_NAME = "glasses_connection"
        private const val KEY_LAST_IP = "last_ip"
        private const val KEY_LAST_PORT = "last_port"
        private const val CONNECT_TIMEOUT_MS = 2000
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val state: StateFlow<ConnectionState> = _state

    suspend fun connect() {
        _state.value = ConnectionState.Searching

        // 1. 尝试缓存的 IP
        val cachedIp = prefs.getString(KEY_LAST_IP, null)
        val cachedPort = prefs.getInt(KEY_LAST_PORT, 8080)
        if (cachedIp != null) {
            _state.value = ConnectionState.Connecting(cachedIp, cachedPort)
            val url = "http://$cachedIp:$cachedPort/"
            if (verifyConnection(cachedIp, cachedPort)) {
                Log.d(TAG, "缓存连接成功: $url")
                _state.value = ConnectionState.Connected(url)
                return
            }
            Log.d(TAG, "缓存连接失败，开始 mDNS 发现")
        }

        // 2. mDNS 发现
        _state.value = ConnectionState.Searching
        val discovered = MdnsDiscovery.discover()
        if (discovered != null) {
            saveConnection(discovered.ip, discovered.port)
            val url = "http://${discovered.ip}:${discovered.port}/"
            Log.d(TAG, "mDNS 发现成功: $url")
            _state.value = ConnectionState.Connected(url)
            return
        }

        // 3. 超时，显示手动输入
        Log.d(TAG, "mDNS 超时，切换手动输入")
        _state.value = ConnectionState.ManualInput
    }

    suspend fun connectManual(ip: String, port: Int) {
        _state.value = ConnectionState.Connecting(ip, port)
        if (verifyConnection(ip, port)) {
            saveConnection(ip, port)
            val url = "http://$ip:$port/"
            _state.value = ConnectionState.Connected(url)
        } else {
            _state.value = ConnectionState.Error("连接失败，请检查地址是否正确")
        }
    }

    fun reset() {
        _state.value = ConnectionState.Idle
    }

    private suspend fun verifyConnection(ip: String, port: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("http://$ip:$port/api/system/lan-info")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = CONNECT_TIMEOUT_MS
                conn.readTimeout = CONNECT_TIMEOUT_MS
                conn.requestMethod = "GET"
                val success = conn.responseCode == 200
                conn.disconnect()
                success
            } catch (e: Exception) {
                Log.d(TAG, "连接验证失败: ${e.message}")
                false
            }
        }
    }

    private fun saveConnection(ip: String, port: Int) {
        prefs.edit()
            .putString(KEY_LAST_IP, ip)
            .putInt(KEY_LAST_PORT, port)
            .apply()
    }
}
