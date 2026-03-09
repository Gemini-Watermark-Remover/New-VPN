package com.ciphervpn.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ciphervpn.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateSettings: () -> Unit,
    onStartVpn: () -> Unit,
    onStopVpn: () -> Unit
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val currentIp by viewModel.currentIp.collectAsState()
    val selectedServer by viewModel.selectedServer.collectAsState(initial = "United_States")

    var isDropdownExpanded by remember { mutableStateOf(false) }
    val servers = listOf("United_States", "United_Kingdom", "Canada", "Germany", "France", "Japan", "Australia", "Singapore")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("CipherVPN", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onNavigateSettings) {
                Text("⚙️", fontSize = 24.sp)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Connection Status text
        val (statusText, statusColor, statusSub) = when (connectionState) {
            ConnectionState.CONNECTED -> Triple("Secured", AccentColor, "Your traffic is secured")
            ConnectionState.CONNECTING -> Triple("Connecting...", AccentColor, "Establishing VPN tunnel...")
            ConnectionState.DISCONNECTING -> Triple("Disconnecting...", DisconnectedColor, "Closing VPN tunnel...")
            ConnectionState.DISCONNECTED -> Triple("Disconnected", DisconnectedColor, "Ready to secure")
        }

        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(if (connectionState == ConnectionState.CONNECTED) AccentColor.copy(alpha=0.15f) else CardBackground)
                .clickable {
                    if (connectionState == ConnectionState.CONNECTED) {
                        viewModel.toggleConnection("France")
                        onStopVpn()
                    } else if (connectionState == ConnectionState.DISCONNECTED) {
                         onStartVpn()
                         viewModel.toggleConnection(selectedServer)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val iconColor = if (connectionState == ConnectionState.CONNECTED) AccentColor else DisconnectedColor
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⏻", fontSize = 72.sp, color = iconColor)
                Spacer(modifier = Modifier.height(16.dp))
                Text(statusText, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                Text(statusSub, color = TextSecondary, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // IP Display
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                 Text("Current IP", color = TextSecondary)
                 Text(currentIp, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Server Selection
        ExposedDropdownMenuBox(
            expanded = isDropdownExpanded,
            onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
        ) {
            OutlinedTextField(
                value = selectedServer.replace("_", " "),
                onValueChange = {},
                readOnly = true,
                label = { Text("Selected Server", color=TextSecondary) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentColor,
                    unfocusedBorderColor = BorderColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = AccentColor
                ),
                modifier = Modifier
                     .menuAnchor()
                     .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false },
                modifier = Modifier.background(CardBackground)
            ) {
                servers.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption.replace("_", " "), color=TextPrimary) },
                        onClick = {
                            viewModel.selectServer(selectionOption)
                            isDropdownExpanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
