package com.glasses.app.ui.archive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import java.math.RoundingMode

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimelineCard(
    item: TimelineItem,
    index: Int,
    onEditOptometry: (OptometryRecord) -> Unit,
    onEditSales: (SalesRecord) -> Unit,
    onDelete: (String, Long) -> Unit
) {
    val isOptometry = item.type == "OPTOMETRY"
    var showDetail by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .staggeredEntrance(index = index)
            .bounceClick { showDetail = true }
            .shadow(6.dp, RoundedCornerShape(18.dp), spotColor = CardShadow),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(if (isOptometry) PrimaryLight else PurpleLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isOptometry) Icons.Default.Visibility else Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = if (isOptometry) Primary else Purple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.title ?: if (isOptometry) "验光单" else "配镜单",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                        if (!item.subtitle.isNullOrBlank()) {
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.date ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(18.dp))
            }
        }

        // ── 数据预览 ──
        val dataMap = item.data as? Map<*, *> ?: emptyMap<String, Any>()
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(8.dp))
        if (isOptometry) {
            val opt = parseOptometry(dataMap)
            Row(modifier = Modifier.fillMaxWidth()) {
                PreviewField("右 OD", formatOptometryValue(opt.odSph, opt.odCyl), Modifier.weight(1f))
                Spacer(modifier = Modifier.width(12.dp))
                PreviewField("左 OS", formatOptometryValue(opt.osSph, opt.osCyl), Modifier.weight(1f))
            }
        } else {
            val sales = parseSales(dataMap)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    if (!sales.frameBrand.isNullOrBlank()) {
                        Text("镜架: ${sales.frameBrand} ${sales.frameModel ?: ""}", fontSize = 12.sp, color = TextSecondary)
                    }
                    if (!sales.lensBrand.isNullOrBlank()) {
                        Text("镜片: ${sales.lensBrand} ${sales.lensParams ?: ""}", fontSize = 12.sp, color = TextSecondary)
                    }
                }
                if (sales.totalAmount != null) {
                    Text(
                        "¥${sales.totalAmount.setScale(2, java.math.RoundingMode.HALF_UP)}",
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    // ── 详情弹出面板 ──
    if (showDetail) {
        val dataMap = item.data as? Map<*, *> ?: emptyMap<String, Any>()
        val recordId = (dataMap["id"] as? Double)?.toLong()

        ModalBottomSheet(
            onDismissRequest = { showDetail = false },
            containerColor = Surface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = BorderColor) }
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 标题
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (isOptometry) PrimaryLight else PurpleLight, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isOptometry) Icons.Default.Visibility else Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = if (isOptometry) Primary else Purple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            item.title ?: if (isOptometry) "验光单" else "配镜单",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 20.sp
                        )
                        Text(item.date ?: "", fontSize = 13.sp, color = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isOptometry) {
                    val opt = parseOptometry(dataMap)

                    // 右眼
                    Surface(shape = RoundedCornerShape(14.dp), color = Background, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            SectionHeader(icon = Icons.Default.Visibility, label = "右眼 (OD)", tint = Primary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                DetailField("球镜 SPH", fmtOptometry(opt.odSph), Modifier.weight(1f))
                                DetailField("柱镜 CYL", fmtOptometry(opt.odCyl), Modifier.weight(1f))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                DetailField("轴位 AXIS", opt.odAxis?.toString(), Modifier.weight(1f))
                                DetailField("视力 VA", opt.odVa, Modifier.weight(1f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 左眼
                    Surface(shape = RoundedCornerShape(14.dp), color = Background, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            SectionHeader(icon = Icons.Default.Visibility, label = "左眼 (OS)", tint = Purple)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                DetailField("球镜 SPH", fmtOptometry(opt.osSph), Modifier.weight(1f))
                                DetailField("柱镜 CYL", fmtOptometry(opt.osCyl), Modifier.weight(1f))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                DetailField("轴位 AXIS", opt.osAxis?.toString(), Modifier.weight(1f))
                                DetailField("视力 VA", opt.osVa, Modifier.weight(1f))
                            }
                        }
                    }

                    // 附加信息
                    if (opt.odPd != null || opt.osPd != null || opt.pdFar != null || opt.pdNear != null || opt.addPower != null || !opt.optometristName.isNullOrBlank() || !opt.remark.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(shape = RoundedCornerShape(14.dp), color = Background, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                SectionHeader(icon = Icons.Default.Tune, label = "附加信息", tint = TextSecondary)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    DetailField("右眼瞳距", opt.odPd?.let { "${fmtPlain(it)} mm" }, Modifier.weight(1f))
                                    DetailField("左眼瞳距", opt.osPd?.let { "${fmtPlain(it)} mm" }, Modifier.weight(1f))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    DetailField("远用瞳距", opt.pdFar?.let { "${fmtPlain(it)} mm" }, Modifier.weight(1f))
                                    DetailField("近用瞳距", opt.pdNear?.let { "${fmtPlain(it)} mm" }, Modifier.weight(1f))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    DetailField("下加光", opt.addPower?.let { fmtOptometry(it) }, Modifier.weight(1f))
                                    DetailField("验光师", opt.optometristName ?: "-", Modifier.weight(1f))
                                }
                                if (!opt.remark.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    DetailField("备注", opt.remark, Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                } else {
                    val sales = parseSales(dataMap)

                    // 镜架
                    Surface(shape = RoundedCornerShape(14.dp), color = Background, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            SectionHeader(icon = Icons.Default.Style, label = "镜架", tint = Primary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                DetailField("品牌", sales.frameBrand, Modifier.weight(1f))
                                DetailField("型号", sales.frameModel, Modifier.weight(1f))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                DetailField("数量", sales.frameQuantity?.toString(), Modifier.weight(1f))
                                DetailField("零售单价", sales.frameRetailPrice?.let { "¥$it" }, Modifier.weight(1f))
                                DetailField("实际售价", sales.framePrice?.let { "¥$it" }, Modifier.weight(1f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 镜片
                    Surface(shape = RoundedCornerShape(14.dp), color = Background, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            SectionHeader(icon = Icons.Default.Lens, label = "镜片", tint = Purple)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                DetailField("品牌", sales.lensBrand, Modifier.weight(1f))
                                DetailField("参数", sales.lensParams, Modifier.weight(1f))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                DetailField("数量", sales.lensQuantity?.toString(), Modifier.weight(1f))
                                DetailField("零售单价", sales.lensRetailPrice?.let { "¥$it" }, Modifier.weight(1f))
                                DetailField("实际售价", sales.lensPrice?.let { "¥$it" }, Modifier.weight(1f))
                            }
                        }
                    }

                    // 金额
                    if (sales.totalRetailPrice != null || sales.totalAmount != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(shape = RoundedCornerShape(14.dp), color = Background, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                SectionHeader(icon = Icons.Default.AttachMoney, label = "金额", tint = TextSecondary)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    DetailField("零售总价", sales.totalRetailPrice?.let { "¥${it.setScale(2, RoundingMode.HALF_UP)}" }, Modifier.weight(1f))
                                    DetailField("实收总价", sales.totalAmount?.let { "¥${it.setScale(2, RoundingMode.HALF_UP)}" }, Modifier.weight(1f))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                val discountText = if (sales.totalRetailPrice != null && sales.totalAmount != null && sales.totalRetailPrice.signum() > 0) {
                                    val d = sales.totalAmount.divide(sales.totalRetailPrice, 4, RoundingMode.HALF_UP)
                                        .multiply(BigDecimal.TEN).setScale(1, RoundingMode.HALF_UP)
                                    "${d}折"
                                } else "-"
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    Text("折扣: ", fontSize = 13.sp, color = TextSecondary)
                                    Text(discountText, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Primary)
                                }
                            }
                        }
                    }

                    if (!sales.remark.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(shape = RoundedCornerShape(14.dp), color = Background, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                DetailField("备注", sales.remark, Modifier.fillMaxWidth())
                            }
                        }
                    }
                }

                // 操作按钮
                if (recordId != null) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        val editInteractionSource = remember { MutableInteractionSource() }
                        OutlinedButton(
                            onClick = {
                                showDetail = false
                                if (isOptometry) onEditOptometry(parseOptometry(dataMap)) else onEditSales(parseSales(dataMap))
                            },
                            interactionSource = editInteractionSource,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Primary),
                            modifier = Modifier.weight(1f).bounceClick(editInteractionSource)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("编辑", color = Primary, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        val deleteInteractionSource = remember { MutableInteractionSource() }
                        OutlinedButton(
                            onClick = {
                                showDetail = false
                                onDelete(item.type, recordId)
                            },
                            interactionSource = deleteInteractionSource,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Error),
                            modifier = Modifier.weight(1f).bounceClick(deleteInteractionSource)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Error, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("删除", color = Error, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
private fun PreviewField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, fontSize = 11.sp, color = TextTertiary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

private fun formatOptometryValue(sph: BigDecimal?, cyl: BigDecimal?): String {
    if (sph == null && cyl == null) return "-"
    val s = fmtOptometry(sph)
    val c = fmtOptometry(cyl)
    return "$s / $c"
}

private fun fmtOptometry(value: BigDecimal?): String {
    if (value == null) return "-"
    val formatted = value.setScale(2, RoundingMode.HALF_UP).toPlainString()
    return if (value.signum() > 0) "+$formatted" else formatted
}

private fun fmtPlain(value: BigDecimal?): String {
    if (value == null) return "-"
    return value.setScale(2, RoundingMode.HALF_UP).toPlainString()
}

@Composable
private fun DetailField(label: String?, value: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 2.dp)) {
        if (label != null) {
            Text(label, fontSize = 11.sp, color = TextTertiary)
        }
        Text(value ?: "-", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
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
    var odPd by remember { mutableStateOf(record?.odPd?.toString() ?: "") }
    var osPd by remember { mutableStateOf(record?.osPd?.toString() ?: "") }
    var pdFar by remember { mutableStateOf(record?.pdFar?.toString() ?: "") }
    var pdNear by remember { mutableStateOf(record?.pdNear?.toString() ?: "") }
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
        cursorColor = Primary,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent
    )

    val sectionShape = RoundedCornerShape(14.dp)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = BorderColor) }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ── 标题 ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(PrimaryLight, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 右眼 ──
            Surface(shape = sectionShape, color = Background, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SectionHeader(icon = Icons.Default.Visibility, label = "右眼 (OD)", tint = Primary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row {
                        SmallField(odSph, { odSph = it }, "球镜 SPH", fieldColors, Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(6.dp))
                        SmallField(odCyl, { odCyl = it }, "柱镜 CYL", fieldColors, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row {
                        SmallField(odAxis, { odAxis = it }, "轴位 AXIS", fieldColors, Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(6.dp))
                        SmallField(odVa, { odVa = it }, "视力 VA", fieldColors, Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── 左眼 ──
            Surface(shape = sectionShape, color = Background, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SectionHeader(icon = Icons.Default.Visibility, label = "左眼 (OS)", tint = Purple)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row {
                        SmallField(osSph, { osSph = it }, "球镜 SPH", fieldColors, Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(6.dp))
                        SmallField(osCyl, { osCyl = it }, "柱镜 CYL", fieldColors, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row {
                        SmallField(osAxis, { osAxis = it }, "轴位 AXIS", fieldColors, Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(6.dp))
                        SmallField(osVa, { osVa = it }, "视力 VA", fieldColors, Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── 瞳距 ──
            Surface(shape = sectionShape, color = Background, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SectionHeader(icon = Icons.Default.Tune, label = "瞳距", tint = TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row {
                        SmallField(odPd, {
                            odPd = it
                            val l = it.toBigDecimalOrNull()
                            val r = osPd.toBigDecimalOrNull()
                            if (l != null && r != null) pdFar = l.add(r).toPlainString()
                        }, "右眼瞳距", fieldColors, Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(6.dp))
                        SmallField(osPd, {
                            osPd = it
                            val l = odPd.toBigDecimalOrNull()
                            val r = it.toBigDecimalOrNull()
                            if (l != null && r != null) pdFar = l.add(r).toPlainString()
                        }, "左眼瞳距", fieldColors, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row {
                        OutlinedTextField(value = pdFar, onValueChange = { pdFar = it }, label = { Text("远用瞳距") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedTextField(value = pdNear, onValueChange = { pdNear = it }, label = { Text("近用瞳距") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── 附加参数 ──
            Surface(shape = sectionShape, color = Background, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SectionHeader(icon = Icons.Default.Tune, label = "附加参数", tint = TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = addPower, onValueChange = { addPower = it }, label = { Text("下加光") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = optometrist, onValueChange = { optometrist = it }, label = { Text("验光师") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = remark, onValueChange = { remark = it }, label = { Text("备注") }, colors = fieldColors, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), minLines = 2)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 操作按钮 ──
            Row(modifier = Modifier.fillMaxWidth()) {
                val cancelInteractionSource = remember { MutableInteractionSource() }
                OutlinedButton(
                    onClick = onDismiss,
                    interactionSource = cancelInteractionSource,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.weight(1f).bounceClick(cancelInteractionSource)
                ) { Text("取消", color = TextSecondary, fontWeight = FontWeight.SemiBold) }
                Spacer(modifier = Modifier.width(12.dp))
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
                                odPd = odPd.toBigDecimalOrNull(),
                                osPd = osPd.toBigDecimalOrNull(),
                                pdFar = pdFar.toBigDecimalOrNull(),
                                pdNear = pdNear.toBigDecimalOrNull(),
                                addPower = addPower.toBigDecimalOrNull(),
                                optometristName = optometrist.ifBlank { null },
                                remark = remark.ifBlank { null }
                            )
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    interactionSource = confirmInteractionSource,
                    modifier = Modifier.weight(1f).bounceClick(confirmInteractionSource)
                ) { Text("保存", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SalesDialog(
    title: String,
    record: SalesRecord? = null,
    onDismiss: () -> Unit,
    onConfirm: (SalesRecord) -> Unit
) {
    var frameBrand by remember { mutableStateOf(record?.frameBrand ?: "") }
    var frameModel by remember { mutableStateOf(record?.frameModel ?: "") }
    var frameQuantity by remember { mutableStateOf(record?.frameQuantity?.toString() ?: "1") }
    var frameRetailPrice by remember { mutableStateOf(record?.frameRetailPrice?.toString() ?: "") }
    var framePrice by remember { mutableStateOf(record?.framePrice?.toString() ?: "") }
    var lensBrand by remember { mutableStateOf(record?.lensBrand ?: "") }
    var lensParams by remember { mutableStateOf(record?.lensParams ?: "") }
    var lensQuantity by remember { mutableStateOf(record?.lensQuantity?.toString() ?: "1") }
    var lensRetailPrice by remember { mutableStateOf(record?.lensRetailPrice?.toString() ?: "") }
    var lensPrice by remember { mutableStateOf(record?.lensPrice?.toString() ?: "") }
    var totalRetailPrice by remember { mutableStateOf(record?.totalRetailPrice?.toString() ?: "") }
    var totalAmount by remember { mutableStateOf(record?.totalAmount?.toString() ?: "") }
    var remark by remember { mutableStateOf(record?.remark ?: "") }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedBorderColor = Primary,
        unfocusedBorderColor = BorderColor,
        focusedLabelColor = Primary,
        unfocusedLabelColor = TextSecondary,
        cursorColor = Primary,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent
    )

    val sectionShape = RoundedCornerShape(14.dp)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = BorderColor) }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ── 标题 ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(PurpleLight, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Purple, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 镜架 ──
            Surface(shape = sectionShape, color = Background, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SectionHeader(icon = Icons.Default.Style, label = "镜架", tint = Primary)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = frameBrand, onValueChange = { frameBrand = it }, label = { Text("品牌") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    Row {
                        OutlinedTextField(value = frameModel, onValueChange = { frameModel = it }, label = { Text("型号") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedTextField(value = frameQuantity, onValueChange = { frameQuantity = it }, label = { Text("数量") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(0.7f))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row {
                        OutlinedTextField(value = frameRetailPrice, onValueChange = { frameRetailPrice = it }, label = { Text("零售单价") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedTextField(value = framePrice, onValueChange = { framePrice = it }, label = { Text("实际售价") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── 镜片 ──
            Surface(shape = sectionShape, color = Background, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SectionHeader(icon = Icons.Default.Lens, label = "镜片", tint = Purple)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row {
                        OutlinedTextField(value = lensBrand, onValueChange = { lensBrand = it }, label = { Text("品牌") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedTextField(value = lensParams, onValueChange = { lensParams = it }, label = { Text("参数") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row {
                        OutlinedTextField(value = lensQuantity, onValueChange = { lensQuantity = it }, label = { Text("数量") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(0.7f))
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedTextField(value = lensRetailPrice, onValueChange = { lensRetailPrice = it }, label = { Text("零售单价") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedTextField(value = lensPrice, onValueChange = { lensPrice = it }, label = { Text("实际售价") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── 金额 ──
            Surface(shape = sectionShape, color = Background, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SectionHeader(icon = Icons.Default.AttachMoney, label = "金额", tint = TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row {
                        OutlinedTextField(value = totalRetailPrice, onValueChange = { totalRetailPrice = it }, label = { Text("零售总价") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedTextField(value = totalAmount, onValueChange = { totalAmount = it }, label = { Text("实收总价") }, singleLine = true, colors = fieldColors, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f))
                    }
                    // 折扣显示
                    val retailVal = totalRetailPrice.toBigDecimalOrNull()
                    val actualVal = totalAmount.toBigDecimalOrNull()
                    val discountText = if (retailVal != null && actualVal != null && retailVal.signum() > 0) {
                        val d = actualVal.divide(retailVal, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.TEN)
                            .setScale(1, RoundingMode.HALF_UP)
                        "${d}折"
                    } else "-"
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("折扣: ", fontSize = 13.sp, color = TextSecondary)
                        Text(discountText, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── 备注 ──
            Surface(shape = sectionShape, color = Background, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(value = remark, onValueChange = { remark = it }, label = { Text("备注") }, colors = fieldColors, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), minLines = 2)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 操作按钮 ──
            Row(modifier = Modifier.fillMaxWidth()) {
                val cancelInteractionSource = remember { MutableInteractionSource() }
                OutlinedButton(
                    onClick = onDismiss,
                    interactionSource = cancelInteractionSource,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.weight(1f).bounceClick(cancelInteractionSource)
                ) { Text("取消", color = TextSecondary, fontWeight = FontWeight.SemiBold) }
                Spacer(modifier = Modifier.width(12.dp))
                val confirmInteractionSource = remember { MutableInteractionSource() }
                Button(
                    onClick = {
                        val fq = frameQuantity.toIntOrNull() ?: 1
                        val fp = framePrice.toBigDecimalOrNull()
                        val lq = lensQuantity.toIntOrNull() ?: 1
                        val lp = lensPrice.toBigDecimalOrNull()
                        val frp = frameRetailPrice.toBigDecimalOrNull()
                        val lrp = lensRetailPrice.toBigDecimalOrNull()
                        val computedTotalRetail = if (frp != null && lrp != null) {
                            frp.multiply(BigDecimal.valueOf(fq.toLong()))
                                .add(lrp.multiply(BigDecimal.valueOf(lq.toLong())))
                        } else frp?.multiply(BigDecimal.valueOf(fq.toLong()))
                            ?: lrp?.multiply(BigDecimal.valueOf(lq.toLong()))
                        val computedTotal = if (fp != null && lp != null) {
                            fp.multiply(BigDecimal.valueOf(fq.toLong()))
                                .add(lp.multiply(BigDecimal.valueOf(lq.toLong())))
                        } else fp?.multiply(BigDecimal.valueOf(fq.toLong()))
                            ?: lp?.multiply(BigDecimal.valueOf(lq.toLong()))
                        onConfirm(
                            SalesRecord(
                                id = record?.id,
                                customerId = record?.customerId,
                                optometryId = record?.optometryId,
                                frameBrand = frameBrand.ifBlank { null },
                                frameModel = frameModel.ifBlank { null },
                                frameQuantity = fq,
                                frameRetailPrice = frp,
                                framePrice = fp,
                                lensBrand = lensBrand.ifBlank { null },
                                lensParams = lensParams.ifBlank { null },
                                lensQuantity = lq,
                                lensRetailPrice = lrp,
                                lensPrice = lp,
                                totalRetailPrice = totalRetailPrice.toBigDecimalOrNull() ?: computedTotalRetail,
                                totalAmount = totalAmount.toBigDecimalOrNull() ?: computedTotal,
                                remark = remark.ifBlank { null }
                            )
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    interactionSource = confirmInteractionSource,
                    modifier = Modifier.weight(1f).bounceClick(confirmInteractionSource)
                ) { Text("保存", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, label: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(tint.copy(alpha = 0.1f), RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontWeight = FontWeight.Bold, color = tint, fontSize = 14.sp)
    }
}

@Composable
private fun SmallField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    colors: TextFieldColors,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        singleLine = true,
        colors = colors,
        shape = RoundedCornerShape(12.dp),
        textStyle = MaterialTheme.typography.bodyMedium,
        modifier = modifier
    )
}
