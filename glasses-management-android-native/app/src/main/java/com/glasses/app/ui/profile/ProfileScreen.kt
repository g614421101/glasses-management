package com.glasses.app.ui.profile

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.glasses.app.theme.*
import com.glasses.app.ui.common.bounceClick
import com.glasses.app.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedBorderColor = Primary,
        unfocusedBorderColor = BorderColor,
        focusedLabelColor = Primary,
        unfocusedLabelColor = TextSecondary,
        cursorColor = Primary
    )

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("个人资料", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = CardShadow),
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
                        Text("账号信息", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                        if (!state.isEditing) {
                            val editInteractionSource = remember { MutableInteractionSource() }
                            IconButton(
                                onClick = { viewModel.startEditing() },
                                interactionSource = editInteractionSource,
                                modifier = Modifier.bounceClick(editInteractionSource)
                            ) {
                                Icon(Icons.Default.Edit, "编辑", tint = Primary)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (state.isEditing) {
                        OutlinedTextField(
                            value = state.realName,
                            onValueChange = { viewModel.onRealNameChange(it) },
                            label = { Text("显示名称") },
                            leadingIcon = { Icon(Icons.Default.Person, null, tint = Primary) },
                            singleLine = true,
                            colors = fieldColors,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.username,
                            onValueChange = { viewModel.onUsernameChange(it) },
                            label = { Text("用户名") },
                            leadingIcon = { Icon(Icons.Default.AccountCircle, null, tint = Primary) },
                            singleLine = true,
                            colors = fieldColors,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.phone,
                            onValueChange = { viewModel.onPhoneChange(it) },
                            label = { Text("手机号") },
                            leadingIcon = { Icon(Icons.Default.Phone, null, tint = Primary) },
                            singleLine = true,
                            colors = fieldColors,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            val cancelInteractionSource = remember { MutableInteractionSource() }
                            TextButton(
                                onClick = { viewModel.cancelEditing() },
                                interactionSource = cancelInteractionSource,
                                modifier = Modifier.bounceClick(cancelInteractionSource)
                            ) {
                                Text("取消", color = TextSecondary)
                            }
                            Spacer(Modifier.width(8.dp))
                            val saveEnabled = !state.isLoading && state.username.isNotBlank()
                            val saveInteractionSource = remember { MutableInteractionSource() }
                            Button(
                                onClick = { viewModel.saveProfile() },
                                enabled = saveEnabled,
                                interactionSource = saveInteractionSource,
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = if (saveEnabled) Modifier.bounceClick(saveInteractionSource) else Modifier
                            ) {
                                Text("保存")
                            }
                        }
                    } else {
                        ProfileRow("显示名称", state.realName.ifBlank { "-" })
                        ProfileRow("用户名", state.username)
                        ProfileRow("手机号", state.phone.ifBlank { "-" })
                        ProfileRow("角色", state.role)
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = CardShadow),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("系统安全", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Spacer(Modifier.height(12.dp))
                    val showPwdInteractionSource = remember { MutableInteractionSource() }
                    OutlinedButton(
                        onClick = { viewModel.showChangePassword() },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                        interactionSource = showPwdInteractionSource,
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick(showPwdInteractionSource)
                    ) {
                        Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("修改密码", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    if (state.showChangePassword) {
        ChangePasswordDialog(
            onDismiss = { viewModel.hideChangePassword() },
            onConfirm = { o, n, c -> viewModel.changePassword(o, n, c) },
            isLoading = state.isLoading
        )
    }

    if (state.success != null) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                val successCloseInteractionSource = remember { MutableInteractionSource() }
                TextButton(
                    onClick = { viewModel.clearMessages() },
                    interactionSource = successCloseInteractionSource,
                    modifier = Modifier.bounceClick(successCloseInteractionSource)
                ) { Text("关闭", color = Primary) }
            },
            containerColor = Color.White,
            contentColor = TextPrimary
        ) { Text(state.success!!) }
    }

    if (state.error != null) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                val errorCloseInteractionSource = remember { MutableInteractionSource() }
                TextButton(
                    onClick = { viewModel.clearMessages() },
                    interactionSource = errorCloseInteractionSource,
                    modifier = Modifier.bounceClick(errorCloseInteractionSource)
                ) { Text("关闭", color = Primary) }
            },
            containerColor = Color.White,
            contentColor = TextPrimary
        ) { Text(state.error!!) }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
    isLoading: Boolean
) {
    var old by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    var oldVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

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
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("修改密码", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = old,
                    onValueChange = { old = it },
                    label = { Text("当前密码") },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = Primary) },
                    trailingIcon = {
                        IconButton(onClick = { oldVisible = !oldVisible }) {
                            Icon(
                                imageVector = if (oldVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (oldVisible) "隐藏密码" else "显示密码",
                                tint = TextSecondary
                            )
                        }
                    },
                    visualTransformation = if (oldVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    colors = fieldColors,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = new,
                    onValueChange = { new = it },
                    label = { Text("新密码") },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = Primary) },
                    trailingIcon = {
                        IconButton(onClick = { newVisible = !newVisible }) {
                            Icon(
                                imageVector = if (newVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (newVisible) "隐藏密码" else "显示密码",
                                tint = TextSecondary
                            )
                        }
                    },
                    visualTransformation = if (newVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    colors = fieldColors,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = { Text("确认新密码") },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = Primary) },
                    trailingIcon = {
                        IconButton(onClick = { confirmVisible = !confirmVisible }) {
                            Icon(
                                imageVector = if (confirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (confirmVisible) "隐藏密码" else "显示密码",
                                tint = TextSecondary
                            )
                        }
                    },
                    visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    colors = fieldColors,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val confirmEnabled = !isLoading && old.isNotBlank() && new.length >= 6 && new == confirm
            val confirmInteractionSource = remember { MutableInteractionSource() }
            Button(
                onClick = { onConfirm(old, new, confirm) },
                enabled = confirmEnabled,
                interactionSource = confirmInteractionSource,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = if (confirmEnabled) Modifier.bounceClick(confirmInteractionSource) else Modifier
            ) { Text("确定", color = Color.White, fontWeight = FontWeight.Bold) }
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
