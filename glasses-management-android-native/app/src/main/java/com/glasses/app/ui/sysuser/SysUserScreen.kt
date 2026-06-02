package com.glasses.app.ui.sysuser

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.glasses.app.data.model.SysUser
import com.glasses.app.viewmodel.SysUserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SysUserScreen(
    onBack: () -> Unit,
    viewModel: SysUserViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("账号管理") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                actions = {
                    IconButton(onClick = { viewModel.loadUsers() }) { Icon(Icons.Default.Refresh, "刷新") }
                }
            )
        }
    ) { padding ->
        Column(modifier = modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(checked = state.includeDeleted, onCheckedChange = { viewModel.toggleIncludeDeleted() })
                Spacer(Modifier.width(8.dp))
                Text("显示已删除")
            }

            when {
                state.isLoading && state.users.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                }
                state.users.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无用户") }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.users, key = { it.id!! }) { user ->
                            UserCard(
                                user = user,
                                onDisable = { viewModel.disableUser(user.id!!) },
                                onEnable = { viewModel.enableUser(user.id!!) },
                                onDelete = { viewModel.showDeleteConfirm(user) },
                                onRestore = { viewModel.restoreUser(user.id!!) },
                                onPurge = { viewModel.showPurgeConfirm(user) },
                                onResetPassword = { viewModel.resetPassword(user.id!!) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.showDeleteConfirm && state.deletingUser != null) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirm() },
            title = { Text("确认删除") },
            text = { Text("确定要删除用户「${state.deletingUser!!.username}」吗？") },
            confirmButton = { TextButton(onClick = { viewModel.deleteUser() }) { Text("删除", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { viewModel.hideDeleteConfirm() }) { Text("取消") } }
        )
    }

    if (state.showPurgeConfirm && state.purgingUser != null) {
        AlertDialog(
            onDismissRequest = { viewModel.hidePurgeConfirm() },
            title = { Text("彻底删除") },
            text = { Text("此操作不可恢复，确定要彻底删除用户「${state.purgingUser!!.username}」吗？") },
            confirmButton = { TextButton(onClick = { viewModel.purgeUser() }) { Text("彻底删除", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { viewModel.hidePurgeConfirm() }) { Text("取消") } }
        )
    }

    if (state.showResetPasswordDialog && state.resetPasswordResult != null) {
        AlertDialog(
            onDismissRequest = { viewModel.hideResetPasswordDialog() },
            title = { Text("密码已重置") },
            text = { Text("临时密码: ${state.resetPasswordResult}\n请告知用户登录后立即修改密码。") },
            confirmButton = { TextButton(onClick = { viewModel.hideResetPasswordDialog() }) { Text("确定") } }
        )
    }
}

@Composable
private fun UserCard(
    user: SysUser,
    onDisable: () -> Unit, onEnable: () -> Unit, onDelete: () -> Unit,
    onRestore: () -> Unit, onPurge: () -> Unit, onResetPassword: () -> Unit
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(user.username ?: "", style = MaterialTheme.typography.titleMedium)
                    if (!user.phone.isNullOrBlank()) Text(user.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row {
                    if (user.deleted == true) {
                        AssistChip(onClick = {}, label = { Text("已删除") }, colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.errorContainer))
                    } else if (user.disabled == true) {
                        AssistChip(onClick = {}, label = { Text("已禁用") }, colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.errorContainer))
                    } else {
                        AssistChip(onClick = {}, label = { Text(user.role ?: "merchant") })
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                if (user.deleted == true) {
                    TextButton(onClick = onRestore) { Text("恢复") }
                    TextButton(onClick = onPurge) { Text("彻底删除", color = MaterialTheme.colorScheme.error) }
                } else {
                    TextButton(onClick = onResetPassword) { Text("重置密码") }
                    if (user.disabled == true) {
                        TextButton(onClick = onEnable) { Text("启用") }
                    } else {
                        TextButton(onClick = onDisable) { Text("禁用") }
                    }
                    TextButton(onClick = onDelete) { Text("删除", color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
}
