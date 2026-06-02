package com.glasses.app.ui.stats

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
import com.glasses.app.viewmodel.StatsViewModel
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
    onNavigateToArchive: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("营收统计") },
            actions = {
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新")
                }
            }
        )

        // Filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = state.showAll,
                onCheckedChange = { viewModel.onShowAllChange(it) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("查看全部", style = MaterialTheme.typography.bodyMedium)

            if (!state.showAll) {
                Spacer(modifier = Modifier.weight(1f))
                OutlinedTextField(
                    value = state.startDate,
                    onValueChange = { viewModel.onDateChange(it, state.endDate) },
                    label = { Text("起始") },
                    singleLine = true,
                    modifier = Modifier.width(120.dp),
                    textStyle = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = state.endDate,
                    onValueChange = { viewModel.onDateChange(state.startDate, it) },
                    label = { Text("截止") },
                    singleLine = true,
                    modifier = Modifier.width(120.dp),
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Summary cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = "总营收",
                value = "¥${state.totalRevenue.setScale(2, RoundingMode.HALF_UP)}",
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "订单数",
                value = "${state.orderCount}",
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "客单价",
                value = if (state.orderCount > 0) {
                    "¥${state.totalRevenue.divide(BigDecimal.valueOf(state.orderCount), 2, RoundingMode.HALF_UP)}"
                } else "¥0.00",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content
        when {
            state.isLoading && state.records.isEmpty() -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.records.isEmpty() -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("暂无销售记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.records, key = { it.id ?: it.hashCode().toLong() }) { record ->
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = record.customerName ?: "未知顾客",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "¥${record.totalAmount?.setScale(2, RoundingMode.HALF_UP) ?: "0.00"}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = record.recordNo ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = record.salesDate ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (!record.frameBrand.isNullOrBlank() || !record.lensBrand.isNullOrBlank()) {
                                    Row(modifier = Modifier.padding(top = 4.dp)) {
                                        if (!record.frameBrand.isNullOrBlank()) {
                                            AssistChip(
                                                onClick = {},
                                                label = { Text("${record.frameBrand} ${record.frameModel ?: ""}", style = MaterialTheme.typography.labelSmall) },
                                                modifier = Modifier.height(24.dp)
                                            )
                                        }
                                        if (!record.lensBrand.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            AssistChip(
                                                onClick = {},
                                                label = { Text(record.lensBrand, style = MaterialTheme.typography.labelSmall) },
                                                modifier = Modifier.height(24.dp)
                                            )
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
                    TextButton(onClick = { viewModel.onPrevPage() }, enabled = state.currentPage > 1) {
                        Text("上一页")
                    }
                    Text("${state.currentPage} / ${state.totalPages}", modifier = Modifier.padding(horizontal = 16.dp))
                    TextButton(onClick = { viewModel.onNextPage() }, enabled = state.currentPage < state.totalPages) {
                        Text("下一页")
                    }
                }
            }
        }

        if (state.error != null) {
            Snackbar(modifier = Modifier.padding(16.dp), action = {
                TextButton(onClick = { viewModel.clearError() }) { Text("关闭") }
            }) { Text(state.error!!) }
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}
