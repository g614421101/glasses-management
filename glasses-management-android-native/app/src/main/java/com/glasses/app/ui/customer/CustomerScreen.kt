package com.glasses.app.ui.customer

import androidx.compose.foundation.clickable
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
import com.glasses.app.data.model.Customer
import com.glasses.app.viewmodel.CustomerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(
    viewModel: CustomerViewModel = hiltViewModel(),
    onNavigateToArchive: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("顾客管理") },
            actions = {
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新")
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.searchKeyword,
                onValueChange = { viewModel.onSearchChange(it) },
                placeholder = { Text("搜索姓名或手机号") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                trailingIcon = {
                    if (state.searchKeyword.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchChange(""); viewModel.onSearch() }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除")
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { viewModel.onSearch() }) {
                Icon(Icons.Default.Search, contentDescription = "搜索")
            }
            Spacer(modifier = Modifier.width(4.dp))
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加顾客", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }

        Text(
            text = "�?${state.totalRecords} 位顾�?,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        when {
            state.isLoading && state.customers.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.customers.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无顾客数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.customers, key = { it.id!! }) { customer ->
                        CustomerCard(
                            customer = customer,
                            onClick = { onNavigateToArchive(customer.id!!) },
                            onEdit = { viewModel.showEditDialog(customer) },
                            onDelete = { viewModel.showDeleteConfirm(customer) }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { viewModel.onPrevPage() },
                        enabled = state.currentPage > 1
                    ) {
                        Text("上一�?)
                    }
                    Text(
                        text = "${state.currentPage} / ${state.totalPages}",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    TextButton(
                        onClick = { viewModel.onNextPage() },
                        enabled = state.currentPage < state.totalPages
                    ) {
                        Text("下一�?)
                    }
                }
            }
        }

        if (state.error != null) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("关闭")
                    }
                }
            ) {
                Text(state.error!!)
            }
        }
    }

    if (state.showAddDialog) {
        CustomerDialog(
            title = "新增顾客",
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { viewModel.addCustomer(it) }
        )
    }

    if (state.showEditDialog && state.editingCustomer != null) {
        CustomerDialog(
            title = "编辑顾客",
            customer = state.editingCustomer,
            onDismiss = { viewModel.hideEditDialog() },
            onConfirm = { viewModel.updateCustomer(it) }
        )
    }

    if (state.showDeleteConfirm && state.deletingCustomer != null) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirm() },
            title = { Text("确认删除") },
            text = { Text("确定要删除顾客�?{state.deletingCustomer!!.name}」吗？删除后可前往回收站恢复�?) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteCustomer() }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteConfirm() }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun CustomerCard(
    customer: Customer,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (!customer.phone.isNullOrBlank()) {
                        Text(
                            text = customer.phone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row {
                    val genderText = when (customer.gender) {
                        1 -> "�?
                        2 -> "�?
                        else -> ""
                    }
                    if (genderText.isNotEmpty()) {
                        AssistChip(
                            onClick = {},
                            label = { Text(genderText) },
                            modifier = Modifier.height(28.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "删除", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (!customer.birthday.isNullOrBlank() || !customer.remark.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                if (!customer.birthday.isNullOrBlank()) {
                    Text(
                        text = "生日: ${customer.birthday}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!customer.remark.isNullOrBlank()) {
                    Text(
                        text = customer.remark,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            if (!customer.createTime.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "注册: ${customer.createTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerDialog(
    title: String,
    customer: Customer? = null,
    onDismiss: () -> Unit,
    onConfirm: (Customer) -> Unit
) {
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }
    var gender by remember { mutableIntStateOf(customer?.gender ?: 0) }
    var birthday by remember { mutableStateOf(customer?.birthday ?: "") }
    var remark by remember { mutableStateOf(customer?.remark ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("姓名 *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("手机�?) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("性别", style = MaterialTheme.typography.bodyMedium)
                Row {
                    FilterChip(
                        selected = gender == 0,
                        onClick = { gender = 0 },
                        label = { Text("未知") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = gender == 1,
                        onClick = { gender = 1 },
                        label = { Text("�?) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = gender == 2,
                        onClick = { gender = 2 },
                        label = { Text("�?) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = birthday,
                    onValueChange = { birthday = it },
                    label = { Text("生日（yyyy-MM-dd�?) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            Customer(
                                id = customer?.id,
                                name = name.trim(),
                                phone = phone.trim().ifBlank { null },
                                gender = gender,
                                birthday = birthday.trim().ifBlank { null },
                                remark = remark.trim().ifBlank { null }
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
