package com.example.shadowswitch.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shadowswitch.ui.components.AnimatedSplitBackground
import com.example.shadowswitch.ui.theme.CyanBright
import com.example.shadowswitch.ui.theme.CyanNeon
import com.example.shadowswitch.ui.theme.MagentaNeon
import com.example.shadowswitch.ui.theme.PinkSoft
import com.example.shadowswitch.ui.theme.SeamCore
import com.example.shadowswitch.ui.theme.ShadowSwitchTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ShadowSwitchTheme.colors

    val leftGlowAnim = remember { Animatable(0f) }
    val seamPulseAnim = remember { Animatable(0f) }
    val rightGlowAnim = remember { Animatable(0f) }
    val titleAlphaAnim = remember { Animatable(0f) }
    val titleScaleAnim = remember { Animatable(0.85f) }

    LaunchedEffect(Unit) {
        // Step 1: Left character powers up
        leftGlowAnim.animateTo(1f, animationSpec = tween(400, easing = FastOutSlowInEasing))

        // Step 2: Energy shockwave shoots across seam
        seamPulseAnim.animateTo(1f, animationSpec = tween(350, easing = LinearEasing))

        // Step 3: Right character activates
        rightGlowAnim.animateTo(1f, animationSpec = tween(400, easing = FastOutSlowInEasing))

        // Step 4: Title fades and springs in
        launch {
            titleAlphaAnim.animateTo(1f, animationSpec = tween(450, easing = FastOutSlowInEasing))
        }
        launch {
            titleScaleAnim.animateTo(1f, animationSpec = tween(450, easing = FastOutSlowInEasing))
        }

        // Wait brief moment, then transition to Home
        delay(600)
        onSplashFinished()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedSplitBackground(
            activeSideProgress = (seamPulseAnim.value + rightGlowAnim.value) * 0.5f
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Mirrored Avatars & Energy Seam Canvas
            Canvas(
                modifier = Modifier.size(width = 220.dp, height = 120.dp)
            ) {
                val w = size.width
                val h = size.height
                val midX = w / 2f
                val midY = h / 2f

                // Draw Left Avatar (Futuristic Runner Silhouette)
                val leftX = midX - 55f
                val leftAlpha = 0.35f + leftGlowAnim.value * 0.65f
                val leftGlow = leftGlowAnim.value

                // Left outer glow halo
                if (leftGlow > 0.05f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(CyanNeon.copy(alpha = 0.5f * leftGlow), Color.Transparent),
                            center = Offset(leftX, midY),
                            radius = 35f
                        ),
                        radius = 35f,
                        center = Offset(leftX, midY)
                    )
                }

                // Left Avatar Body & Visor
                drawCircle(
                    color = CyanNeon.copy(alpha = leftAlpha),
                    radius = 16f,
                    center = Offset(leftX, midY)
                )
                drawCircle(
                    color = Color.White.copy(alpha = leftAlpha),
                    radius = 7f,
                    center = Offset(leftX + 4f, midY - 2f)
                )

                // Draw Center Seam
                drawLine(
                    color = SeamCore.copy(alpha = 0.8f),
                    start = Offset(midX, 10f),
                    end = Offset(midX, h - 10f),
                    strokeWidth = 2.5f
                )

                // Seam Crossing Pulse
                if (seamPulseAnim.value in 0.01f..0.99f) {
                    val pulseX = leftX + (w - leftX * 2f) * seamPulseAnim.value
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White, CyanBright, Color.Transparent),
                            center = Offset(pulseX, midY),
                            radius = 24f
                        ),
                        radius = 24f,
                        center = Offset(pulseX, midY)
                    )
                }

                // Draw Right Avatar
                val rightX = midX + 55f
                val rightAlpha = 0.35f + rightGlowAnim.value * 0.65f
                val rightGlow = rightGlowAnim.value

                // Right outer glow halo
                if (rightGlow > 0.05f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(MagentaNeon.copy(alpha = 0.5f * rightGlow), Color.Transparent),
                            center = Offset(rightX, midY),
                            radius = 35f
                        ),
                        radius = 35f,
                        center = Offset(rightX, midY)
                    )
                }

                // Right Avatar Body & Visor
                drawCircle(
                    color = MagentaNeon.copy(alpha = rightAlpha),
                    radius = 16f,
                    center = Offset(rightX, midY)
                )
                drawCircle(
                    color = Color.White.copy(alpha = rightAlpha),
                    radius = 7f,
                    center = Offset(rightX - 4f, midY - 2f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Game Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(titleAlphaAnim.value)
                    .scale(titleScaleAnim.value)
            ) {
                Text(
                    text = "SHADOW SWITCH",
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                    letterSpacing = 3.sp,
                    color = extendedColors.textPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Two worlds. One tap.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    color = extendedColors.textSecondary
                )
            }
        }
    }
}
