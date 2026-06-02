package com.glasses.app.ui.splash

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.glasses.app.discovery.ConnectionState
import com.glasses.app.theme.*
import com.glasses.app.ui.common.bounceClick

@Composable
fun SplashScreen(
    connectionState: ConnectionState,
    onConnectManual: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    GradientBackground {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Spacing / Brand Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(top = 60.dp)
                    .weight(1f, fill = false)
            ) {
                GlassesLogo()
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "视光档案",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "OPTICAL RECORD SYSTEM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary.copy(alpha = 0.6f),
                    letterSpacing = 2.sp
                )
            }

            // Middle Interactive Content Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = connectionState,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                    },
                    label = "connectionStateTransition"
                ) { state ->
                    when (state) {
                        is ConnectionState.Searching, is ConnectionState.Connecting -> {
                            LoadingSection(isConnecting = state is ConnectionState.Connecting)
                        }
                        is ConnectionState.ManualInput -> {
                            GlassCard {
                                ManualInputSection(onConnectManual = onConnectManual)
                            }
                        }
                        is ConnectionState.Error -> {
                            GlassCard {
                                Column {
                                    ErrorAlert(message = state.message)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    ManualInputSection(onConnectManual = onConnectManual)
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }

            // Footer Version
            Text(
                text = "v3.1.0 (Compose Native)",
                fontSize = 12.sp,
                color = TextSecondary.copy(alpha = 0.4f),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
fun GradientBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAFC), // Slate 50
                        Color(0xFFEFF6FF), // Blue 50
                        Color(0xFFE0F2FE)  // Sky 100
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0x1F3B82F6), // Soft blue glow
                radius = size.width * 0.9f,
                center = Offset(size.width * 0.1f, size.height * 0.15f)
            )
            drawCircle(
                color = Color(0x140EA5E9), // Soft sky blue glow
                radius = size.width * 0.8f,
                center = Offset(size.width * 0.9f, size.height * 0.85f)
            )
        }
        content()
    }
}

@Composable
fun GlassesLogo(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "logoPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .size(90.dp)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = CardShadow,
                spotColor = Primary.copy(alpha = 0.3f)
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Primary, SkyBlue)
                ),
                shape = CircleShape
            )
            .border(1.dp, Color(0x33FFFFFF), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(52.dp * scale)) {
            val width = size.width
            val height = size.height

            // Draw left lens
            drawCircle(
                color = Color.White,
                radius = width * 0.19f,
                center = Offset(width * 0.29f, height * 0.5f),
                style = Stroke(width = 3.dp.toPx())
            )
            // Draw right lens
            drawCircle(
                color = Color.White,
                radius = width * 0.19f,
                center = Offset(width * 0.71f, height * 0.5f),
                style = Stroke(width = 3.dp.toPx())
            )

            // Draw bridge (joining arc)
            drawArc(
                color = Color.White,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(width * 0.39f, height * 0.44f),
                size = Size(width * 0.22f, height * 0.12f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Left stem handle hook
            drawArc(
                color = Color.White,
                startAngle = 180f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(width * 0.05f, height * 0.43f),
                size = Size(width * 0.07f, height * 0.12f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Right stem handle hook
            drawArc(
                color = Color.White,
                startAngle = 270f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(width * 0.88f, height * 0.43f),
                size = Size(width * 0.07f, height * 0.12f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun LoadingSection(isConnecting: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "loadingPulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "pulseScale"
            )
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "pulseAlpha"
            )

            Box(
                modifier = Modifier
                    .size(72.dp * pulseScale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Primary.copy(alpha = 0.3f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )

            CircularProgressIndicator(
                color = Primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(64.dp)
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = if (isConnecting) "正在连接服务端..." else "正在搜索局域网服务...",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "同一 Wi-Fi 网络下会自动完成配对",
            fontSize = 13.sp,
            color = TextSecondary
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(26.dp),
                clip = false,
                spotColor = CardShadow,
                ambientColor = CardShadow
            )
            .background(Color(0xE6FFFFFF), RoundedCornerShape(26.dp))
            .border(
                border = BorderStroke(1.dp, Brush.verticalGradient(listOf(Color(0x332563EB), Color(0x102563EB)))),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(24.dp),
        content = content
    )
}

@Composable
fun ErrorAlert(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Error.copy(alpha = 0.08f))
            .border(1.dp, Error.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error icon",
            tint = Error,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = message,
            fontSize = 13.sp,
            color = Error,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ManualInputSection(onConnectManual: (String, Int) -> Unit) {
    var ip by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("8080") }

    Text(
        text = "连接到桌面端",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "局域网自动配对未成功，请输入电脑 IP 地址",
        fontSize = 12.sp,
        color = TextSecondary
    )

    Spacer(modifier = Modifier.height(20.dp))

    OutlinedTextField(
        value = ip,
        onValueChange = { ip = it },
        label = { Text("IP 地址", color = TextSecondary) },
        placeholder = { Text("例如 10.0.2.2 或本机 IP", color = TextTertiary.copy(alpha = 0.5f)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = Primary,
            unfocusedBorderColor = BorderColor,
            focusedLabelColor = Primary,
            unfocusedLabelColor = TextSecondary,
            cursorColor = Primary
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = port,
        onValueChange = { port = it },
        label = { Text("端口号", color = TextSecondary) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = Primary,
            unfocusedBorderColor = BorderColor,
            focusedLabelColor = Primary,
            unfocusedLabelColor = TextSecondary,
            cursorColor = Primary
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(24.dp))

    val connectInteractionSource = remember { MutableInteractionSource() }
    Button(
        onClick = {
            val p = port.toIntOrNull() ?: 8080
            if (ip.isNotBlank()) onConnectManual(ip.trim(), p)
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        interactionSource = connectInteractionSource,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .bounceClick(connectInteractionSource)
            .shadow(12.dp, RoundedCornerShape(14.dp), spotColor = Primary.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Primary, SkyBlue)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "开始连接",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
