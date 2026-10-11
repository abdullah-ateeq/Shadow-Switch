package com.example.shadowswitch.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.shadowswitch.game.GameEngine
import com.example.shadowswitch.game.GameStatus
import com.example.shadowswitch.game.Obstacle
import com.example.shadowswitch.game.ObstacleSide
import com.example.shadowswitch.game.ObstacleType
import com.example.shadowswitch.game.PlayerSide
import com.example.shadowswitch.ui.components.GlassCard
import com.example.shadowswitch.ui.components.NeonButton
import com.example.shadowswitch.ui.components.NeonButtonStyle
import com.example.shadowswitch.ui.components.NeonIconButton
import com.example.shadowswitch.ui.theme.CyanAqua
import com.example.shadowswitch.ui.theme.CyanBright
import com.example.shadowswitch.ui.theme.CyanGlow
import com.example.shadowswitch.ui.theme.CyanNeon
import com.example.shadowswitch.ui.theme.DangerRed
import com.example.shadowswitch.ui.theme.GoldTrophy
import com.example.shadowswitch.ui.theme.LaserCore
import com.example.shadowswitch.ui.theme.MagentaGlow
import com.example.shadowswitch.ui.theme.MagentaNeon
import com.example.shadowswitch.ui.theme.PinkSoft
import com.example.shadowswitch.ui.theme.SeamCore
import com.example.shadowswitch.ui.theme.ShadowSwitchTheme
import com.example.shadowswitch.viewmodel.GameViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ShadowSwitchTheme.colors
    val engine = viewModel.engine

    // Lifecycle observer to pause game when app moves to background
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                if (engine.status == GameStatus.PLAYING) {
                    engine.pause()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Frame-based game loop
    var canvasWidth by remember { mutableFloatStateOf(1080f) }
    var canvasHeight by remember { mutableFloatStateOf(2400f) }

    LaunchedEffect(Unit) {
        var lastFrameTime = 0L
        while (true) {
            withFrameNanos { frameTimeNanos ->
                if (lastFrameTime != 0L) {
                    val deltaSeconds = (frameTimeNanos - lastFrameTime) / 1_000_000_000f
                    engine.update(deltaSeconds, canvasWidth, canvasHeight)
                }
                lastFrameTime = frameTimeNanos
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        viewModel.handlePlayfieldTap(canvasWidth, canvasHeight)
                    }
                )
            }
    ) {
        // Main Game Canvas
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            canvasWidth = size.width
            canvasHeight = size.height

            drawGameWorld(
                engine = engine,
                isDark = extendedColors.isDark
            )
        }

        // Top Minimal HUD
        GameTopHUD(
            score = engine.score,
            bestScore = viewModel.bestScore,
            combo = engine.combo,
            activeSide = engine.activeSide,
            onPauseClick = { viewModel.pause() }
        )

        // Floating Notices (e.g. "+1 PERFECT SWITCH!")
        FloatingNoticesOverlay(engine = engine)

        // Countdown Overlay (3, 2, 1, SWITCH!)
        if (engine.status == GameStatus.COUNTDOWN) {
            CountdownOverlay(countdownText = engine.countdownText)
        }

        // First-Run Interactive Tutorial Hint
        if (!viewModel.isTutorialCompleted && engine.status == GameStatus.PLAYING) {
            TutorialHintOverlay(
                activeSide = engine.activeSide,
                onDismiss = { viewModel.completeTutorial() }
            )
        }

        // Pause Menu Dialog
        if (engine.status == GameStatus.PAUSED) {
            PauseMenuDialog(
                onResume = { viewModel.resume() },
                onRestart = { viewModel.restart() },
                onHome = onHomeClick
            )
        }
    }
}

// ---------------------------------------------------------------------------
// CANVAS RENDERING LOGIC
// ---------------------------------------------------------------------------

private fun DrawScope.drawGameWorld(
    engine: GameEngine,
    isDark: Boolean
) {
    val frame = engine.frameTick
    val w = size.width
    val h = size.height
    val halfW = w / 2f
    val horizonY = h * 0.38f

    // Camera Shake Offset (during collision game over)
    val shakeX = engine.shakeOffsetX
    val shakeY = engine.shakeOffsetY

    val activeSide = engine.activeSide

    // 1. Split-World Backgrounds
    // Left half (Cool Cyber)
    val leftActive = (activeSide == PlayerSide.ACTIVE_LEFT)
    val leftBgColor = if (leftActive) Color(0xFF041226) else Color(0xFF020712)
    drawRect(
        color = leftBgColor,
        topLeft = Offset(shakeX, shakeY),
        size = Size(halfW, h)
    )

    // Right half (Warm Cyber)
    val rightActive = (activeSide == PlayerSide.ACTIVE_RIGHT)
    val rightBgColor = if (rightActive) Color(0xFF1E052B) else Color(0xFF0B0112)
    drawRect(
        color = rightBgColor,
        topLeft = Offset(halfW + shakeX, shakeY),
        size = Size(halfW, h)
    )

    // Active side radiant floor bloom
    val bloomX = if (leftActive) w * 0.25f else w * 0.75f
    val bloomColor = if (leftActive) CyanGlow else MagentaGlow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(bloomColor.copy(alpha = 0.28f), Color.Transparent),
            center = Offset(bloomX + shakeX, h * 0.72f + shakeY),
            radius = w * 0.45f
        ),
        center = Offset(bloomX + shakeX, h * 0.72f + shakeY),
        radius = w * 0.45f
    )

    // 2. Perspective Floor Grid (giving intense forward speed)
    val gridOffset = engine.perspectiveGridOffset
    val lineCount = 9
    for (i in 0 until lineCount) {
        val p = ((i.toFloat() / lineCount) + gridOffset) % 1f
        // Perspective depth curve
        val y = horizonY + (h - horizonY) * (p * p)
        val alpha = (p * 0.32f).coerceIn(0f, 0.35f)

        // Left floor line
        drawLine(
            color = CyanNeon.copy(alpha = if (leftActive) alpha else alpha * 0.4f),
            start = Offset(shakeX, y + shakeY),
            end = Offset(halfW + shakeX, y + shakeY),
            strokeWidth = 1.5f + p * 2f
        )
        // Right floor line
        drawLine(
            color = MagentaNeon.copy(alpha = if (rightActive) alpha else alpha * 0.4f),
            start = Offset(halfW + shakeX, y + shakeY),
            end = Offset(w + shakeX, y + shakeY),
            strokeWidth = 1.5f + p * 2f
        )
    }

    // Floor perspective guide rays
    drawLine(
        color = CyanNeon.copy(alpha = if (leftActive) 0.22f else 0.08f),
        start = Offset(halfW + shakeX, horizonY + shakeY),
        end = Offset(w * 0.10f + shakeX, h + shakeY),
        strokeWidth = 1.8f
    )
    drawLine(
        color = MagentaNeon.copy(alpha = if (rightActive) 0.22f else 0.08f),
        start = Offset(halfW + shakeX, horizonY + shakeY),
        end = Offset(w * 0.90f + shakeX, h + shakeY),
        strokeWidth = 1.8f
    )

    // 3. Approaching Obstacles
    for (obs in engine.obstacles) {
        drawObstacle(obs, w, h, shakeX, shakeY)
    }

    // 4. Center Glowing Seam / Dimensional Tear
    drawCenterSeam(
        engine = engine,
        w = w,
        h = h,
        shakeX = shakeX,
        shakeY = shakeY
    )

    // 5. Phase Ripples (when shadow phases safely through obstacles)
    for (pr in engine.phaseRipples) {
        drawCircle(
            color = pr.color.copy(alpha = pr.alpha),
            radius = pr.currentRadius,
            center = Offset(pr.x + shakeX, pr.y + shakeY),
            style = Stroke(width = 3.5f)
        )
        // Second inner ripple
        if (pr.currentRadius > 15f) {
            drawCircle(
                color = Color.White.copy(alpha = pr.alpha * 0.7f),
                radius = pr.currentRadius * 0.65f,
                center = Offset(pr.x + shakeX, pr.y + shakeY),
                style = Stroke(width = 2f)
            )
        }
    }

    // 6. Cyber Particles (trail, bursts, explosions)
    for (p in engine.particles) {
        drawCircle(
            color = p.color.copy(alpha = p.alpha),
            radius = p.size,
            center = Offset(p.x + shakeX, p.y + shakeY)
        )
    }

    // 7. Mirrored Runners
    val runnerY = h * 0.72f
    val leftRunnerX = w * 0.25f
    val rightRunnerX = w * 0.75f

    // Draw Left Runner
    drawRunnerAvatar(
        x = leftRunnerX + shakeX,
        y = runnerY + shakeY,
        isActive = (activeSide == PlayerSide.ACTIVE_LEFT),
        scaleModifier = if (activeSide == PlayerSide.ACTIVE_LEFT) engine.activeCharacterScale else 1f,
        strideCycle = engine.runStrideCycle,
        primaryColor = CyanNeon,
        glowColor = CyanGlow,
        gameOverState = engine.gameOverState
    )

    // Draw Right Runner
    drawRunnerAvatar(
        x = rightRunnerX + shakeX,
        y = runnerY + shakeY,
        isActive = (activeSide == PlayerSide.ACTIVE_RIGHT),
        scaleModifier = if (activeSide == PlayerSide.ACTIVE_RIGHT) engine.activeCharacterScale else 1f,
        strideCycle = engine.runStrideCycle,
        primaryColor = MagentaNeon,
        glowColor = MagentaGlow,
        gameOverState = engine.gameOverState
    )

    // 8. Game Over Flash
    engine.gameOverState?.let { go ->
        if (go.flashAlpha > 0f) {
            drawRect(
                color = Color.White.copy(alpha = go.flashAlpha * 0.45f),
                topLeft = Offset.Zero,
                size = size
            )
        }
    }
}

// ---------------------------------------------------------------------------
// RUNNER AVATAR RENDERING
// ---------------------------------------------------------------------------

private fun DrawScope.drawRunnerAvatar(
    x: Float,
    y: Float,
    isActive: Boolean,
    scaleModifier: Float,
    strideCycle: Float,
    primaryColor: Color,
    glowColor: Color,
    gameOverState: com.example.shadowswitch.game.GameOverAnimationState?
) {
    if (gameOverState != null && isActive) {
        // In collision game over: active character breaks into particles
        return
    }

    val alpha = if (gameOverState != null && !isActive) {
        gameOverState.shadowAlpha
    } else if (isActive) {
        1f
    } else {
        0.38f
    }

    val baseScale = (if (isActive) 1.08f else 0.94f) * scaleModifier
    val strideOffset = sin(strideCycle) * 3.5f * baseScale

    val avatarY = y + strideOffset

    // 1. Radiant Aura (Active character only)
    if (isActive) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glowColor.copy(alpha = 0.55f), Color.Transparent),
                center = Offset(x, avatarY),
                radius = 42f * baseScale
            ),
            radius = 42f * baseScale,
            center = Offset(x, avatarY)
        )
    }

    // 2. Sleek Runner Body Capsule / Torso
    val bodyRadius = 14f * baseScale
    drawCircle(
        color = primaryColor.copy(alpha = alpha),
        radius = bodyRadius,
        center = Offset(x, avatarY - 8f)
    )

    // 3. Glowing Visor / Cyber Helmet
    val visorColor = if (isActive) Color.White else primaryColor.copy(alpha = alpha)
    drawRoundRect(
        color = visorColor,
        topLeft = Offset(x - 9f * baseScale, avatarY - 14f),
        size = Size(18f * baseScale, 7f * baseScale),
        cornerRadius = CornerRadius(3.5f, 3.5f)
    )

    // 4. Energetic Power Core (Arc Reactor)
    drawCircle(
        color = Color.White.copy(alpha = alpha),
        radius = 4f * baseScale,
        center = Offset(x, avatarY - 4f)
    )

    // 5. Stylized Cyber Limbs (Running stride cycle)
    val legStroke = 3.2f * baseScale
    val legColor = primaryColor.copy(alpha = alpha)

    // Left leg
    drawLine(
        color = legColor,
        start = Offset(x - 5f * baseScale, avatarY + 4f),
        end = Offset(x - 8f * baseScale, avatarY + 18f - strideOffset * 1.5f),
        strokeWidth = legStroke
    )
    // Right leg
    drawLine(
        color = legColor,
        start = Offset(x + 5f * baseScale, avatarY + 4f),
        end = Offset(x + 8f * baseScale, avatarY + 18f + strideOffset * 1.5f),
        strokeWidth = legStroke
    )

    // Subtle outline glow for active avatar
    if (isActive) {
        drawCircle(
            color = Color.White.copy(alpha = 0.75f),
            radius = bodyRadius + 1.5f,
            center = Offset(x, avatarY - 8f),
            style = Stroke(width = 1.2f)
        )
    }
}

// ---------------------------------------------------------------------------
// OBSTACLE RENDERING
// ---------------------------------------------------------------------------

private fun DrawScope.drawObstacle(
    obs: Obstacle,
    w: Float,
    h: Float,
    shakeX: Float,
    shakeY: Float
) {
    val obsCenterX = if (obs.side == ObstacleSide.LEFT) w * 0.25f else w * 0.75f
    val primaryColor = if (obs.side == ObstacleSide.LEFT) CyanNeon else MagentaNeon
    val accentColor = if (obs.side == ObstacleSide.LEFT) CyanAqua else PinkSoft

    val left = obsCenterX - obs.width * 0.5f + shakeX
    val top = obs.y + shakeY

    when (obs.type) {
        ObstacleType.ENERGY_WALL -> {
            // Tall pulsating electric forcefield barrier
            val wallBrush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.95f),
                    accentColor.copy(alpha = 0.75f),
                    primaryColor.copy(alpha = 0.95f)
                ),
                startY = top,
                endY = top + obs.height
            )
            drawRoundRect(
                brush = wallBrush,
                topLeft = Offset(left, top),
                size = Size(obs.width, obs.height),
                cornerRadius = CornerRadius(8f, 8f)
            )
            // Luminous frame border
            drawRoundRect(
                color = Color.White.copy(alpha = 0.85f),
                topLeft = Offset(left, top),
                size = Size(obs.width, obs.height),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(width = 2.2f)
            )
            // Internal energy lattice stripes
            val stripeCount = 4
            for (i in 1..stripeCount) {
                val stripeX = left + (obs.width / (stripeCount + 1)) * i
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = Offset(stripeX, top + 6f),
                    end = Offset(stripeX, top + obs.height - 6f),
                    strokeWidth = 1.8f
                )
            }
        }

        ObstacleType.LOW_BLOCK -> {
            // Cyber bunker obstacle with metallic beveled edge
            drawRoundRect(
                color = primaryColor.copy(alpha = 0.9f),
                topLeft = Offset(left, top),
                size = Size(obs.width, obs.height),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.75f),
                topLeft = Offset(left, top),
                size = Size(obs.width, obs.height),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 2f)
            )
            // Hazard Chevron diagonal accents
            drawLine(
                color = Color.White.copy(alpha = 0.85f),
                start = Offset(left + 12f, top + 8f),
                end = Offset(left + obs.width * 0.5f, top + obs.height - 8f),
                strokeWidth = 2.5f
            )
            drawLine(
                color = Color.White.copy(alpha = 0.85f),
                start = Offset(left + obs.width * 0.5f, top + obs.height - 8f),
                end = Offset(left + obs.width - 12f, top + 8f),
                strokeWidth = 2.5f
            )
        }

        ObstacleType.LASER_GATE -> {
            // Emitter pylons on both sides + sizzling laser beam
            val pylonWidth = 16f
            val pylonHeight = obs.height

            // Left Pylon
            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(left, top),
                size = Size(pylonWidth, pylonHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Right Pylon
            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(left + obs.width - pylonWidth, top),
                size = Size(pylonWidth, pylonHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Outer laser beam glow
            val laserMidY = top + pylonHeight * 0.5f
            drawLine(
                color = primaryColor.copy(alpha = 0.6f),
                start = Offset(left + pylonWidth, laserMidY),
                end = Offset(left + obs.width - pylonWidth, laserMidY),
                strokeWidth = 9f
            )
            // Intense core laser beam
            drawLine(
                color = LaserCore,
                start = Offset(left + pylonWidth, laserMidY),
                end = Offset(left + obs.width - pylonWidth, laserMidY),
                strokeWidth = 3.5f
            )
        }

        ObstacleType.SPIKE_ZONE -> {
            // Stylized crystalline hazard spikes
            val spikeCount = 4
            val spikeWidth = obs.width / spikeCount
            for (i in 0 until spikeCount) {
                val spikeLeft = left + i * spikeWidth
                val path = Path().apply {
                    moveTo(spikeLeft, top + obs.height)
                    lineTo(spikeLeft + spikeWidth * 0.5f, top)
                    lineTo(spikeLeft + spikeWidth, top + obs.height)
                    close()
                }
                drawPath(path = path, color = primaryColor.copy(alpha = 0.9f))
                drawPath(path = path, color = Color.White.copy(alpha = 0.75f), style = Stroke(width = 1.8f))
            }
        }

        ObstacleType.CLOSING_BARRIER -> {
            // Dual plates closing inward
            val halfObsW = obs.width * 0.5f
            val closeOffset = obs.closingProgress * (halfObsW * 0.42f)

            // Left plate
            drawRoundRect(
                color = primaryColor.copy(alpha = 0.95f),
                topLeft = Offset(left + closeOffset, top),
                size = Size(halfObsW * 0.85f, obs.height),
                cornerRadius = CornerRadius(5f, 5f)
            )
            // Right plate
            drawRoundRect(
                color = primaryColor.copy(alpha = 0.95f),
                topLeft = Offset(left + obs.width - (halfObsW * 0.85f) - closeOffset, top),
                size = Size(halfObsW * 0.85f, obs.height),
                cornerRadius = CornerRadius(5f, 5f)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// CENTER ENERGY SEAM RENDERING
// ---------------------------------------------------------------------------

private fun DrawScope.drawCenterSeam(
    engine: GameEngine,
    w: Float,
    h: Float,
    shakeX: Float,
    shakeY: Float
) {
    val midX = w / 2f + shakeX
    val pulse = engine.switchEnergyPulse

    // 1. Wide Seam Glow Corona (intensifies during side-switch)
    val glowWidth = 24f + pulse * 48f
    val glowAlpha = (0.28f + pulse * 0.55f).coerceAtMost(0.9f)

    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(
                CyanNeon.copy(alpha = glowAlpha * 0.45f),
                SeamCore.copy(alpha = glowAlpha),
                MagentaNeon.copy(alpha = glowAlpha * 0.45f)
            ),
            startX = midX - glowWidth,
            endX = midX + glowWidth
        ),
        topLeft = Offset(midX - glowWidth, shakeY),
        size = Size(glowWidth * 2f, h)
    )

    // 2. Shockwaves crossing the seam
    for (sw in engine.shockwaves) {
        val swRadius = sw.progress * (w * 0.42f)
        val swAlpha = (1f - sw.progress).coerceIn(0f, 1f)
        val shockColor = if (sw.fromSide == PlayerSide.ACTIVE_LEFT) CyanNeon else MagentaNeon

        drawCircle(
            color = shockColor.copy(alpha = swAlpha * 0.75f),
            radius = swRadius,
            center = Offset(midX, h * 0.72f + shakeY),
            style = Stroke(width = 4f * (1f - sw.progress) + 1.5f)
        )
    }

    // 3. Central Luminous Core Seam
    drawLine(
        brush = Brush.verticalGradient(
            colors = listOf(
                CyanNeon,
                SeamCore,
                MagentaNeon
            ),
            startY = 0f,
            endY = h
        ),
        start = Offset(midX, shakeY),
        end = Offset(midX, h + shakeY),
        strokeWidth = 2.8f + pulse * 3f
    )
}

// ---------------------------------------------------------------------------
// HUD COMPONENTS
// ---------------------------------------------------------------------------

@Composable
private fun GameTopHUD(
    score: Int,
    bestScore: Int,
    combo: Int,
    activeSide: PlayerSide,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ShadowSwitchTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Top-Left: Best Score Badge
        GlassCard(
            cornerRadius = 16.dp,
            modifier = Modifier.height(44.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = GoldTrophy,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$bestScore",
                    color = extendedColors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Top-Center: Score + Combo Badge
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = extendedColors.textPrimary
            )
            if (combo > 1) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(CyanNeon, MagentaNeon)
                            )
                        )
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "COMBO x$combo",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }
        }

        // Top-Right: Pause Button
        NeonIconButton(
            icon = Icons.Default.Pause,
            onClick = onPauseClick,
            size = 44.dp,
            iconSize = 20.dp,
            contentDescription = "Pause"
        )
    }
}

// ---------------------------------------------------------------------------
// FLOATING NOTICES OVERLAY
// ---------------------------------------------------------------------------

@Composable
private fun FloatingNoticesOverlay(engine: GameEngine) {
    Box(modifier = Modifier.fillMaxSize()) {
        for (notice in engine.floatingNotices) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = (notice.x - 100f).coerceAtLeast(0f).dp,
                        top = (notice.y).coerceAtLeast(0f).dp
                    )
                    .alpha(notice.alpha)
            ) {
                Text(
                    text = notice.text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = notice.color
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// COUNTDOWN OVERLAY
// ---------------------------------------------------------------------------

@Composable
private fun CountdownOverlay(countdownText: String) {
    val extendedColors = ShadowSwitchTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = countdownText,
                fontSize = if (countdownText == "SWITCH!") 44.sp else 64.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = if (countdownText == "SWITCH!") CyanNeon else extendedColors.textPrimary
            )
        }
    }
}

// ---------------------------------------------------------------------------
// TUTORIAL HINT OVERLAY
// ---------------------------------------------------------------------------

@Composable
private fun TutorialHintOverlay(
    activeSide: PlayerSide,
    onDismiss: () -> Unit
) {
    val extendedColors = ShadowSwitchTheme.colors

    val infiniteTransition = rememberInfiniteTransition(label = "TutorialPulse")
    val tapPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TapPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 120.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        GlassCard(
            modifier = Modifier.padding(horizontal = 32.dp),
            cornerRadius = 24.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = CyanNeon,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "TAP TO SWITCH WORLDS",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp,
                    color = extendedColors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Avoid obstacles on the active side",
                    fontSize = 12.sp,
                    color = extendedColors.textSecondary
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// PAUSE MENU DIALOG
// ---------------------------------------------------------------------------

@Composable
private fun PauseMenuDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    val extendedColors = ShadowSwitchTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.70f)),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            cornerRadius = 28.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PAUSED",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = extendedColors.textPrimary
                )

                Spacer(modifier = Modifier.height(28.dp))

                NeonButton(
                    text = "RESUME",
                    onClick = onResume,
                    icon = Icons.Default.PlayArrow,
                    style = NeonButtonStyle.SPLIT_GRADIENT,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                NeonButton(
                    text = "RESTART",
                    onClick = onRestart,
                    icon = Icons.Default.Refresh,
                    style = NeonButtonStyle.GHOST,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                NeonButton(
                    text = "HOME",
                    onClick = onHome,
                    style = NeonButtonStyle.GHOST,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
