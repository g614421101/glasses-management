package com.glasses.app.ui.datamanage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.glasses.app.viewmodel.DataManageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManageScreen(
    onBack: () -> Unit,
    viewModel: DataManageViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val btnEnabled = !state.isLoading

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
                title = { Text("数据管理", fontWeight = FontWeight.Bold, color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextSecondary
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
                modifier = Modifier.shadow(4.dp, spotColor = CardShadow)
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Export Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = CardShadow),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("导出数据", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("将所有数据导出为 JSON 文件到设备存储中", fontSize = 12.sp, color = TextSecondary)
                    Spacer(Modifier.height(16.dp))
                    val exportInteractionSource = remember { MutableInteractionSource() }
                    Button(
                        onClick = { viewModel.exportData() },
                        enabled = btnEnabled,
                        interactionSource = exportInteractionSource,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (btnEnabled) Modifier.bounceClick(exportInteractionSource) else Modifier)
                    ) {
                        Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("导出 JSON", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Import Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = CardShadow),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("导入数据", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("选择数据文件进行批量还原与覆盖", fontSize = 12.sp, color = TextSecondary)
                    Spacer(Modifier.height(12.dp))
                    Row {
                        val mergeInteractionSource = remember { MutableInteractionSource() }
                        FilterChip(
                            selected = state.importMode == "merge",
                            onClick = { viewModel.setImportMode("merge") },
                            interactionSource = mergeInteractionSource,
                            label = { Text("合并") },
                            colors = chipColors,
                            modifier = Modifier.bounceClick(mergeInteractionSource)
                        )
                        Spacer(Modifier.width(8.dp))
                        val replaceInteractionSource = remember { MutableInteractionSource() }
                        FilterChip(
                            selected = state.importMode == "replace",
                            onClick = { viewModel.setImportMode("replace") },
                            interactionSource = replaceInteractionSource,
                            label = { Text("替换") },
                            colors = chipColors,
                            modifier = Modifier.bounceClick(replaceInteractionSource)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (state.importMode == "merge") "合并模式：跳过已存在的记录" else "替换模式：清空所有记录后重新导入",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(14.dp))
                    val importInteractionSource = remember { MutableInteractionSource() }
                    OutlinedButton(
                        onClick = { viewModel.importData(null) },
                        enabled = btnEnabled,
                        interactionSource = importInteractionSource,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BorderColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (btnEnabled) Modifier.bounceClick(importInteractionSource) else Modifier)
                    ) {
                        Icon(Icons.Default.Upload, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("选择文件并导入", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Reset Card (Danger Zone)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = CardShadow),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                border = BorderStroke(1.dp, Error.copy(alpha = 0.2f))
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("重置数据", fontWeight = FontWeight.Bold, color = Error, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("清空所有业务数据（顾客、验光、销售记录），保留管理员账号", fontSize = 12.sp, color = Error.copy(alpha = 0.7f))
                    Spacer(Modifier.height(16.dp))
                    val showResetInteractionSource = remember { MutableInteractionSource() }
                    Button(
                        onClick = { viewModel.showResetConfirm() },
                        interactionSource = showResetInteractionSource,
                        colors = ButtonDefaults.buttonColors(containerColor = Error),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick(showResetInteractionSource)
                    ) {
                        Text("重置数据", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            if (state.isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Primary)
            }

            if (state.message != null) {
                Snackbar(
                    modifier = Modifier.fillMaxWidth(),
                    action = {
                        val snackbarInteractionSource = remember { MutableInteractionSource() }
                        TextButton(
                            onClick = { viewModel.clearMessage() },
                            interactionSource = snackbarInteractionSource,
                            modifier = Modifier.bounceClick(snackbarInteractionSource)
                        ) { Text("关闭", color = Primary) }
                    },
                    containerColor = Color.White,
                    contentColor = TextPrimary
                ) { Text(state.message!!) }
            }
        }
    }

    if (state.showResetConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.hideResetConfirm() },
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
                    Text("确认重置", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            },
            text = { Text("此操作将清空所有业务数据（顾客、验光、销售记录），仅保留管理员账号。是否继续？", color = TextSecondary) },
            confirmButton = {
                val resetConfirmInteractionSource = remember { MutableInteractionSource() }
                Button(
                    onClick = { viewModel.resetData() },
                    interactionSource = resetConfirmInteractionSource,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Error),
                    modifier = Modifier.bounceClick(resetConfirmInteractionSource)
                ) {
                    Text("重置", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                val resetCancelInteractionSource = remember { MutableInteractionSource() }
                TextButton(
                    onClick = { viewModel.hideResetConfirm() },
                    interactionSource = resetCancelInteractionSource,
                    modifier = Modifier.bounceClick(resetCancelInteractionSource)
                ) {
                    Text("取消", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}
