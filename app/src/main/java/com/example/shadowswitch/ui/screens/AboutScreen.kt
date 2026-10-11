package com.example.shadowswitch.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shadowswitch.ui.components.AnimatedSplitBackground
import com.example.shadowswitch.ui.components.GlassCard
import com.example.shadowswitch.ui.components.NeonIconButton
import com.example.shadowswitch.ui.navigation.Screen
import com.example.shadowswitch.ui.theme.CyanNeon
import com.example.shadowswitch.ui.theme.MagentaNeon
import com.example.shadowswitch.ui.theme.ShadowSwitchTheme

@Composable
fun AboutScreen(
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ShadowSwitchTheme.colors

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedSplitBackground(activeSideProgress = 0.5f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = { onNavigate(Screen.HOME) },
                    contentDescription = "Back"
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "ABOUT",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = extendedColors.textPrimary
                )
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Brand Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SHADOW SWITCH",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = extendedColors.textPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Control two mirrored runners and switch worlds at the perfect moment to survive.",
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = extendedColors.textSecondary
                        )
                    }
                }

                // How to Play Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "HOW TO PLAY",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = extendedColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        RuleItem(number = "1", text = "Both runners move forward automatically.")
                        RuleItem(number = "2", text = "Only one world is active at a time.")
                        RuleItem(number = "3", text = "Tap anywhere to switch which side is active.")
                        RuleItem(number = "4", text = "The inactive shadow passes through obstacles safely.")
                        RuleItem(number = "5", text = "Avoid obstacles on the active side to survive!")
                    }
                }

                // Offline & Privacy Notice Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.OfflineBolt,
                                contentDescription = null,
                                tint = MagentaNeon,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "OFFLINE & PRIVATE",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = extendedColors.textPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Shadow Switch works fully offline and does not require an account. No tracking, no data collection, no background internet usage.",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = extendedColors.textSecondary
                        )
                    }
                }

                // Version & Build Info
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Version",
                            fontSize = 14.sp,
                            color = extendedColors.textSecondary
                        )
                        Text(
                            text = "1.0.0 (Release)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bottom Navigation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                BottomNavBar(
                    currentScreen = Screen.ABOUT,
                    onNavigate = onNavigate
                )
            }
        }
    }
}

@Composable
private fun RuleItem(number: String, text: String) {
    val extendedColors = ShadowSwitchTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$number.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = CyanNeon,
            modifier = Modifier.width(22.dp)
        )
        Text(
            text = text,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = extendedColors.textSecondary
        )
    }
}
