package com.glasses.app.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.glasses.app.theme.*

enum class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    Home("home", "工作台", Icons.Default.Home),
    Customer("customer", "顾客", Icons.Default.People),
    Stats("stats", "统计", Icons.Default.BarChart)
}

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onMoreClick: () -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
        modifier = Modifier.shadow(8.dp, spotColor = CardShadow)
    ) {
        BottomNavItem.entries.forEach { item ->
            val selected = currentRoute == item.route
            val scale by animateFloatAsState(if (selected) 1.15f else 1.0f, label = "iconScale")
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = { 
                    Icon(
                        item.icon, 
                        contentDescription = item.label,
                        modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
                    ) 
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    indicatorColor = PrimaryLight,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
        }
        NavigationBarItem(
            selected = false,
            onClick = onMoreClick,
            icon = { Icon(Icons.Default.MoreVert, contentDescription = "更多") },
            label = { Text("更多") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )
    }
}
