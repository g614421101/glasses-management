package com.glasses.app.ui.stats

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.hilt.navigation.compose.hiltViewModel
import com.glasses.app.theme.*
import com.glasses.app.ui.common.bounceClick
import com.glasses.app.ui.common.staggeredEntrance
import com.glasses.app.viewmodel.StatsViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
    onNavigateToArchive: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopAppBar(
            title = { Text("营收统计", fontWeight = FontWeight.Bold, color = TextPrimary) },
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

        // Filters
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = state.showAll,
                    onCheckedChange = { viewModel.onShowAllChange(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Primary,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = BorderColor
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("查看全部", fontWeight = FontWeight.SemiBold, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
            }

            if (!state.showAll) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DatePickerField(
                        label = "起始日期",
                        date = state.startDate,
                        onDateSelected = { dateString ->
                            viewModel.onDateChange(dateString, state.endDate)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    DatePickerField(
                        label = "截止日期",
                        date = state.endDate,
                        onDateSelected = { dateString ->
                            viewModel.onDateChange(state.startDate, dateString)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Summary cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard(
                title = "总营收",
                value = "¥${state.totalRevenue.setScale(2, RoundingMode.HALF_UP)}",
                isHighlight = true,
                modifier = Modifier.weight(1.2f)
            )
            SummaryCard(
                title = "订单数",
                value = "${state.orderCount}",
                isHighlight = false,
                modifier = Modifier.weight(0.9f)
            )
            SummaryCard(
                title = "客单价",
                value = if (state.orderCount > 0) {
                    "¥${state.totalRevenue.divide(BigDecimal.valueOf(state.orderCount), 2, RoundingMode.HALF_UP)}"
                } else "¥0.00",
                isHighlight = false,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content
        when {
            state.isLoading && state.records.isEmpty() -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            state.records.isEmpty() -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("暂无销售记录", color = TextSecondary)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(state.records, key = { _, record -> record.id ?: record.hashCode().toLong() }) { index, record ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .staggeredEntrance(index = index)
                                .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = CardShadow),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = record.customerName ?: "未知顾客",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "¥${record.totalAmount?.setScale(2, RoundingMode.HALF_UP) ?: "0.00"}",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Primary,
                                        fontSize = 15.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "单号: ${record.recordNo ?: ""}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = record.salesDate ?: "",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                                if (!record.frameBrand.isNullOrBlank() || !record.lensBrand.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row {
                                        if (!record.frameBrand.isNullOrBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .background(PrimaryLight, RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    "${record.frameBrand} ${record.frameModel ?: ""}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Primary
                                                )
                                            }
                                        }
                                        if (!record.lensBrand.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFFF3E8FF), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    record.lensBrand,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF8B5CF6)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Pagination
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
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
}

@Composable
private fun SummaryCard(title: String, value: String, isHighlight: Boolean, modifier: Modifier = Modifier) {
    val cardBackground = if (isHighlight) {
        Brush.linearGradient(colors = listOf(Primary, SkyBlue))
    } else {
        Brush.linearGradient(colors = listOf(Color.White, Color.White))
    }

    val cardBorder = if (isHighlight) null else BorderStroke(1.dp, BorderColor)

    Card(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = if (isHighlight) Primary.copy(alpha = 0.3f) else CardShadow),
        shape = RoundedCornerShape(18.dp),
        border = cardBorder
    ) {
        Column(
            modifier = Modifier
                .background(brush = cardBackground)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isHighlight) Color.White.copy(alpha = 0.8f) else TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isHighlight) Color.White else TextPrimary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    date: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = date,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "选择日期",
                    tint = Primary
                )
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = Primary,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedLabelColor = Primary,
                unfocusedLabelColor = TextSecondary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .bounceClick {
                    showDialog = true
                }
        )
    }

    if (showDialog) {
        val initialEpochMillis = remember(date) {
            try {
                val localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
                localDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialEpochMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                val confirmInteractionSource = remember { MutableInteractionSource() }
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedLocalDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            val formattedDate = selectedLocalDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            onDateSelected(formattedDate)
                        }
                        showDialog = false
                    },
                    interactionSource = confirmInteractionSource,
                    modifier = Modifier.bounceClick(confirmInteractionSource)
                ) {
                    Text("确定", color = Primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                val cancelInteractionSource = remember { MutableInteractionSource() }
                TextButton(
                    onClick = { showDialog = false },
                    interactionSource = cancelInteractionSource,
                    modifier = Modifier.bounceClick(cancelInteractionSource)
                ) {
                    Text("取消", color = TextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = Color.White
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary,
                    headlineContentColor = TextPrimary,
                    weekdayContentColor = TextSecondary,
                    subheadContentColor = TextSecondary,
                    navigationContentColor = Primary,
                    yearContentColor = TextPrimary,
                    selectedYearContentColor = Color.White,
                    selectedYearContainerColor = Primary,
                    dayContentColor = TextPrimary,
                    selectedDayContentColor = Color.White,
                    selectedDayContainerColor = Primary,
                    todayContentColor = Primary,
                    todayDateBorderColor = Primary
                )
            )
        }
    }
}
