package com.glasses.native.ui.archive

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
import com.glasses.native.data.model.OptometryRecord
import com.glasses.native.data.model.SalesRecord
import com.glasses.native.data.model.TimelineItem
import com.glasses.native.viewmodel.ArchiveViewModel
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    customerId: Long,
    onBack: () -> Unit,
    viewModel: ArchiveViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(customerId) {
        viewModel.loadArchive(customerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("顾客档案") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }
            )
        },
        floatingActionButton = {
            Column {
                SmallFloatingActionButton(
                    onClick = { viewModel.showAddSales() },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "添加配镜单", tint = MaterialTheme.colorScheme.onSecondary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                SmallFloatingActionButton(
                    onClick = { viewModel.showAddOptometry() }
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = "添加验光单")
                }
            }
        }
    ) { padding ->
        when {
            state.isLoading && state.timeline.isEmpty() -> {
                Box(modifier = modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null && state.timeline.isEmpty() -> {
                Box(modifier = modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("加载失败: ${state.error}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }) { Text("重试") }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Customer info card
                    item {
                        state.customer?.let { c ->
                            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(c.name, style = MaterialTheme.typography.titleLarge)
                                    if (!c.phone.isNullOrBlank()) {
                                        Text(c.phone, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Row(modifier = Modifier.padding(top = 8.dp)) {
                                        val gender = when (c.gender) { 1 -> "男"; 2 -> "女"; else -> "未知" }
                                        AssistChip(onClick = {}, label = { Text(gender) }, modifier = Modifier.height(28.dp))
                                        if (!c.birthday.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            AssistChip(onClick = {}, label = { Text(c.birthday) }, modifier = Modifier.height(28.dp))
                                        }
                                    }
                                    Text(
                                        "共 ${state.timeline.size} 条记录",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Timeline
                    if (state.timeline.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("暂无验光或配镜记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    items(state.timeline, key = { "${it.type}_${it.date}_${it.hashCode()}" }) { item ->
                        TimelineCard(
                            item = item,
                            onEditOptometry = { record ->
                                viewModel.showEditOptometry(record)
                            },
                            onEditSales = { record ->
                                viewModel.showEditSales(record)
                            },
                            onDelete = { type, id ->
                                viewModel.showDeleteConfirm(type, id)
                            }
                        )
                    }
                }
            }
        }

        // Error snackbar
        if (state.error != null) {
            Snackbar(modifier = Modifier.padding(16.dp), action = {
                TextButton(onClick = { viewModel.clearError() }) { Text("关闭") }
            }) { Text(state.error!!) }
        }
    }

    // Dialogs
    if (state.showAddOptometry) {
        OptometryDialog(
            title = "录入验光单",
            onDismiss = { viewModel.hideAddOptometry() },
            onConfirm = { viewModel.addOptometry(it) }
        )
    }

    if (state.showEditOptometry && state.editingOptometry != null) {
        OptometryDialog(
            title = "编辑验光单",
            record = state.editingOptometry,
            onDismiss = { viewModel.hideEditOptometry() },
            onConfirm = { viewModel.updateOptometry(it) }
        )
    }

    if (state.showAddSales) {
        SalesDialog(
            title = "新增配镜单",
            onDismiss = { viewModel.hideAddSales() },
            onConfirm = { viewModel.addSales(it) }
        )
    }

    if (state.showEditSales && state.editingSales != null) {
        SalesDialog(
            title = "编辑配镜单",
            record = state.editingSales,
            onDismiss = { viewModel.hideEditSales() },
            onConfirm = { viewModel.updateSales(it) }
        )
    }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirm() },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条记录吗？") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteRecord() }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteConfirm() }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun TimelineCard(
    item: TimelineItem,
    onEditOptometry: (OptometryRecord) -> Unit,
    onEditSales: (SalesRecord) -> Unit,
    onDelete: (String, Long) -> Unit
) {
    val isOptometry = item.type == "OPTOMETRY"
    val cardColor = if (isOptometry) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isOptometry) Icons.Default.Visibility else Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = if (isOptometry) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.title ?: if (isOptometry) "验光单" else "配镜单",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = item.date ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!item.subtitle.isNullOrBlank()) {
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Parse data for edit/delete actions
            val dataMap = item.data as? Map<*, *> ?: emptyMap<String, Any>()
            val recordId = (dataMap["id"] as? Double)?.toLong()

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (recordId != null) {
                    TextButton(onClick = {
                        if (isOptometry) {
                            onEditOptometry(parseOptometry(dataMap))
                        } else {
                            onEditSales(parseSales(dataMap))
                        }
                    }) {
                        Text("编辑")
                    }
                    TextButton(onClick = {
                        onDelete(item.type, recordId)
                    }) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

private fun parseOptometry(map: Map<*, *>): OptometryRecord {
    return OptometryRecord(
        id = (map["id"] as? Double)?.toLong(),
        customerId = (map["customerId"] as? Double)?.toLong(),
        odSph = (map["odSph"] as? Number)?.let { BigDecimal.valueOf(it.toDouble()) },
        odCyl = (map["odCyl"] as? Number)?.let { BigDecimal.valueOf(it.toDouble()) },
        odAxis = (map["odAxis"] as? Double)?.toInt(),
        odVa = map["odVa"] as? String,
        osSph = (map["osSph"] as? Number)?.let { BigDecimal.valueOf(it.toDouble()) },
        osCyl = (map["osCyl"] as? Number)?.let { BigDecimal.valueOf(it.toDouble()) },
        osAxis = (map["osAxis"] as? Double)?.toInt(),
        osVa = map["osVa"] as? String,
        odPd = (map["odPd"] as? Number)?.let { BigDecimal.valueOf(it.toDouble()) },
        osPd = (map["osPd"] as? Number)?.let { BigDecimal.valueOf(it.toDouble()) },
        pdFar = (map["pdFar"] as? Number)?.let { BigDecimal.valueOf(it.toDouble()) },
        pdNear = (map["pdNear"] as? Number)?.let { BigDecimal.valueOf(it.toDouble()) },
        addPower = (map["addPower"] as? Number)?.let { BigDecimal.valueOf(it.toDouble()) },
        optometristName = map["optometristName"] as? String,
        remark = map["remark"] as? String,
        examDate = map["examDate"] as? String
    )
}

private fun parseSales(map: Map<*, *>): SalesRecord {
    return SalesRecord(
        id = (map["id"] as? Double)?.toLong(),
        customerId = (map["customerId"] as? Double)?.toLong(),
        optometryId = (map["optometryId"] as? Double)?.toLong(),
        recordNo = map["recordNo"] as? String,
        frameBrand = map["frameBrand"] as? String,
        frameModel = map["frameModel"] as? String,
        frameQuantity = (map["frameQuantity"] as? Double)?.toInt(),
        frameRetailPrice = (map["frameRetailPrice"] as? Number)?.let { BigDecimal.valueOf(it.toDouble()) },
        framePrice = (map["framePrice"] as? Number)?.let { BigDecimal.valueOf(it.toDouble()) },
        lensBrand = map["lensBrand"] as? String,
        lensParams = map["lensParams"] as? String,
        lensQuantity = (map["lensQuantity"] as? Double)?.toInt(),
        lensRetailPrice = (map["lensRetailPrice"] as? Number)?.let { BigDecimal.valueOf(it.toDouble()) },
        lensPrice = (map["lensPrice"] as? Number)?.let { BigDecimal.valueOf(it.toDouble()) },
        totalRetailPrice = (map["totalRetailPrice"] as? Number)?.let { BigDecimal.valueOf(it.toDouble()) },
        totalAmount = (map["totalAmount"] as? Number)?.let { BigDecimal.valueOf(it.toDouble()) },
        remark = map["remark"] as? String,
        salesDate = map["salesDate"] as? String
    )
}

@Composable
private fun OptometryDialog(
    title: String,
    record: OptometryRecord? = null,
    onDismiss: () -> Unit,
    onConfirm: (OptometryRecord) -> Unit
) {
    var odSph by remember { mutableStateOf(record?.odSph?.toString() ?: "") }
    var odCyl by remember { mutableStateOf(record?.odCyl?.toString() ?: "") }
    var odAxis by remember { mutableStateOf(record?.odAxis?.toString() ?: "") }
    var odVa by remember { mutableStateOf(record?.odVa ?: "") }
    var osSph by remember { mutableStateOf(record?.osSph?.toString() ?: "") }
    var osCyl by remember { mutableStateOf(record?.osCyl?.toString() ?: "") }
    var osAxis by remember { mutableStateOf(record?.osAxis?.toString() ?: "") }
    var osVa by remember { mutableStateOf(record?.osVa ?: "") }
    var pdFar by remember { mutableStateOf(record?.pdFar?.toString() ?: "") }
    var addPower by remember { mutableStateOf(record?.addPower?.toString() ?: "") }
    var optometrist by remember { mutableStateOf(record?.optometristName ?: "") }
    var remark by remember { mutableStateOf(record?.remark ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text("右眼 (OD)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Row {
                    OutlinedTextField(value = odSph, onValueChange = { odSph = it }, label = { Text("SPH") }, singleLine = true, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(4.dp))
                    OutlinedTextField(value = odCyl, onValueChange = { odCyl = it }, label = { Text("CYL") }, singleLine = true, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(4.dp))
                    OutlinedTextField(value = odAxis, onValueChange = { odAxis = it }, label = { Text("AXIS") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = odVa, onValueChange = { odVa = it }, label = { Text("视力") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(8.dp))
                Text("左眼 (OS)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                Row {
                    OutlinedTextField(value = osSph, onValueChange = { osSph = it }, label = { Text("SPH") }, singleLine = true, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(4.dp))
                    OutlinedTextField(value = osCyl, onValueChange = { osCyl = it }, label = { Text("CYL") }, singleLine = true, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(4.dp))
                    OutlinedTextField(value = osAxis, onValueChange = { osAxis = it }, label = { Text("AXIS") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = osVa, onValueChange = { osVa = it }, label = { Text("视力") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    OutlinedTextField(value = pdFar, onValueChange = { pdFar = it }, label = { Text("瞳距") }, singleLine = true, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(4.dp))
                    OutlinedTextField(value = addPower, onValueChange = { addPower = it }, label = { Text("下加光") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = optometrist, onValueChange = { optometrist = it }, label = { Text("验光师") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = remark, onValueChange = { remark = it }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        OptometryRecord(
                            id = record?.id,
                            customerId = record?.customerId,
                            odSph = odSph.toBigDecimalOrNull(),
                            odCyl = odCyl.toBigDecimalOrNull(),
                            odAxis = odAxis.toIntOrNull(),
                            odVa = odVa.ifBlank { null },
                            osSph = osSph.toBigDecimalOrNull(),
                            osCyl = osCyl.toBigDecimalOrNull(),
                            osAxis = osAxis.toIntOrNull(),
                            osVa = osVa.ifBlank { null },
                            pdFar = pdFar.toBigDecimalOrNull(),
                            addPower = addPower.toBigDecimalOrNull(),
                            optometristName = optometrist.ifBlank { null },
                            remark = remark.ifBlank { null }
                        )
                    )
                }
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun SalesDialog(
    title: String,
    record: SalesRecord? = null,
    onDismiss: () -> Unit,
    onConfirm: (SalesRecord) -> Unit
) {
    var frameBrand by remember { mutableStateOf(record?.frameBrand ?: "") }
    var frameModel by remember { mutableStateOf(record?.frameModel ?: "") }
    var framePrice by remember { mutableStateOf(record?.framePrice?.toString() ?: "") }
    var lensBrand by remember { mutableStateOf(record?.lensBrand ?: "") }
    var lensParams by remember { mutableStateOf(record?.lensParams ?: "") }
    var lensPrice by remember { mutableStateOf(record?.lensPrice?.toString() ?: "") }
    var remark by remember { mutableStateOf(record?.remark ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text("镜架", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(value = frameBrand, onValueChange = { frameBrand = it }, label = { Text("品牌") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = frameModel, onValueChange = { frameModel = it }, label = { Text("型号") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = framePrice, onValueChange = { framePrice = it }, label = { Text("售价(元)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(8.dp))
                Text("镜片", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                OutlinedTextField(value = lensBrand, onValueChange = { lensBrand = it }, label = { Text("品牌") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = lensParams, onValueChange = { lensParams = it }, label = { Text("参数") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = lensPrice, onValueChange = { lensPrice = it }, label = { Text("售价(元)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = remark, onValueChange = { remark = it }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val fp = framePrice.toBigDecimalOrNull()
                    val lp = lensPrice.toBigDecimalOrNull()
                    val total = if (fp != null && lp != null) fp.add(lp) else fp ?: lp
                    onConfirm(
                        SalesRecord(
                            id = record?.id,
                            customerId = record?.customerId,
                            frameBrand = frameBrand.ifBlank { null },
                            frameModel = frameModel.ifBlank { null },
                            frameQuantity = 1,
                            framePrice = fp,
                            lensBrand = lensBrand.ifBlank { null },
                            lensParams = lensParams.ifBlank { null },
                            lensQuantity = 1,
                            lensPrice = lp,
                            totalAmount = total,
                            remark = remark.ifBlank { null }
                        )
                    )
                }
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
