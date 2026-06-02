package com.glasses.native.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.glasses.native.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个人资料") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("账号信息", style = MaterialTheme.typography.titleMedium)
                        if (!state.isEditing) {
                            IconButton(onClick = { viewModel.startEditing() }) { Icon(Icons.Default.Edit, "编辑") }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (state.isEditing) {
                        OutlinedTextField(value = state.realName, onValueChange = { viewModel.onRealNameChange(it) }, label = { Text("显示名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = state.username, onValueChange = { viewModel.onUsernameChange(it) }, label = { Text("用户名") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = state.phone, onValueChange = { viewModel.onPhoneChange(it) }, label = { Text("手机号") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { viewModel.cancelEditing() }) { Text("取消") }
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = { viewModel.saveProfile() }, enabled = !state.isLoading) { Text("保存") }
                        }
                    } else {
                        ProfileRow("显示名称", state.realName.ifBlank { "-" })
                        ProfileRow("用户名", state.username)
                        ProfileRow("手机号", state.phone.ifBlank { "-" })
                        ProfileRow("角色", state.role)
                    }
                }
            }

            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("安全", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { viewModel.showChangePassword() }) {
                        Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("修改密码")
                    }
                }
            }
        }
    }

    if (state.showChangePassword) {
        ChangePasswordDialog(
            onDismiss = { viewModel.hideChangePassword() },
            onConfirm = { o, n, c -> viewModel.changePassword(o, n, c) },
            isLoading = state.isLoading
        )
    }

    if (state.success != null) {
        Snackbar(modifier = Modifier.padding(16.dp), action = {
            TextButton(onClick = { viewModel.clearMessages() }) { Text("关闭") }
        }) { Text(state.success!!) }
    }

    if (state.error != null) {
        Snackbar(modifier = Modifier.padding(16.dp), action = {
            TextButton(onClick = { viewModel.clearMessages() }) { Text("关闭") }
        }) { Text(state.error!!) }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(80.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ChangePasswordDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit, isLoading: Boolean) {
    var old by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("修改密码") },
        text = {
            Column {
                OutlinedTextField(value = old, onValueChange = { old = it }, label = { Text("当前密码") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = new, onValueChange = { new = it }, label = { Text("新密码") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = confirm, onValueChange = { confirm = it }, label = { Text("确认新密码") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(old, new, confirm) }, enabled = !isLoading && old.isNotBlank() && new.length >= 6 && new == confirm) { Text("确定") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
