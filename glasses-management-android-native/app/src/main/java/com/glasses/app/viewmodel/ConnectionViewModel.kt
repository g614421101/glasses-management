package com.glasses.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glasses.app.discovery.ConnectionManager
import com.glasses.app.discovery.ConnectionState
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
