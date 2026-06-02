package com.glasses.app.discovery

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.glasses.app.di.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

sealed class ConnectionState {
    data object Idle : ConnectionState()
    data object Searching : ConnectionState()
    data class Connecting(val ip: String, val port: Int) : ConnectionState()
    data class Connected(val baseUrl: String, val ip: String, val port: Int) : ConnectionState()
    data object ManualInput : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

@Singleton
class ConnectionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "ConnectionManager"
        private const val CONNECT_TIMEOUT_MS = 2000
        private val KEY_IP = stringPreferencesKey("last_ip")
        private val KEY_PORT = intPreferencesKey("last_port")
    }

    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val state: StateFlow<ConnectionState> = _state

    val baseUrl: String?
        get() {
            val s = _state.value
            return if (s is ConnectionState.Connected) s.baseUrl else null
        }

    suspend fun connect() {
        _state.value = ConnectionState.Searching

        val cachedIp = context.dataStore.data.map { it[KEY_IP] }.firstOrNull()
        val cachedPort = context.dataStore.data.map { it[KEY_PORT] }.firstOrNull() ?: 8080

        if (cachedIp != null) {
            _state.value = ConnectionState.Connecting(cachedIp, cachedPort)
            if (verifyConnection(cachedIp, cachedPort)) {
                val url = "http://$cachedIp:$cachedPort"
                Log.d(TAG, "缓存连接成功: $url")
                _state.value = ConnectionState.Connected(url, cachedIp, cachedPort)
                return
            }
            Log.d(TAG, "缓存连接失败，开�?mDNS 发现")
        }

        _state.value = ConnectionState.Searching
        val discovered = MdnsDiscovery.discover()
        if (discovered != null) {
            saveConnection(discovered.ip, discovered.port)
            val url = "http://${discovered.ip}:${discovered.port}"
            Log.d(TAG, "mDNS 发现成功: $url")
            _state.value = ConnectionState.Connected(url, discovered.ip, discovered.port)
            return
        }

        Log.d(TAG, "mDNS 超时，切换手动输�?)
        _state.value = ConnectionState.ManualInput
    }

    suspend fun connectManual(ip: String, port: Int) {
        _state.value = ConnectionState.Connecting(ip, port)
        if (verifyConnection(ip, port)) {
            saveConnection(ip, port)
            val url = "http://$ip:$port"
            _state.value = ConnectionState.Connected(url, ip, port)
        } else {
            _state.value = ConnectionState.Error("连接失败，请检查地址是否正确")
        }
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

    private suspend fun saveConnection(ip: String, port: Int) {
        context.dataStore.edit {
            it[KEY_IP] = ip
            it[KEY_PORT] = port
        }
    }
}
