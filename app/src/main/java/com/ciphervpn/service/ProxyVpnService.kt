package com.ciphervpn.service

import android.content.Intent
import android.net.ProxyInfo
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log

class ProxyVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_DISCONNECT) {
            stopVpn()
            return START_NOT_STICKY
        }

        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        if (vpnInterface != null) {
            Log.d("ProxyVpnService", "VPN is already running")
            return
        }

        val builder = Builder()
        builder.setSession("CipherVPN")
        
        // Android proxy routing magic! (API 29+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Forward traffic through our Squid Proxy on the VPS, excluding the VPS IP so API requests work
            val proxyInfo = ProxyInfo.buildDirectProxy("76.13.59.198", 8899, listOf("localhost", "127.0.0.1", "76.13.59.198"))
            builder.setHttpProxy(proxyInfo)
        }

        // Technically need to add an address to make Android route traffic to the TUN interface.
        // A dummy local IP is fine.
        builder.addAddress("10.0.0.2", 24)
        
        // Route all traffic
        builder.addRoute("0.0.0.0", 0)

        // Exclude the VPS API endpoint from the VPN routing table so the tunnel connection to port 8900 does not drop
        // But Android builder.addRoute requires routing IPs, so we could explicitly not route the VPS instead of excluding,
        // but there is no excludeRoute in early APIs. However, because we already set exception in setHttpProxy,
        // standard HTTP requests to 76.13.59.198 will bypass the proxy anyway!

        try {
            vpnInterface = builder.establish()
            Log.d("ProxyVpnService", "VPN established!")
        } catch (e: Exception) {
            Log.e("ProxyVpnService", "Error establishing VPN", e)
            stopVpn()
        }
    }

    private fun stopVpn() {
        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            Log.e("ProxyVpnService", "Error closing VPN interface", e)
        } finally {
            vpnInterface = null
        }
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
    }

    companion object {
        const val ACTION_DISCONNECT = "com.ciphervpn.DISCONNECT"
    }
}
