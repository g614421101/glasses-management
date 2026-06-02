package com.glasses.app.ui.customer

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.glasses.app.data.model.Customer
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.glasses.app.theme.*
import com.glasses.app.ui.common.bounceClick
import com.glasses.app.ui.common.staggeredEntrance
import com.glasses.app.viewmodel.CustomerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(
    modifier: Modifier = Modifier,
    viewModel: CustomerViewModel = hiltViewModel(),
    onNavigateToArchive: (Long) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopAppBar(
            title = { Text("顾客管理", fontWeight = FontWeight.Bold, color = TextPrimary) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White,
                titleContentColor = TextPrimary,
                actionIconContentColor = TextSecondary
            ),
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.searchKeyword,
                onValueChange = { viewModel.onSearchChange(it) },
                placeholder = { Text("搜索姓名或手机号", color = TextTertiary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Primary) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = BorderColor,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
                modifier = Modifier
                    .weight(1f)
                    .shadow(2.dp, RoundedCornerShape(24.dp), spotColor = CardShadow),
                trailingIcon = {
                    if (state.searchKeyword.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchChange(""); viewModel.onSearch() }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除", tint = TextSecondary)
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            val searchInteractionSource = remember { MutableInteractionSource() }
            IconButton(
                onClick = { viewModel.onSearch() },
                interactionSource = searchInteractionSource,
                modifier = Modifier
                    .size(44.dp)
                    .shadow(2.dp, CircleShape, spotColor = CardShadow)
                    .background(PrimaryLight, CircleShape)
                    .bounceClick(searchInteractionSource)
            ) {
                Icon(Icons.Default.Search, contentDescription = "搜索", tint = Primary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            val addInteractionSource = remember { MutableInteractionSource() }
            IconButton(
                onClick = { viewModel.showAddDialog() },
                interactionSource = addInteractionSource,
                modifier = Modifier
                    .size(44.dp)
                    .shadow(4.dp, CircleShape, spotColor = Primary.copy(alpha = 0.4f))
                    .background(
                        brush = Brush.linearGradient(colors = listOf(Primary, SkyBlue)),
                        shape = CircleShape
                    )
                    .bounceClick(addInteractionSource)
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加顾客", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        Text(
            text = "共 ${state.totalRecords} 位顾客",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        when {
            state.isLoading && state.customers.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            state.customers.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无顾客数据", color = TextSecondary)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(state.customers, key = { _, item -> item.id!! }) { index, customer ->
                        CustomerCard(
                            customer = customer,
                            index = index,
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
                    val prevInteractionSource = remember { MutableInteractionSource() }
                    TextButton(
                        onClick = { viewModel.onPrevPage() },
                        enabled = state.currentPage > 1,
                        interactionSource = prevInteractionSource,
                        modifier = Modifier.bounceClick(prevInteractionSource)
                    ) {
                        Text("上一页", color = if (state.currentPage > 1) Primary else TextSecondary.copy(alpha = 0.5f))
                    }
                    Text(
                        text = "${state.currentPage} / ${state.totalPages}",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    val nextInteractionSource = remember { MutableInteractionSource() }
                    TextButton(
                        onClick = { viewModel.onNextPage() },
                        enabled = state.currentPage < state.totalPages,
                        interactionSource = nextInteractionSource,
                        modifier = Modifier.bounceClick(nextInteractionSource)
                    ) {
                        Text("下一页", color = if (state.currentPage < state.totalPages) Primary else TextSecondary.copy(alpha = 0.5f))
                    }
                }
            }
        }

        if (state.error != null) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    val errCloseInteractionSource = remember { MutableInteractionSource() }
                    TextButton(
                        onClick = { viewModel.clearError() },
                        interactionSource = errCloseInteractionSource,
                        modifier = Modifier.bounceClick(errCloseInteractionSource)
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
            text = { Text("确定要删除顾客「${state.deletingCustomer!!.name}」吗？\n删除后可前往回收站恢复。", color = TextSecondary) },
            confirmButton = {
                val delConfirmInteractionSource = remember { MutableInteractionSource() }
                Button(
                    onClick = { viewModel.deleteCustomer() },
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
private fun CustomerCard(
    customer: Customer,
    index: Int,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .staggeredEntrance(index = index)
            .bounceClick(onClick = onClick)
            .shadow(6.dp, RoundedCornerShape(18.dp), spotColor = CardShadow),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (!customer.phone.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = customer.phone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val genderText = when (customer.gender) {
                        1 -> "男"
                        2 -> "女"
                        else -> ""
                    }

                    if (genderText.isNotEmpty()) {
                        val chipBg = if (customer.gender == 1) MaleBadgeBg else FemaleBadgeBg
                        val chipText = if (customer.gender == 1) MaleBadge else FemaleBadge
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(chipBg, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = genderText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = chipText
                            )
                        }
                    }
                    val editInteractionSource = remember { MutableInteractionSource() }
                    IconButton(
                        onClick = onEdit,
                        interactionSource = editInteractionSource,
                        modifier = Modifier
                            .size(34.dp)
                            .background(PrimaryLight, CircleShape)
                            .bounceClick(editInteractionSource)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑",
                            modifier = Modifier.size(16.dp),
                            tint = Primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    val deleteInteractionSource = remember { MutableInteractionSource() }
                    IconButton(
                        onClick = onDelete,
                        interactionSource = deleteInteractionSource,
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFFFFF1F2), CircleShape)
                            .bounceClick(deleteInteractionSource)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            modifier = Modifier.size(16.dp),
                            tint = Error
                        )
                    }
                }
            }

            if (!customer.birthday.isNullOrBlank() || !customer.remark.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                if (!customer.birthday.isNullOrBlank()) {
                    Text(
                        text = "🎂 生日: ${customer.birthday}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                if (!customer.remark.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📝 ${customer.remark}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }

            if (!customer.createTime.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = BorderColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "注册时间: ${customer.createTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
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
                    imageVector = if (customer == null) Icons.Default.PersonAdd else Icons.Default.Edit,
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
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("姓名 *") },
                    singleLine = true,
                    colors = fieldColors,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("手机号") },
                    singleLine = true,
                    colors = fieldColors,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))

                Text("性别", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    FilterChip(
                        selected = gender == 0,
                        onClick = { gender = 0 },
                        label = { Text("未知") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryLight,
                            selectedLabelColor = Primary,
                            labelColor = TextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = gender == 1,
                        onClick = { gender = 1 },
                        label = { Text("男") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaleBadgeBg,
                            selectedLabelColor = MaleBadge,
                            labelColor = TextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = gender == 2,
                        onClick = { gender = 2 },
                        label = { Text("女") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FemaleBadgeBg,
                            selectedLabelColor = FemaleBadge,
                            labelColor = TextSecondary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = birthday,
                    onValueChange = { birthday = it },
                    label = { Text("生日（yyyy-MM-dd）") },
                    singleLine = true,
                    colors = fieldColors,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("备注") },
                    colors = fieldColors,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            val confirmInteractionSource = remember { MutableInteractionSource() }
            Button(
                onClick = {
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
                },
                enabled = name.isNotBlank(),
                interactionSource = confirmInteractionSource,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = if (name.isNotBlank()) Modifier.bounceClick(confirmInteractionSource) else Modifier
            ) {
                Text("确定", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            val cancelInteractionSource = remember { MutableInteractionSource() }
            TextButton(
                onClick = onDismiss,
                interactionSource = cancelInteractionSource,
                modifier = Modifier.bounceClick(cancelInteractionSource)
            ) {
                Text("取消", color = TextSecondary, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
