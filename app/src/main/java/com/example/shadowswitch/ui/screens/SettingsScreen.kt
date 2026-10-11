package com.example.shadowswitch.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shadowswitch.ui.components.AnimatedSplitBackground
import com.example.shadowswitch.ui.components.ConfirmDialog
import com.example.shadowswitch.ui.components.GlassCard
import com.example.shadowswitch.ui.components.NeonButton
import com.example.shadowswitch.ui.components.NeonButtonStyle
import com.example.shadowswitch.ui.components.NeonIconButton
import com.example.shadowswitch.ui.components.SettingToggleRow
import com.example.shadowswitch.ui.navigation.Screen
import com.example.shadowswitch.ui.theme.CyanNeon
import com.example.shadowswitch.ui.theme.DangerRed
import com.example.shadowswitch.ui.theme.ShadowSwitchTheme
import com.example.shadowswitch.ui.theme.ThemeMode
import com.example.shadowswitch.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ShadowSwitchTheme.colors
    val prefs by viewModel.preferences.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }

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
                    text = "SETTINGS",
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
                // Theme Mode Selector
                ThemeSelectorCard(
                    currentTheme = prefs.themeMode,
                    onThemeSelected = { viewModel.setThemeMode(it) }
                )

                // Sound Toggle
                SettingToggleRow(
                    title = "Sound Effects",
                    description = "Procedural audio cues for switches, combos, and collisions",
                    checked = prefs.soundEnabled,
                    onCheckedChange = { viewModel.setSoundEnabled(it) }
                )

                // Haptics Toggle
                SettingToggleRow(
                    title = "Haptic Feedback",
                    description = "Vibrations on switches, impacts, and menu interactions",
                    checked = prefs.hapticsEnabled,
                    onCheckedChange = { viewModel.setHapticsEnabled(it) }
                )

                // Particles Toggle
                SettingToggleRow(
                    title = "Particle Effects",
                    description = "Runner dust wakes, switch shockwaves, and shatter bursts",
                    checked = prefs.particlesEnabled,
                    onCheckedChange = { viewModel.setParticlesEnabled(it) }
                )

                // Reduced Effects Toggle
                SettingToggleRow(
                    title = "Reduced Effects",
                    description = "Lowers camera shake and flashes for visual comfort",
                    checked = prefs.reducedEffectsEnabled,
                    onCheckedChange = { viewModel.setReducedEffectsEnabled(it) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Reset High Score Button
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    borderColor = DangerRed.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Reset Best Score",
                                color = extendedColors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Current record: ${prefs.bestScore}",
                                color = extendedColors.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                        NeonButton(
                            text = "RESET",
                            onClick = { showResetDialog = true },
                            style = NeonButtonStyle.MAGENTA_PRIMARY,
                            height = 42.dp
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
                    currentScreen = Screen.SETTINGS,
                    onNavigate = onNavigate
                )
            }
        }

        if (showResetDialog) {
            ConfirmDialog(
                title = "Reset Best Score?",
                message = "Are you sure you want to reset your high score? This action cannot be undone.",
                confirmText = "RESET",
                cancelText = "CANCEL",
                onConfirm = {
                    viewModel.resetBestScore()
                    showResetDialog = false
                },
                onDismiss = {
                    showResetDialog = false
                }
            )
        }
    }
}

@Composable
private fun ThemeSelectorCard(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    val extendedColors = ShadowSwitchTheme.colors

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = CyanNeon,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Theme Appearance",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = extendedColors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOptionChip(
                    label = "Dark",
                    isSelected = currentTheme == ThemeMode.DARK,
                    onClick = { onThemeSelected(ThemeMode.DARK) },
                    modifier = Modifier.weight(1f)
                )
                ThemeOptionChip(
                    label = "Light",
                    isSelected = currentTheme == ThemeMode.LIGHT,
                    onClick = { onThemeSelected(ThemeMode.LIGHT) },
                    modifier = Modifier.weight(1f)
                )
                ThemeOptionChip(
                    label = "System",
                    isSelected = currentTheme == ThemeMode.SYSTEM,
                    onClick = { onThemeSelected(ThemeMode.SYSTEM) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = ShadowSwitchTheme.colors

    val bg = if (isSelected) extendedColors.leftPrimary else extendedColors.glassBg
    val textColor = if (isSelected) Color.Black else extendedColors.textSecondary
    val border = if (isSelected) extendedColors.leftPrimary else extendedColors.glassBorder

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}
