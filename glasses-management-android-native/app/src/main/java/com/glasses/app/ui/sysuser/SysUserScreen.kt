package com.glasses.app.ui.sysuser

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
import com.glasses.app.data.model.SysUser
import com.glasses.app.theme.*
import com.glasses.app.ui.common.bounceClick
import com.glasses.app.ui.common.staggeredEntrance
import com.glasses.app.viewmodel.SysUserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SysUserScreen(
    onBack: () -> Unit,
    viewModel: SysUserViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("账号管理", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
                        onClick = { viewModel.loadUsers() },
                        interactionSource = refreshInteractionSource,
                        modifier = Modifier.bounceClick(refreshInteractionSource)
                    ) {
                        Icon(Icons.Default.Refresh, "刷新")
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = state.includeDeleted,
                    onCheckedChange = { viewModel.toggleIncludeDeleted() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Primary,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = BorderColor
                    )
                )
                Spacer(Modifier.width(8.dp))
                Text("显示已删除", fontWeight = FontWeight.SemiBold, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
            }

            when {
                state.isLoading && state.users.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                state.users.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("暂无用户", color = TextSecondary)
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(state.users, key = { _, user -> user.id!! }) { index, user ->
                            UserCard(
                                user = user,
                                index = index,
                                onDisable = { viewModel.disableUser(user.id!!) },
                                onEnable = { viewModel.enableUser(user.id!!) },
                                onDelete = { viewModel.showDeleteConfirm(user) },
                                onRestore = { viewModel.restoreUser(user.id!!) },
                                onPurge = { viewModel.showPurgeConfirm(user) },
                                onResetPassword = { viewModel.resetPassword(user.id!!) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.showDeleteConfirm && state.deletingUser != null) {
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
            text = { Text("确定要删除用户「${state.deletingUser!!.username}」吗？", color = TextSecondary) },
            confirmButton = {
                val delConfirmInteractionSource = remember { MutableInteractionSource() }
                Button(
                    onClick = { viewModel.deleteUser() },
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

    if (state.showPurgeConfirm && state.purgingUser != null) {
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
            text = { Text("此操作不可恢复，确定要彻底删除用户「${state.purgingUser!!.username}」吗？", color = TextSecondary) },
            confirmButton = {
                val purgeConfirmInteractionSource = remember { MutableInteractionSource() }
                Button(
                    onClick = { viewModel.purgeUser() },
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

    if (state.showResetPasswordDialog && state.resetPasswordResult != null) {
        AlertDialog(
            onDismissRequest = { viewModel.hideResetPasswordDialog() },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("密码已重置", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            },
            text = { Text("临时密码: ${state.resetPasswordResult}\n请告知用户登录后立即修改密码。", color = TextSecondary) },
            confirmButton = {
                val resetCloseInteractionSource = remember { MutableInteractionSource() }
                Button(
                    onClick = { viewModel.hideResetPasswordDialog() },
                    interactionSource = resetCloseInteractionSource,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier.bounceClick(resetCloseInteractionSource)
                ) {
                    Text("已复制新密码并关闭", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun UserCard(
    user: SysUser,
    index: Int,
    onDisable: () -> Unit,
    onEnable: () -> Unit,
    onDelete: () -> Unit,
    onRestore: () -> Unit,
    onPurge: () -> Unit,
    onResetPassword: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .staggeredEntrance(index = index)
            .shadow(6.dp, RoundedCornerShape(18.dp), spotColor = CardShadow),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        user.username ?: "",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                    if (!user.phone.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            user.phone,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
                Row {
                    if (user.deleted == true) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFFF1F2), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("已删除", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Error)
                        }
                    } else if (user.disabled == true) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFFF1F2), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("已禁用", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Error)
                        }
                    } else {
                        val roleLabel = when (user.role) {
                            "admin" -> "管理员"
                            "merchant" -> "商户"
                            else -> "商户"
                        }
                        Box(
                            modifier = Modifier
                                .background(PrimaryLight, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(roleLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Primary)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderColor)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (user.deleted == true) {
                    val restoreInteractionSource = remember { MutableInteractionSource() }
                    TextButton(
                        onClick = onRestore,
                        interactionSource = restoreInteractionSource,
                        modifier = Modifier.bounceClick(restoreInteractionSource)
                    ) {
                        Text("恢复", color = Primary)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    val purgeInteractionSource = remember { MutableInteractionSource() }
                    TextButton(
                        onClick = onPurge,
                        interactionSource = purgeInteractionSource,
                        modifier = Modifier.bounceClick(purgeInteractionSource)
                    ) {
                        Text("彻底删除", color = Error)
                    }
                } else {
                    val resetPwdInteractionSource = remember { MutableInteractionSource() }
                    TextButton(
                        onClick = onResetPassword,
                        interactionSource = resetPwdInteractionSource,
                        modifier = Modifier.bounceClick(resetPwdInteractionSource)
                    ) {
                        Text("重置密码", color = Primary)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    if (user.disabled == true) {
                        val enableInteractionSource = remember { MutableInteractionSource() }
                        TextButton(
                            onClick = onEnable,
                            interactionSource = enableInteractionSource,
                            modifier = Modifier.bounceClick(enableInteractionSource)
                        ) {
                            Text("启用", color = Primary)
                        }
                    } else {
                        val disableInteractionSource = remember { MutableInteractionSource() }
                        TextButton(
                            onClick = onDisable,
                            interactionSource = disableInteractionSource,
                            modifier = Modifier.bounceClick(disableInteractionSource)
                        ) {
                            Text("禁用", color = Primary)
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    val deleteInteractionSource = remember { MutableInteractionSource() }
                    TextButton(
                        onClick = onDelete,
                        interactionSource = deleteInteractionSource,
                        modifier = Modifier.bounceClick(deleteInteractionSource)
                    ) {
                        Text("删除", color = Error)
                    }
                }
            }
        }
    }
}
