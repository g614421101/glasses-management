package com.glasses.app.ui.recyclebin

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
import com.glasses.app.viewmodel.RecycleBinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleBinScreen(
    onBack: () -> Unit,
    viewModel: RecycleBinViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("回收�?) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                actions = {
                    IconButton(onClick = { viewModel.loadRecycleBin() }) { Icon(Icons.Default.Refresh, "刷新") }
                    IconButton(onClick = { viewModel.showEmptyConfirm() }) { Icon(Icons.Default.DeleteSweep, "清空", tint = MaterialTheme.colorScheme.error) }
                }
            )
        }
    ) { padding ->
        Column(modifier = modifier.fillMaxSize().padding(padding)) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                FilterChip(selected = state.filterType == "all", onClick = { viewModel.onFilterChange("all") }, label = { Text("全部") })
                Spacer(Modifier.width(8.dp))
                FilterChip(selected = state.filterType == "customer", onClick = { viewModel.onFilterChange("customer") }, label = { Text("顾客") })
                Spacer(Modifier.width(8.dp))
                FilterChip(selected = state.filterType == "optometry", onClick = { viewModel.onFilterChange("optometry") }, label = { Text("验光") })
                Spacer(Modifier.width(8.dp))
                FilterChip(selected = state.filterType == "sales", onClick = { viewModel.onFilterChange("sales") }, label = { Text("配镜") })
            }

            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                }
                state.data.customers.isEmpty() && state.data.optometryRecords.isEmpty() && state.data.salesRecords.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("回收站为�?) }
                }
                else -> {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (state.filterType == "all" || state.filterType == "customer") {
                            items(state.data.customers, key = { "c_${it.id}" }) { c ->
                                RecycleItemCard(
                                    title = c.name,
                                    subtitle = c.phone ?: "",
                                    type = "顾客",
                                    onRestore = { viewModel.restore("customer", c.id!!) },
                                    onPurge = { viewModel.showPurgeConfirm("customer", c.id!!) }
                                )
                            }
                        }
                        if (state.filterType == "all" || state.filterType == "optometry") {
                            items(state.data.optometryRecords, key = { "o_${it.id}" }) { r ->
                                RecycleItemCard(
                                    title = r.customerName ?: "验光记录",
                                    subtitle = r.optometristName ?: "",
                                    type = "验光",
                                    onRestore = { viewModel.restore("optometry", r.id!!) },
                                    onPurge = { viewModel.showPurgeConfirm("optometry", r.id!!) }
                                )
                            }
                        }
                        if (state.filterType == "all" || state.filterType == "sales") {
                            items(state.data.salesRecords, key = { "s_${it.id}" }) { r ->
                                RecycleItemCard(
                                    title = r.customerName ?: "配镜记录",
                                    subtitle = "¥${r.totalAmount ?: 0}",
                                    type = "配镜",
                                    onRestore = { viewModel.restore("sales", r.id!!) },
                                    onPurge = { viewModel.showPurgeConfirm("sales", r.id!!) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showPurgeConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.hidePurgeConfirm() },
            title = { Text("彻底删除") },
            text = { Text("此操作不可恢复，确定要彻底删除吗�?) },
            confirmButton = { TextButton(onClick = { viewModel.purge() }) { Text("彻底删除", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { viewModel.hidePurgeConfirm() }) { Text("取消") } }
        )
    }

    if (state.showEmptyConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.hideEmptyConfirm() },
            title = { Text("清空回收�?) },
            text = { Text("确定要彻底删除回收站中的所有数据吗？此操作不可恢复�?) },
            confirmButton = { TextButton(onClick = { viewModel.emptyRecycleBin() }) { Text("清空", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { viewModel.hideEmptyConfirm() }) { Text("取消") } }
        )
    }
}

@Composable
private fun RecycleItemCard(title: String, subtitle: String, type: String, onRestore: () -> Unit, onPurge: () -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AssistChip(onClick = {}, label = { Text(type) }, modifier = Modifier.height(28.dp))
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = onRestore) { Text("恢复") }
            TextButton(onClick = onPurge) { Text("删除", color = MaterialTheme.colorScheme.error) }
        }
    }
}
