package com.glasses.app.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.glasses.app.theme.*

data class MoreMenuItem(
    val label: String,
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
        MoreMenuItem("回收站", Icons.Default.Delete, "recycle-bin"),
        MoreMenuItem("用户管理", Icons.Default.AdminPanelSettings, "sys-user"),
        MoreMenuItem("数据管理", Icons.Default.Storage, "data-manage"),
        MoreMenuItem("个人资料", Icons.Default.Person, "profile")
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = BorderColor) }
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(
                text = "更多功能",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            items.forEach { item ->
                ListItem(
                    headlineContent = { Text(item.label, color = TextPrimary) },
                    leadingContent = { Icon(item.icon, contentDescription = null, tint = Primary) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable {
                        onDismiss()
                        if (item.route != null) onNavigate(item.route)
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderColor)

            ListItem(
                headlineContent = {
                    Text("退出登录", color = Error)
                },
                leadingContent = {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = Error)
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.clickable {
                    onDismiss()
                    onLogout()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
