package com.glasses.native.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glasses.native.discovery.ConnectionManager
import com.glasses.native.discovery.ConnectionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val connectionManager: ConnectionManager
) : ViewModel() {

    val state: StateFlow<ConnectionState> = connectionManager.state

    fun connect() {
        viewModelScope.launch {
            connectionManager.connect()
        }
    }

    fun connectManual(ip: String, port: Int) {
        viewModelScope.launch {
            connectionManager.connectManual(ip, port)
        }
    }
}
