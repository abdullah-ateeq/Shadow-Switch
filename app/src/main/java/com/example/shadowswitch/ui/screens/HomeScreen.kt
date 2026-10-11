package com.example.shadowswitch.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shadowswitch.ui.components.AnimatedSplitBackground
import com.example.shadowswitch.ui.components.GlassCard
import com.example.shadowswitch.ui.components.NeonButton
import com.example.shadowswitch.ui.components.NeonButtonStyle
import com.example.shadowswitch.ui.navigation.Screen
import com.example.shadowswitch.ui.theme.CyanBright
import com.example.shadowswitch.ui.theme.CyanNeon
import com.example.shadowswitch.ui.theme.GoldTrophy
import com.example.shadowswitch.ui.theme.MagentaNeon
import com.example.shadowswitch.ui.theme.PinkSoft
import com.example.shadowswitch.ui.theme.SeamCore
import com.example.shadowswitch.ui.theme.ShadowSwitchTheme
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun HomeScreen(
    bestScore: Int,
    onPlayClick: () -> Unit,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ShadowSwitchTheme.colors

    // Auto demo side switch: alternates every 2.4 seconds
    var demoActiveLeft by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(2400)
            demoActiveLeft = !demoActiveLeft
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AnimatedSplitBackground(
            activeSideProgress = if (demoActiveLeft) 0.3f else 0.7f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Title + Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "SHADOW SWITCH",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.5.sp,
                    color = extendedColors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Two worlds. One tap.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    color = extendedColors.textSecondary
                )
            }

            // Center: Live Running Preview with Obstacle Demo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                HomeLivePreviewCanvas(
                    demoActiveLeft = demoActiveLeft
                )
            }

            // Bottom Section: CTA + High Score + Navigation Bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Play Button
                NeonButton(
                    text = "PLAY",
                    onClick = onPlayClick,
                    icon = Icons.Default.PlayArrow,
                    style = NeonButtonStyle.SPLIT_GRADIENT,
                    height = 62.dp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Best Score Pill
                GlassCard(
                    modifier = Modifier.fillMaxWidth(0.65f),
                    cornerRadius = 24.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = GoldTrophy,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BEST SCORE: ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = extendedColors.textSecondary
                        )
                        Text(
                            text = "$bestScore",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = extendedColors.textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Navigation Pill
                BottomNavBar(
                    currentScreen = Screen.HOME,
                    onNavigate = onNavigate
                )
            }
        }
    }
}

@Composable
private fun HomeLivePreviewCanvas(
    demoActiveLeft: Boolean,
    modifier: Modifier = Modifier
) {
    val extendedColors = ShadowSwitchTheme.colors

    val infiniteTransition = rememberInfiniteTransition(label = "HomeDemo")
    val stride by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "HomeStride"
    )

    val obstacleYOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 240f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ObstacleMotion"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .border(1.dp, extendedColors.glassBorder, RoundedCornerShape(24.dp))
    ) {
        val w = size.width
        val h = size.height
        val midX = w / 2f
        val runnerY = h * 0.70f

        val leftRunnerX = w * 0.25f
        val rightRunnerX = w * 0.75f

        // Draw Center Seam
        drawLine(
            brush = Brush.verticalGradient(
                listOf(CyanNeon.copy(alpha = 0.6f), SeamCore, MagentaNeon.copy(alpha = 0.6f))
            ),
            start = Offset(midX, 0f),
            end = Offset(midX, h),
            strokeWidth = 2.5f
        )

        // Draw Approach Obstacles
        // Left Obstacle (Low Block)
        val leftObsY = (obstacleYOffset) % h
        val leftObsWidth = w * 0.35f
        val leftObsHeight = 28f
        val leftObsAlpha = if (leftObsY < h * 0.85f) 0.9f else 0.3f

        drawRoundRect(
            color = CyanNeon.copy(alpha = if (demoActiveLeft) 0.85f else 0.35f),
            topLeft = Offset(leftRunnerX - leftObsWidth * 0.5f, leftObsY),
            size = Size(leftObsWidth, leftObsHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )

        // Right Obstacle (Laser Gate)
        val rightObsY = (obstacleYOffset + h * 0.5f) % h
        val rightObsWidth = w * 0.38f
        val rightObsHeight = 16f

        drawRoundRect(
            color = MagentaNeon.copy(alpha = if (!demoActiveLeft) 0.85f else 0.35f),
            topLeft = Offset(rightRunnerX - rightObsWidth * 0.5f, rightObsY),
            size = Size(rightObsWidth, rightObsHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )

        // Draw Runners
        val strideOffset = sin(stride) * 4f

        // 1. Left Runner
        val isLeftActive = demoActiveLeft
        val leftAlpha = if (isLeftActive) 1f else 0.35f
        val leftScale = if (isLeftActive) 1.08f else 0.92f

        // Left Glow Aura
        if (isLeftActive) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CyanNeon.copy(alpha = 0.45f), Color.Transparent),
                    center = Offset(leftRunnerX, runnerY + strideOffset),
                    radius = 36f
                ),
                radius = 36f,
                center = Offset(leftRunnerX, runnerY + strideOffset)
            )
        }

        // Left Runner Torso & Head
        drawCircle(
            color = CyanNeon.copy(alpha = leftAlpha),
            radius = 14f * leftScale,
            center = Offset(leftRunnerX, runnerY - 14f + strideOffset)
        )
        drawCircle(
            color = Color.White.copy(alpha = leftAlpha),
            radius = 6f * leftScale,
            center = Offset(leftRunnerX + 3f, runnerY - 16f + strideOffset)
        )
        // Legs
        drawLine(
            color = CyanNeon.copy(alpha = leftAlpha),
            start = Offset(leftRunnerX - 4f, runnerY - 2f + strideOffset),
            end = Offset(leftRunnerX - 6f, runnerY + 14f - strideOffset),
            strokeWidth = 3f * leftScale
        )
        drawLine(
            color = CyanNeon.copy(alpha = leftAlpha),
            start = Offset(leftRunnerX + 4f, runnerY - 2f + strideOffset),
            end = Offset(leftRunnerX + 6f, runnerY + 14f + strideOffset),
            strokeWidth = 3f * leftScale
        )

        // 2. Right Runner
        val isRightActive = !demoActiveLeft
        val rightAlpha = if (isRightActive) 1f else 0.35f
        val rightScale = if (isRightActive) 1.08f else 0.92f

        // Right Glow Aura
        if (isRightActive) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(MagentaNeon.copy(alpha = 0.45f), Color.Transparent),
                    center = Offset(rightRunnerX, runnerY + strideOffset),
                    radius = 36f
                ),
                radius = 36f,
                center = Offset(rightRunnerX, runnerY + strideOffset)
            )
        }

        // Right Runner Torso & Head
        drawCircle(
            color = MagentaNeon.copy(alpha = rightAlpha),
            radius = 14f * rightScale,
            center = Offset(rightRunnerX, runnerY - 14f + strideOffset)
        )
        drawCircle(
            color = Color.White.copy(alpha = rightAlpha),
            radius = 6f * rightScale,
            center = Offset(rightRunnerX - 3f, runnerY - 16f + strideOffset)
        )
        // Legs
        drawLine(
            color = MagentaNeon.copy(alpha = rightAlpha),
            start = Offset(rightRunnerX - 4f, runnerY - 2f + strideOffset),
            end = Offset(rightRunnerX - 6f, runnerY + 14f - strideOffset),
            strokeWidth = 3f * rightScale
        )
        drawLine(
            color = MagentaNeon.copy(alpha = rightAlpha),
            start = Offset(rightRunnerX + 4f, runnerY - 2f + strideOffset),
            end = Offset(rightRunnerX + 6f, runnerY + 14f + strideOffset),
            strokeWidth = 3f * rightScale
        )
    }
}

@Composable
fun BottomNavBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ShadowSwitchTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth(0.85f)
            .height(58.dp)
            .clip(RoundedCornerShape(29.dp))
            .background(extendedColors.glassBg)
            .border(1.dp, extendedColors.glassBorder, RoundedCornerShape(29.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavButton(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = currentScreen == Screen.HOME,
                onClick = { onNavigate(Screen.HOME) }
            )
            NavButton(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = currentScreen == Screen.SETTINGS,
                onClick = { onNavigate(Screen.SETTINGS) }
            )
            NavButton(
                icon = Icons.Default.Info,
                label = "About",
                isSelected = currentScreen == Screen.ABOUT,
                onClick = { onNavigate(Screen.ABOUT) }
            )
        }
    }
}

@Composable
private fun NavButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val extendedColors = ShadowSwitchTheme.colors

    val activeColor = if (isSelected) extendedColors.leftPrimary else extendedColors.textTertiary

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = activeColor,
            modifier = Modifier.size(20.dp)
        )
        if (isSelected) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = activeColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
