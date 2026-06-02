package com.glasses.app.ui.recyclebin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.glasses.app.theme.*
import com.glasses.app.ui.common.bounceClick
import com.glasses.app.ui.common.staggeredEntrance
import com.glasses.app.viewmodel.RecycleBinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleBinScreen(
    onBack: () -> Unit,
    viewModel: RecycleBinViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = PrimaryLight,
        selectedLabelColor = Primary,
        labelColor = TextSecondary,
        containerColor = Color.White
    )

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("回收站", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    val refreshInteractionSource = remember { MutableInteractionSource() }
                    IconButton(
                        onClick = { viewModel.loadRecycleBin() },
                        interactionSource = refreshInteractionSource,
                        modifier = Modifier.bounceClick(refreshInteractionSource)
                    ) {
                        Icon(Icons.Default.Refresh, "刷新")
                    }
                    val emptySweepInteractionSource = remember { MutableInteractionSource() }
                    IconButton(
                        onClick = { viewModel.showEmptyConfirm() },
                        interactionSource = emptySweepInteractionSource,
                        modifier = Modifier.bounceClick(emptySweepInteractionSource)
                    ) {
                        Icon(Icons.Default.DeleteSweep, "清空", tint = Error)
                    }
                },
                modifier = Modifier.shadow(4.dp, spotColor = CardShadow)
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.filterType == "all",
                    onClick = { viewModel.onFilterChange("all") },
                    label = { Text("全部") },
                    colors = chipColors
                )
                FilterChip(
                    selected = state.filterType == "customer",
                    onClick = { viewModel.onFilterChange("customer") },
                    label = { Text("顾客") },
                    colors = chipColors
                )
                FilterChip(
                    selected = state.filterType == "optometry",
                    onClick = { viewModel.onFilterChange("optometry") },
                    label = { Text("验光") },
                    colors = chipColors
                )
                FilterChip(
                    selected = state.filterType == "sales",
                    onClick = { viewModel.onFilterChange("sales") },
                    label = { Text("配镜") },
                    colors = chipColors
                )
            }

            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                state.data.customers.isEmpty() && state.data.optometryRecords.isEmpty() && state.data.salesRecords.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("回收站为空", color = TextSecondary)
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        var index = 0
                        if (state.filterType == "all" || state.filterType == "customer") {
                            itemsIndexed(state.data.customers, key = { _, it -> "c_${it.id}" }) { _, c ->
                                RecycleItemCard(
                                    title = c.name,
                                    subtitle = c.phone ?: "",
                                    type = "顾客",
                                    index = index++,
                                    onRestore = { viewModel.restore("customer", c.id!!) },
                                    onPurge = { viewModel.showPurgeConfirm("customer", c.id!!) }
                                )
                            }
                        }
                        if (state.filterType == "all" || state.filterType == "optometry") {
                            itemsIndexed(state.data.optometryRecords, key = { _, it -> "o_${it.id}" }) { _, r ->
                                RecycleItemCard(
                                    title = r.customerName ?: "验光记录",
                                    subtitle = r.optometristName ?: "",
                                    type = "验光",
                                    index = index++,
                                    onRestore = { viewModel.restore("optometry", r.id!!) },
                                    onPurge = { viewModel.showPurgeConfirm("optometry", r.id!!) }
                                )
                            }
                        }
                        if (state.filterType == "all" || state.filterType == "sales") {
                            itemsIndexed(state.data.salesRecords, key = { _, it -> "s_${it.id}" }) { _, r ->
                                RecycleItemCard(
                                    title = r.customerName ?: "配镜记录",
                                    subtitle = "¥${r.totalAmount ?: 0}",
                                    type = "配镜",
                                    index = index++,
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
                    Text("彻底删除", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            },
            text = { Text("此操作不可恢复，确定要彻底删除吗？", color = TextSecondary) },
            confirmButton = {
                val purgeConfirmInteractionSource = remember { MutableInteractionSource() }
                Button(
                    onClick = { viewModel.purge() },
                    interactionSource = purgeConfirmInteractionSource,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Error),
                    modifier = Modifier.bounceClick(purgeConfirmInteractionSource)
                ) {
                    Text("彻底删除", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                val purgeCancelInteractionSource = remember { MutableInteractionSource() }
                TextButton(
                    onClick = { viewModel.hidePurgeConfirm() },
                    interactionSource = purgeCancelInteractionSource,
                    modifier = Modifier.bounceClick(purgeCancelInteractionSource)
                ) {
                    Text("取消", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    if (state.showEmptyConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.hideEmptyConfirm() },
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
                    Text("清空回收站", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            },
            text = { Text("确定要彻底删除回收站中的所有数据吗？\n此操作不可恢复。", color = TextSecondary) },
            confirmButton = {
                val emptyConfirmInteractionSource = remember { MutableInteractionSource() }
                Button(
                    onClick = { viewModel.emptyRecycleBin() },
                    interactionSource = emptyConfirmInteractionSource,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Error),
                    modifier = Modifier.bounceClick(emptyConfirmInteractionSource)
                ) {
                    Text("清空", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                val emptyCancelInteractionSource = remember { MutableInteractionSource() }
                TextButton(
                    onClick = { viewModel.hideEmptyConfirm() },
                    interactionSource = emptyCancelInteractionSource,
                    modifier = Modifier.bounceClick(emptyCancelInteractionSource)
                ) {
                    Text("取消", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

@Composable
private fun RecycleItemCard(
    title: String,
    subtitle: String,
    type: String,
    index: Int,
    onRestore: () -> Unit,
    onPurge: () -> Unit
) {
    val badgeBg = when (type) {
        "顾客" -> PrimaryLight
        "验光" -> Color(0xFFF3E8FF)
        else -> Color(0xFFEFF6FF)
    }
    val badgeText = when (type) {
        "顾客" -> Primary
        "验光" -> Color(0xFF8B5CF6)
        else -> Primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .staggeredEntrance(index = index)
            .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = CardShadow),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(subtitle, fontSize = 12.sp, color = TextSecondary)
                }
            }
            Box(
                modifier = Modifier
                    .background(badgeBg, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(type, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = badgeText)
            }
            Spacer(Modifier.width(10.dp))
            val restoreInteractionSource = remember { MutableInteractionSource() }
            TextButton(
                onClick = onRestore,
                interactionSource = restoreInteractionSource,
                modifier = Modifier.bounceClick(restoreInteractionSource)
            ) {
                Text("恢复", color = Primary)
            }
            Spacer(Modifier.width(4.dp))
            val purgeInteractionSource = remember { MutableInteractionSource() }
            TextButton(
                onClick = onPurge,
                interactionSource = purgeInteractionSource,
                modifier = Modifier.bounceClick(purgeInteractionSource)
            ) {
                Text("删除", color = Error)
            }
        }
    }
}
