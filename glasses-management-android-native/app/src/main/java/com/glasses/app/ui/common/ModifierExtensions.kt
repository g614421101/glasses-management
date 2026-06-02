package com.glasses.app.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Adds an elastic bounce effect on click.
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.94f,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounceScale"
    )
    
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }.then(
        if (onClick != null) {
            Modifier.clickable(
                interactionSource = interactionSource,
                indication = null, // Custom feedback instead of native ripple
                onClick = onClick
            )
        } else {
            Modifier
        }
    )
}

/**
 * Adds an elastic bounce effect based on an external interaction source.
 * Useful for standard Material 3 components (like Button, IconButton) to prevent click conflicts.
 */
fun Modifier.bounceClick(
    interactionSource: MutableInteractionSource,
    scaleDown: Float = 0.94f
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounceScale"
    )
    
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Slide-up & Fade-in staggered entrance animation for list items.
 */
fun Modifier.staggeredEntrance(
    index: Int,
    durationMillis: Int = 350,
    delayOffsetMillis: Int = 40
): Modifier = composed {
    val animatedState = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        animatedState.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = durationMillis,
                delayMillis = index * delayOffsetMillis,
                easing = FastOutSlowInEasing
            )
        )
    }
    
    this.graphicsLayer {
        alpha = animatedState.value
        translationY = (1f - animatedState.value) * 60f // Slide up by 60px
    }
}

/**
 * Shimmer sweeping animation brush for loading skeletons.
 */
fun Modifier.shimmer(
    colors: List<Color> = listOf(
        Color(0xFFE2E8F0),
        Color(0xFFF1F5F9),
        Color(0xFFE2E8F0)
    )
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    
    this.drawBehind {
        val brush = Brush.linearGradient(
            colors = colors,
            start = Offset(x = translateAnim - 500f, y = 0f),
            end = Offset(x = translateAnim, y = 500f)
        )
        drawRect(brush = brush)
    }
}
