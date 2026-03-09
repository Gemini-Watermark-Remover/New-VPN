package com.ciphervpn

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ciphervpn.service.ProxyVpnService
import com.ciphervpn.ui.HomeScreen
import com.ciphervpn.ui.MainViewModel
import com.ciphervpn.ui.SettingsScreen
import com.ciphervpn.ui.theme.CipherVPNTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val startVpnLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Permission granted, start service
            val intent = Intent(this, ProxyVpnService::class.java)
            startService(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CipherVPNTheme {
                val navController = rememberNavController()
                
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateSettings = { navController.navigate("settings") },
                            onStartVpn = { startVpn() },
                            onStopVpn = { stopVpn() }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    private fun startVpn() {
        // Asks user for permission to route traffic to VPN framework
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startVpnLauncher.launch(intent)
        } else {
            // Already prepared
            val serviceIntent = Intent(this, ProxyVpnService::class.java)
            startService(serviceIntent)
        }
    }

    private fun stopVpn() {
        val intent = Intent(this, ProxyVpnService::class.java).apply {
            action = ProxyVpnService.ACTION_DISCONNECT
        }
        startService(intent)
    }
}
