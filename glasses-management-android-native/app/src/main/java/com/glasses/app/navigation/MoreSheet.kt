package com.glasses.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glasses.app.theme.*
import com.glasses.app.ui.common.bounceClick
import com.glasses.app.ui.common.staggeredEntrance

data class MoreMenuItem(
    val label: String,
    val description: String,
    val icon: ImageVector,
    val route: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreSheet(
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val items = listOf(
        MoreMenuItem("回收站", "查看和恢复已删除的数据", Icons.Default.Delete, "recycle-bin"),
        MoreMenuItem("用户管理", "管理账号权限与状态", Icons.Default.AdminPanelSettings, "sys-user"),
        MoreMenuItem("数据管理", "导入导出与数据重置", Icons.Default.Storage, "data-manage"),
        MoreMenuItem("个人资料", "修改个人信息与密码", Icons.Default.Person, "profile")
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = BorderColor) }
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {

            // ── 标题区域 ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Background)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Apps,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "更多功能",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "快捷操作与系统功能",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 主功能卡片 ──
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Surface,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    items.forEachIndexed { index, item ->
                        MoreSheetItem(
                            item = item,
                            index = index,
                            onClick = {
                                onDismiss()
                                if (item.route != null) onNavigate(item.route)
                            }
                        )
                        if (index < items.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = BorderColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── 退出登录卡片 ──
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Surface,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(index = items.size)
            ) {
                MoreSheetLogoutItem(
                    onClick = {
                        onDismiss()
                        onLogout()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MoreSheetItem(
    item: MoreMenuItem,
    index: Int,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .staggeredEntrance(index = index)
            .bounceClick(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // 图标容器
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PrimaryLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                item.icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // 标题 + 描述
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                fontSize = 12.sp
            )
        }

        // 右箭头
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun MoreSheetLogoutItem(onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(ErrorLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Logout,
                contentDescription = null,
                tint = Error,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = "退出登录",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = Error
        )
    }
}
