package com.ciphervpn.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ciphervpn.data.SettingsRepository
import com.ciphervpn.network.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

enum class ConnectionState {
    DISCONNECTED, CONNECTING, CONNECTED, DISCONNECTING
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)
    private val api = NetworkModule.api

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _currentIp = MutableStateFlow("Loading...")
    val currentIp: StateFlow<String> = _currentIp

    val selectedServer = repository.selectedServerFlow

    private val _settings = MutableStateFlow<Map<String, Any>>(emptyMap())
    val settings: StateFlow<Map<String, Any>> = _settings

    init {
        fetchIp()
        checkStatus()
    }

    fun fetchIp() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ipRes = URL("https://api.ipify.org?format=json").readText()
                val ip = JSONObject(ipRes).getString("ip")
                _currentIp.value = ip
            } catch (e: Exception) {
                _currentIp.value = "Unavailable"
            }
        }
    }

    private fun checkStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = api.getStatus()
                if (res.isSuccessful && res.body()?.status == "success") {
                    if (res.body()?.vpn_connected == true) {
                        _connectionState.value = ConnectionState.CONNECTED
                        _currentIp.value = res.body()?.ip ?: "Unavailable"
                    } else {
                        _connectionState.value = ConnectionState.DISCONNECTED
                    }
                }
            } catch (e: Exception) {
                // Network error means proxy is down or VPS is unreachable
            }
        }
    }

    fun selectServer(server: String) {
        viewModelScope.launch {
            repository.saveSelectedServer(server)
            if (_connectionState.value == ConnectionState.CONNECTED) {
                toggleConnection(server) // Reconnect to new server
            }
        }
    }

    fun toggleConnection(server: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val isDisconnect = _connectionState.value == ConnectionState.CONNECTED
            if (isDisconnect) {
                _connectionState.value = ConnectionState.DISCONNECTING
                try {
                    api.connect("France")
                    _connectionState.value = ConnectionState.DISCONNECTED
                } catch(e: Exception) {
                    _connectionState.value = ConnectionState.DISCONNECTED
                }
            } else {
                _connectionState.value = ConnectionState.CONNECTING
                try {
                    // Start connection process on VPS
                    api.connect(server)
                    
                    // Poll status
                    var connected = false
                    for(i in 1..15) {
                        delay(2000)
                        try {
                            val statusRes = api.getStatus()
                            if (statusRes.isSuccessful && statusRes.body()?.vpn_connected == true) {
                                connected = true
                                break
                            }
                        } catch(e: Exception) {
                            // Poll failure expected during route change
                        }
                    }
                    if (connected) {
                         _connectionState.value = ConnectionState.CONNECTED
                    } else {
                         _connectionState.value = ConnectionState.DISCONNECTED
                    }
                } catch(e: Exception) {
                    _connectionState.value = ConnectionState.DISCONNECTED
                }
            }
            fetchIp()
        }
    }

    fun fetchSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = api.getSettings()
                if (res.isSuccessful) {
                    _settings.value = res.body()?.settings ?: emptyMap()
                }
            } catch(e: Exception) {}
        }
    }

    fun updateSetting(feature: String, on: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                api.updateSetting(feature, if (on) "on" else "off")
                fetchSettings()
            } catch(e: Exception) {}
        }
    }

    fun updateProtocol(protocol: String) {
        viewModelScope.launch(Dispatchers.IO) {
             try {
                api.updateSetting("technology", protocol)
                fetchSettings()
             } catch(e: Exception) {}
        }
    }
}
