package com.ciphervpn.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

data class ConnectResponse(
    val status: String,
    val message: String? = null
)

data class StatusResponse(
    val status: String,
    val vpn_connected: Boolean = false,
    val ip: String? = null,
    val error: String? = null
)

data class SettingsResponse(
    val status: String,
    val settings: Map<String, Any>? = null,
    val error: String? = null
)

data class SetSettingResponse(
    val status: String,
    val message: String? = null
)

interface NordVpnApi {
    @GET("/connect")
    suspend fun connect(
        @Query("country") country: String,
        @Query("t") timestamp: Long = System.currentTimeMillis()
    ): Response<ConnectResponse>

    @GET("/status")
    suspend fun getStatus(
        @Query("t") timestamp: Long = System.currentTimeMillis()
    ): Response<StatusResponse>

    @GET("/settings")
    suspend fun getSettings(
        @Query("t") timestamp: Long = System.currentTimeMillis()
    ): Response<SettingsResponse>

    @GET("/set")
    suspend fun updateSetting(
        @Query("feature") feature: String,
        @Query("value") value: String,
        @Query("t") timestamp: Long = System.currentTimeMillis()
    ): Response<SetSettingResponse>
}
