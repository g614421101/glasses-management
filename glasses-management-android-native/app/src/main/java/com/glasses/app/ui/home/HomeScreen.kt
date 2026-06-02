package com.glasses.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
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
import androidx.navigation.NavHostController
import com.glasses.app.discovery.ConnectionState
import com.glasses.app.navigation.Routes
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.glasses.app.theme.*
import com.glasses.app.ui.common.bounceClick
import com.glasses.app.viewmodel.ConnectionViewModel
import com.glasses.app.viewmodel.CustomerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    customerViewModel: CustomerViewModel = hiltViewModel(),
    connectionViewModel: ConnectionViewModel = hiltViewModel()
) {
    val customerState by customerViewModel.uiState.collectAsState()
    val connectionState by connectionViewModel.state.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Header / Greeting
            Text(
                text = "工作台",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "输入顾客手机号即可快速调出其历史视光与配镜档案",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Search Bar & Results
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            customerViewModel.onSearchChange(it)
                            if (it.isNotEmpty()) {
                                customerViewModel.onSearch()
                            }
                        },
                        placeholder = { Text("输入顾客手机号或姓名进行搜索...", color = TextTertiary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Primary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    customerViewModel.onSearchChange("")
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "清除",
                                        tint = TextSecondary
                                    )
                                }
                            }
                        },
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
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = CardShadow)
                    )

                    // Search Results Popper
                    AnimatedVisibility(
                        visible = searchQuery.isNotEmpty() && customerState.customers.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .shadow(16.dp, RoundedCornerShape(18.dp), spotColor = CardShadow),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "搜索结果 (${customerState.customers.size})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    )
                                    IconButton(
                                        onClick = {
                                            searchQuery = ""
                                            customerViewModel.onSearchChange("")
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "关闭",
                                            tint = TextTertiary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                HorizontalDivider(color = BorderColor, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                                customerState.customers.take(5).forEach { customer ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .bounceClick {
                                                navController.navigate("archive/${customer.id}")
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = customer.name,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            if (!customer.phone.isNullOrBlank()) {
                                                Text(
                                                    text = customer.phone,
                                                    fontSize = 12.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Shortcuts Group
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ShortcutCard(
                    title = "新建顾客",
                    description = "录入新客户信息",
                    icon = Icons.Default.Add,
                    gradientColors = listOf(Primary, SkyBlue),
                    onClick = {
                        navController.navigate(Routes.CUSTOMER)
                    },
                    modifier = Modifier.weight(1f)
                )

                ShortcutCard(
                    title = "所有顾客",
                    description = "浏览已登记列表",
                    icon = Icons.Default.Grid3x3,
                    gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)),
                    onClick = {
                        navController.navigate(Routes.CUSTOMER)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Connection Info Card
            if (connectionState is ConnectionState.Connected) {
                val conn = connectionState as ConnectionState.Connected
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = CardShadow),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "局域网连接成功",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "当前已自动连接至电脑端服务地址：",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryLight)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = conn.baseUrl,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        val rebindInteractionSource = remember { MutableInteractionSource() }
                        OutlinedButton(
                            onClick = { connectionViewModel.disconnect() },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderColor),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                            interactionSource = rebindInteractionSource,
                            modifier = Modifier
                                .fillMaxWidth()
                                .bounceClick(rebindInteractionSource)
                        ) {
                            Text("重新配对连接", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ShortcutCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .bounceClick(onClick = onClick)
            .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = CardShadow),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.linearGradient(colors = gradientColors),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
