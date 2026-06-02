package com.glasses.app.ui.archive

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.glasses.app.data.model.OptometryRecord
import com.glasses.app.data.model.SalesRecord
import com.glasses.app.data.model.TimelineItem
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.glasses.app.theme.*
import com.glasses.app.ui.common.bounceClick
import com.glasses.app.ui.common.staggeredEntrance
import com.glasses.app.viewmodel.ArchiveViewModel
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
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("顾客档案", fontWeight = FontWeight.Bold, color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextSecondary,
                    actionIconContentColor = TextSecondary
                ),
                navigationIcon = {
                    val backInteractionSource = remember { MutableInteractionSource() }
                    IconButton(
                        onClick = onBack,
                        interactionSource = backInteractionSource,
                        modifier = Modifier.bounceClick(backInteractionSource)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    val refreshInteractionSource = remember { MutableInteractionSource() }
                    IconButton(
                        onClick = { viewModel.refresh() },
                        interactionSource = refreshInteractionSource,
                        modifier = Modifier.bounceClick(refreshInteractionSource)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                },
                modifier = Modifier.shadow(4.dp, spotColor = CardShadow)
            )
        },
        floatingActionButton = {
            Column {
                val addSalesInteractionSource = remember { MutableInteractionSource() }
                FloatingActionButton(
                    onClick = { viewModel.showAddSales() },
                    interactionSource = addSalesInteractionSource,
                    containerColor = Primary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(54.dp)
                        .bounceClick(addSalesInteractionSource)
                        .shadow(6.dp, CircleShape, spotColor = Primary.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "添加配镜单", modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                val addOptometryInteractionSource = remember { MutableInteractionSource() }
                FloatingActionButton(
                    onClick = { viewModel.showAddOptometry() },
                    interactionSource = addOptometryInteractionSource,
                    containerColor = SkyBlue,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(54.dp)
                        .bounceClick(addOptometryInteractionSource)
                        .shadow(6.dp, CircleShape, spotColor = SkyBlue.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = "添加验光单", modifier = Modifier.size(24.dp))
                }
            }
        }
    ) { padding ->
        when {
            state.isLoading && state.timeline.isEmpty() -> {
                Box(modifier = modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            state.error != null && state.timeline.isEmpty() -> {
                Box(modifier = modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("加载失败: ${state.error}", color = Error, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            modifier = Modifier.bounceClick { viewModel.refresh() }
                        ) {
                            Text("重试")
                        }
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
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = CardShadow),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, BorderColor)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = c.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    if (!c.phone.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = c.phone,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary
                                        )
                                    }
                                    Row(modifier = Modifier.padding(top = 10.dp)) {
                                        val gender = when (c.gender) { 1 -> "男"; 2 -> "女"; else -> "未知" }
                                        val chipBg = if (c.gender == 1) MaleBadgeBg else if (c.gender == 2) FemaleBadgeBg else BorderColor
                                        val chipText = if (c.gender == 1) MaleBadge else if (c.gender == 2) FemaleBadge else TextSecondary
                                        Box(
                                            modifier = Modifier
                                                .background(chipBg, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Text(gender, fontSize = 12.sp, color = chipText, fontWeight = FontWeight.Bold)
                                        }

                                        if (!c.birthday.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(PrimaryLight, RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                            ) {
                                                Text(c.birthday, fontSize = 12.sp, color = Primary, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = BorderColor)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "共 ${state.timeline.size} 条记录",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                    )
                                }
                            }
                        }
                    }

                    // Timeline
                    if (state.timeline.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("暂无验光或配镜记录", color = TextSecondary)
                            }
                        }
                    }

                    itemsIndexed(state.timeline, key = { _, item -> "${item.type}_${item.date}_${item.hashCode()}" }) { index, item ->
                        TimelineCard(
                            item = item,
                            index = index,
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
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    val refreshErrInteractionSource = remember { MutableInteractionSource() }
                    TextButton(
                        onClick = { viewModel.clearError() },
                        interactionSource = refreshErrInteractionSource,
                        modifier = Modifier.bounceClick(refreshErrInteractionSource)
                    ) {
                        Text("关闭", color = Primary)
                    }
                },
                containerColor = Color.White,
                contentColor = TextPrimary
            ) {
                Text(state.error!!)
            }
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
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("确认删除", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            },
            text = { Text("确定要删除这条记录吗？", color = TextSecondary) },
            confirmButton = {
                val delConfirmInteractionSource = remember { MutableInteractionSource() }
                Button(
                    onClick = { viewModel.deleteRecord() },
                    interactionSource = delConfirmInteractionSource,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Error),
                    modifier = Modifier.bounceClick(delConfirmInteractionSource)
                ) {
                    Text("删除", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                val delCancelInteractionSource = remember { MutableInteractionSource() }
                TextButton(
                    onClick = { viewModel.hideDeleteConfirm() },
                    interactionSource = delCancelInteractionSource,
                    modifier = Modifier.bounceClick(delCancelInteractionSource)
                ) {
                    Text("取消", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

@Composable
private fun TimelineCard(
    item: TimelineItem,
    index: Int,
    onEditOptometry: (OptometryRecord) -> Unit,
    onEditSales: (SalesRecord) -> Unit,
    onDelete: (String, Long) -> Unit
) {
    val isOptometry = item.type == "OPTOMETRY"
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .staggeredEntrance(index = index)
            .bounceClick { expanded = !expanded }
            .shadow(6.dp, RoundedCornerShape(18.dp), spotColor = CardShadow),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(if (isOptometry) PrimaryLight else Color(0xFFF3E8FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isOptometry) Icons.Default.Visibility else Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = if (isOptometry) Primary else Color(0xFF8B5CF6),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = item.title ?: if (isOptometry) "验光单" else "配镜单",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                }
                Text(
                    text = item.date ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            val dataMap = item.data as? Map<*, *> ?: emptyMap<String, Any>()
            val recordId = (dataMap["id"] as? Double)?.toLong()

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderColor)
                Spacer(modifier = Modifier.height(10.dp))

                if (isOptometry) {
                    val opt = parseOptometry(dataMap)
                    Column {
                        Row(modifier = Modifier.fillMaxWidth().background(PrimaryLight, RoundedCornerShape(8.dp)).padding(8.dp)) {
                            Text("眼别", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                            Text("球镜(SPH)", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                            Text("柱镜(CYL)", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                            Text("轴位(AXIS)", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                            Text("视力(VA)", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
                            Text("右 OD", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium, fontSize = 13.sp, color = TextPrimary)
                            Text(opt.odSph?.toString() ?: "-", modifier = Modifier.weight(1.2f), fontSize = 13.sp, color = TextSecondary)
                            Text(opt.odCyl?.toString() ?: "-", modifier = Modifier.weight(1.2f), fontSize = 13.sp, color = TextSecondary)
                            Text(opt.odAxis?.toString() ?: "-", modifier = Modifier.weight(1.2f), fontSize = 13.sp, color = TextSecondary)
                            Text(opt.odVa ?: "-", modifier = Modifier.weight(1f), fontSize = 13.sp, color = TextSecondary)
                        }
                        HorizontalDivider(color = BorderColor, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
                            Text("左 OS", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium, fontSize = 13.sp, color = TextPrimary)
                            Text(opt.osSph?.toString() ?: "-", modifier = Modifier.weight(1.2f), fontSize = 13.sp, color = TextSecondary)
                            Text(opt.osCyl?.toString() ?: "-", modifier = Modifier.weight(1.2f), fontSize = 13.sp, color = TextSecondary)
                            Text(opt.osAxis?.toString() ?: "-", modifier = Modifier.weight(1.2f), fontSize = 13.sp, color = TextSecondary)
                            Text(opt.osVa ?: "-", modifier = Modifier.weight(1f), fontSize = 13.sp, color = TextSecondary)
                        }

                        if (opt.pdFar != null || opt.addPower != null || !opt.optometristName.isNullOrBlank() || !opt.remark.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                                if (opt.pdFar != null) {
                                    Text("瞳距: ${opt.pdFar} mm", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(end = 16.dp))
                                }
                                if (opt.addPower != null) {
                                    Text("下加光: +${opt.addPower}", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(end = 16.dp))
                                }
                                if (!opt.optometristName.isNullOrBlank()) {
                                    Text("验光师: ${opt.optometristName}", fontSize = 12.sp, color = TextSecondary)
                                }
                            }
                            if (!opt.remark.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("备注: ${opt.remark}", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(horizontal = 8.dp))
                            }
                        }
                    }
                } else {
                    val sales = parseSales(dataMap)
                    Column {
                        if (!sales.frameBrand.isNullOrBlank() || !sales.frameModel.isNullOrBlank()) {
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("镜架: ${sales.frameBrand ?: ""} ${sales.frameModel ?: ""}", fontSize = 13.sp, color = TextPrimary)
                                Text(if (sales.framePrice != null) "¥${sales.framePrice}" else "-", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            }
                        }
                        if (!sales.lensBrand.isNullOrBlank() || !sales.lensParams.isNullOrBlank()) {
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("镜片: ${sales.lensBrand ?: ""} ${sales.lensParams ?: ""}", fontSize = 13.sp, color = TextPrimary)
                                Text(if (sales.lensPrice != null) "¥${sales.lensPrice}" else "-", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            }
                        }
                        if (!sales.remark.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("备注: ${sales.remark}", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(horizontal = 8.dp))
                        }
                        if (sales.totalAmount != null) {
                            HorizontalDivider(color = BorderColor, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("总计金额", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("¥${sales.totalAmount}", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Primary)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (recordId != null) {
                        val editInteractionSource = remember { MutableInteractionSource() }
                        IconButton(
                            onClick = {
                                if (isOptometry) {
                                    onEditOptometry(parseOptometry(dataMap))
                                } else {
                                    onEditSales(parseSales(dataMap))
                                }
                            },
                            interactionSource = editInteractionSource,
                            modifier = Modifier
                                .size(34.dp)
                                .background(PrimaryLight, CircleShape)
                                .bounceClick(editInteractionSource)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "编辑", tint = Primary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        val deleteInteractionSource = remember { MutableInteractionSource() }
                        IconButton(
                            onClick = { onDelete(item.type, recordId) },
                            interactionSource = deleteInteractionSource,
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFFFFF1F2), CircleShape)
                                .bounceClick(deleteInteractionSource)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "删除", tint = Error, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            } else {
                if (!item.subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1
                    )
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

@OptIn(ExperimentalMaterial3Api::class)
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

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedBorderColor = Primary,
        unfocusedBorderColor = BorderColor,
        focusedLabelColor = Primary,
        unfocusedLabelColor = TextSecondary,
        cursorColor = Primary
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("右眼 (OD)", style = MaterialTheme.typography.labelLarge, color = Primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    OutlinedTextField(value = odSph, onValueChange = { odSph = it }, label = { Text("SPH") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(4.dp))
                    OutlinedTextField(value = odCyl, onValueChange = { odCyl = it }, label = { Text("CYL") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(4.dp))
                    OutlinedTextField(value = odAxis, onValueChange = { odAxis = it }, label = { Text("AXIS") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(value = odVa, onValueChange = { odVa = it }, label = { Text("视力") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(12.dp))
                Text("左眼 (OS)", style = MaterialTheme.typography.labelLarge, color = Color(0xFF8B5CF6), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    OutlinedTextField(value = osSph, onValueChange = { osSph = it }, label = { Text("SPH") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(4.dp))
                    OutlinedTextField(value = osCyl, onValueChange = { osCyl = it }, label = { Text("CYL") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(4.dp))
                    OutlinedTextField(value = osAxis, onValueChange = { osAxis = it }, label = { Text("AXIS") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(value = osVa, onValueChange = { osVa = it }, label = { Text("视力") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    OutlinedTextField(value = pdFar, onValueChange = { pdFar = it }, label = { Text("瞳距") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(4.dp))
                    OutlinedTextField(value = addPower, onValueChange = { addPower = it }, label = { Text("下加光") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = optometrist, onValueChange = { optometrist = it }, label = { Text("验光师") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = remark, onValueChange = { remark = it }, label = { Text("备注") }, colors = fieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = {
            val confirmInteractionSource = remember { MutableInteractionSource() }
            Button(
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
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                interactionSource = confirmInteractionSource,
                modifier = Modifier.bounceClick(confirmInteractionSource)
            ) { Text("保存", color = Color.White, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            val cancelInteractionSource = remember { MutableInteractionSource() }
            TextButton(
                onClick = onDismiss,
                interactionSource = cancelInteractionSource,
                modifier = Modifier.bounceClick(cancelInteractionSource)
            ) { Text("取消", color = TextSecondary) }
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

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedBorderColor = Primary,
        unfocusedBorderColor = BorderColor,
        focusedLabelColor = Primary,
        unfocusedLabelColor = TextSecondary,
        cursorColor = Primary
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("镜架", style = MaterialTheme.typography.labelLarge, color = Primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(value = frameBrand, onValueChange = { frameBrand = it }, label = { Text("品牌") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(value = frameModel, onValueChange = { frameModel = it }, label = { Text("型号") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(value = framePrice, onValueChange = { framePrice = it }, label = { Text("售价(元)") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(12.dp))
                Text("镜片", style = MaterialTheme.typography.labelLarge, color = Color(0xFF8B5CF6), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(value = lensBrand, onValueChange = { lensBrand = it }, label = { Text("品牌") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(value = lensParams, onValueChange = { lensParams = it }, label = { Text("参数") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(value = lensPrice, onValueChange = { lensPrice = it }, label = { Text("售价(元)") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = remark, onValueChange = { remark = it }, label = { Text("备注") }, colors = fieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = {
            val confirmInteractionSource = remember { MutableInteractionSource() }
            Button(
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
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                interactionSource = confirmInteractionSource,
                modifier = Modifier.bounceClick(confirmInteractionSource)
            ) { Text("保存", color = Color.White, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            val cancelInteractionSource = remember { MutableInteractionSource() }
            TextButton(
                onClick = onDismiss,
                interactionSource = cancelInteractionSource,
                modifier = Modifier.bounceClick(cancelInteractionSource)
            ) { Text("取消", color = TextSecondary, fontWeight = FontWeight.SemiBold) }
        }
    )
}
