package com.glasses.native.ui.splash

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.glasses.native.discovery.ConnectionState

@Composable
fun SplashScreen(
    connectionState: ConnectionState,
    onConnectManual: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "视光档案",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(48.dp))

        when (connectionState) {
            is ConnectionState.Searching, is ConnectionState.Connecting -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "正在搜索桌面端服务…",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            is ConnectionState.ManualInput -> {
                ManualInputSection(onConnectManual)
            }
            is ConnectionState.Error -> {
                Text(
                    text = connectionState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                ManualInputSection(onConnectManual)
            }
            else -> {}
        }
    }
}

@Composable
private fun ManualInputSection(onConnect: (String, Int) -> Unit) {
    var ip by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("8080") }

    Text(
        text = "未找到桌面端服务，请确认已启动",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(24.dp))

    OutlinedTextField(
        value = ip,
        onValueChange = { ip = it },
        label = { Text("IP 地址") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = port,
        onValueChange = { port = it },
        label = { Text("端口号") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = {
            val p = port.toIntOrNull() ?: 8080
            if (ip.isNotBlank()) onConnect(ip.trim(), p)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("连接")
    }
}
