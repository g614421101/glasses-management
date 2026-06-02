package com.glasses.native.ui.datamanage

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.glasses.native.viewmodel.DataManageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManageScreen(
    onBack: () -> Unit,
    viewModel: DataManageViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据管理") },
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
                    Text("导出数据", style = MaterialTheme.typography.titleMedium)
                    Text("将所有数据导出为 JSON 文件", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.exportData() }, enabled = !state.isLoading) {
                        Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("导出 JSON")
                    }
                }
            }

            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("导入数据", style = MaterialTheme.typography.titleMedium)
                    Text("从 JSON 文件导入数据", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Row {
                        FilterChip(selected = state.importMode == "merge", onClick = { viewModel.setImportMode("merge") }, label = { Text("合并") })
                        Spacer(Modifier.width(8.dp))
                        FilterChip(selected = state.importMode == "replace", onClick = { viewModel.setImportMode("replace") }, label = { Text("替换") })
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (state.importMode == "merge") "合并模式：跳过已存在的记录" else "替换模式：清空后导入",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { viewModel.importData(null) }, enabled = !state.isLoading) {
                        Icon(Icons.Default.Upload, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("选择文件并导入")
                    }
                }
            }

            ElevatedCard(Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(Modifier.padding(16.dp)) {
                    Text("重置数据", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                    Text("清空所有业务数据，保留管理员账号", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f))
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.showResetConfirm() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("重置数据") }
                }
            }

            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (state.message != null) {
                Snackbar(modifier = Modifier.fillMaxWidth(), action = {
                    TextButton(onClick = { viewModel.clearMessage() }) { Text("关闭") }
                }) { Text(state.message!!) }
            }
        }
    }

    if (state.showResetConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.hideResetConfirm() },
            title = { Text("确认重置") },
            text = { Text("此操作将清空所有业务数据（顾客、验光、销售记录），仅保留管理员账号。是否继续？") },
            confirmButton = { TextButton(onClick = { viewModel.resetData() }) { Text("重置", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { viewModel.hideResetConfirm() }) { Text("取消") } }
        )
    }
}
