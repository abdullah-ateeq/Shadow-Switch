package com.example.shadowswitch.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.shadowswitch.ui.theme.CyanNeon
import com.example.shadowswitch.ui.theme.MagentaNeon
import com.example.shadowswitch.ui.theme.SeamCore
import com.example.shadowswitch.ui.theme.ShadowSwitchTheme
import kotlin.math.sin

@Composable
fun AnimatedSplitBackground(
    modifier: Modifier = Modifier,
    activeSideProgress: Float = 0.5f // 0.0 = left dominant, 1.0 = right dominant, 0.5 = balanced
) {
    val extendedColors = ShadowSwitchTheme.colors

    val infiniteTransition = rememberInfiniteTransition(label = "BackgroundMotion")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    val gridOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "GridOffset"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val halfW = w / 2f

        // 1. Left Background (Cool Cyber)
        val leftBgBrush = Brush.verticalGradient(
            colors = listOf(
                extendedColors.leftDeep,
                Color(0xFF040A18),
                Color(0xFF01040A)
            ),
            startY = 0f,
            endY = h
        )
        drawRect(
            brush = leftBgBrush,
            topLeft = Offset(0f, 0f),
            size = Size(halfW, h)
        )

        // Left subtle horizon radial aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    extendedColors.leftPrimary.copy(alpha = 0.18f * (1f - activeSideProgress * 0.5f)),
                    Color.Transparent
                ),
                center = Offset(w * 0.25f, h * 0.45f),
                radius = w * 0.45f
            ),
            center = Offset(w * 0.25f, h * 0.45f),
            radius = w * 0.45f
        )

        // 2. Right Background (Warm Cyber)
        val rightBgBrush = Brush.verticalGradient(
            colors = listOf(
                extendedColors.rightDeep,
                Color(0xFF0D031A),
                Color(0xFF05010B)
            ),
            startY = 0f,
            endY = h
        )
        drawRect(
            brush = rightBgBrush,
            topLeft = Offset(halfW, 0f),
            size = Size(halfW, h)
        )

        // Right subtle horizon radial aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    extendedColors.rightPrimary.copy(alpha = 0.18f * (0.5f + activeSideProgress * 0.5f)),
                    Color.Transparent
                ),
                center = Offset(w * 0.75f, h * 0.45f),
                radius = w * 0.45f
            ),
            center = Offset(w * 0.75f, h * 0.45f),
            radius = w * 0.45f
        )

        // 3. Perspective Lines on Floor (from horizon down)
        val horizonY = h * 0.42f
        val lineCount = 7
        for (i in 0 until lineCount) {
            val progress = ((i.toFloat() / lineCount) + gridOffset) % 1f
            // Exponential distribution for perspective depth
            val y = horizonY + (h - horizonY) * (progress * progress)
            val alpha = (progress * 0.25f).coerceIn(0f, 0.25f)

            // Left floor horizontal line
            drawLine(
                color = extendedColors.leftPrimary.copy(alpha = alpha),
                start = Offset(0f, y),
                end = Offset(halfW - 2f, y),
                strokeWidth = 1.2f
            )

            // Right floor horizontal line
            drawLine(
                color = extendedColors.rightPrimary.copy(alpha = alpha),
                start = Offset(halfW + 2f, y),
                end = Offset(w, y),
                strokeWidth = 1.2f
            )
        }

        // Perspective vertical / diagonal perspective guidelines
        // Left diagonal rays
        drawLine(
            color = extendedColors.leftPrimary.copy(alpha = 0.15f),
            start = Offset(halfW, horizonY),
            end = Offset(0f, h),
            strokeWidth = 1.5f
        )
        drawLine(
            color = extendedColors.leftPrimary.copy(alpha = 0.18f),
            start = Offset(halfW, horizonY),
            end = Offset(w * 0.25f, h),
            strokeWidth = 1.5f
        )

        // Right diagonal rays
        drawLine(
            color = extendedColors.rightPrimary.copy(alpha = 0.15f),
            start = Offset(halfW, horizonY),
            end = Offset(w, h),
            strokeWidth = 1.5f
        )
        drawLine(
            color = extendedColors.rightPrimary.copy(alpha = 0.18f),
            start = Offset(halfW, horizonY),
            end = Offset(w * 0.75f, h),
            strokeWidth = 1.5f
        )

        // 4. Center Glowing Seam
        val seamGlowWidth = 22f
        // Soft outer seam halo
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    extendedColors.leftPrimary.copy(alpha = 0.15f * pulse),
                    extendedColors.seamColor.copy(alpha = 0.35f * pulse),
                    extendedColors.rightPrimary.copy(alpha = 0.15f * pulse)
                ),
                startX = halfW - seamGlowWidth,
                endX = halfW + seamGlowWidth
            ),
            topLeft = Offset(halfW - seamGlowWidth, 0f),
            size = Size(seamGlowWidth * 2f, h)
        )

        // Sharp core seam
        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    extendedColors.leftPrimary.copy(alpha = 0.7f),
                    SeamCore,
                    extendedColors.rightPrimary.copy(alpha = 0.7f)
                ),
                startY = 0f,
                endY = h
            ),
            start = Offset(halfW, 0f),
            end = Offset(halfW, h),
            strokeWidth = 2.5f
        )
    }
}
