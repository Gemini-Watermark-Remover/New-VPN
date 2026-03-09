package com.ciphervpn.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ciphervpn.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchSettings()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Text("←", fontSize = 24.sp, color = TextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Settings", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("PREFERENCES", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))

            SettingToggle("Kill Switch", "Block internet when VPN drops", settings["kill_switch"] as? Boolean ?: false) { 
                viewModel.updateSetting("kill_switch", it) 
            }
            SettingToggle("Threat Protection Lite", "Block ads and malware", settings["threat_protection_lite"] as? Boolean ?: false) { 
                viewModel.updateSetting("threat_protection_lite", it) 
            }
            SettingToggle("Auto Connect", "Connect on startup", settings["auto-connect"] as? Boolean ?: false) { 
                viewModel.updateSetting("auto-connect", it) 
            }
            // Add remaining toggles based on the VPS NordVPN config
            SettingToggle("Meshnet", "Link devices securely", settings["meshnet"] as? Boolean ?: false) { 
                viewModel.updateSetting("meshnet", it) 
            }
            SettingToggle("LAN Discovery", "Access local network", settings["lan_discovery"] as? Boolean ?: false) { 
                viewModel.updateSetting("lan_discovery", it) 
            }
            SettingToggle("Virtual Location", "Allow virtual servers", settings["virtual_location"] as? Boolean ?: false) { 
                viewModel.updateSetting("virtual_location", it) 
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("CONNECTION", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
            
            val currentTech = (settings["technology"] as? String)?.lowercase() ?: "nordlynx"
            val isNordLynx = currentTech.contains("nordlynx")
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Protocol", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text(if (isNordLynx) "NordLynx" else "OpenVPN", color = TextSecondary, fontSize = 14.sp)
                }
                
                Button(
                    onClick = { viewModel.updateProtocol(if (isNordLynx) "OpenVPN" else "NordLynx") },
                    colors = ButtonDefaults.buttonColors(containerColor = CardBackground)
                ) {
                    Text("Toggle", color = AccentColor)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextSecondary, fontSize = 14.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentColor,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = CardBackground,
                uncheckedBorderColor = BorderColor
            )
        )
    }
}
