package com.glasses.app.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = Primary,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor,
    outlineVariant = BorderColor,
    error = Error
)

@Composable
fun GlassesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
