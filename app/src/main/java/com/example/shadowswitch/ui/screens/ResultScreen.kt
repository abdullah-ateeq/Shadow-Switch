package com.example.shadowswitch.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shadowswitch.ui.components.AnimatedSplitBackground
import com.example.shadowswitch.ui.components.GlassCard
import com.example.shadowswitch.ui.components.NeonButton
import com.example.shadowswitch.ui.components.NeonButtonStyle
import com.example.shadowswitch.ui.theme.CyanNeon
import com.example.shadowswitch.ui.theme.GoldTrophy
import com.example.shadowswitch.ui.theme.MagentaNeon
import com.example.shadowswitch.ui.theme.ShadowSwitchTheme
import com.example.shadowswitch.viewmodel.RunSummary

@Composable
fun ResultScreen(
    runSummary: RunSummary,
    onPlayAgainClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ShadowSwitchTheme.colors
    val context = LocalContext.current

    val scoreAnim = remember { Animatable(0f) }

    LaunchedEffect(runSummary.score) {
        scoreAnim.animateTo(
            targetValue = runSummary.score.toFloat(),
            animationSpec = tween(900, easing = FastOutSlowInEasing)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "NewBestPulse")
    val badgeScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BadgeScale"
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AnimatedSplitBackground(activeSideProgress = 0.5f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "RUN COMPLETE",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = extendedColors.textPrimary
                )

                if (runSummary.isNewBest) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .scale(badgeScale)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(GoldTrophy, Color(0xFFFF9E00))
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "NEW BEST SCORE!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            // Score Card & Stats
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Large Score
                Text(
                    text = "${scoreAnim.value.toInt()}",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Black,
                    color = extendedColors.textPrimary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = GoldTrophy,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BEST: ${runSummary.bestScore}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Stats Grid Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(
                            label = "SWITCHES",
                            value = "${runSummary.switches}",
                            color = CyanNeon
                        )
                        StatItem(
                            label = "PERFECT",
                            value = "${runSummary.perfectSwitches}",
                            color = GoldTrophy
                        )
                        StatItem(
                            label = "CLEARED",
                            value = "${runSummary.obstaclesCleared}",
                            color = MagentaNeon
                        )
                    }
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NeonButton(
                    text = "PLAY AGAIN",
                    onClick = onPlayAgainClick,
                    icon = Icons.Default.Replay,
                    style = NeonButtonStyle.SPLIT_GRADIENT,
                    height = 58.dp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    NeonButton(
                        text = "HOME",
                        onClick = onHomeClick,
                        icon = Icons.Default.Home,
                        style = NeonButtonStyle.GHOST,
                        height = 50.dp,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    NeonButton(
                        text = "SHARE",
                        onClick = { shareScore(context, runSummary.score) },
                        icon = Icons.Default.Share,
                        style = NeonButtonStyle.GHOST,
                        height = 50.dp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    val extendedColors = ShadowSwitchTheme.colors

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = extendedColors.textTertiary
        )
    }
}

private fun shareScore(context: Context, score: Int) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            "I survived two mirrored worlds and scored $score in Shadow Switch! Can you beat it?"
        )
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Score")
    context.startActivity(shareIntent)
}
