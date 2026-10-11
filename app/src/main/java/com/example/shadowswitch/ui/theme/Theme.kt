package com.example.shadowswitch.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode {
    DARK,
    LIGHT,
    SYSTEM
}

@Immutable
data class ExtendedColors(
    val isDark: Boolean,
    val leftPrimary: Color,
    val leftGlow: Color,
    val leftDeep: Color,
    val rightPrimary: Color,
    val rightGlow: Color,
    val rightDeep: Color,
    val seamColor: Color,
    val glassBg: Color,
    val glassBorder: Color,
    val surfaceElevated: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val gold: Color,
    val perfect: Color,
    val danger: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        isDark = true,
        leftPrimary = CyanNeon,
        leftGlow = CyanGlow,
        leftDeep = NavyDark,
        rightPrimary = MagentaNeon,
        rightGlow = MagentaGlow,
        rightDeep = PurpleDark,
        seamColor = SeamCore,
        glassBg = GlassSurface,
        glassBorder = GlassBorder,
        surfaceElevated = DarkSurfaceElevated,
        textPrimary = TextPrimaryDark,
        textSecondary = TextSecondaryDark,
        textTertiary = TextTertiaryDark,
        gold = GoldTrophy,
        perfect = PerfectGreen,
        danger = DangerRed
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = CyanNeon,
    onPrimary = Color.Black,
    secondary = MagentaNeon,
    onSecondary = Color.White,
    tertiary = VioletNeon,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = GlassBorder
)

private val LightColorScheme = lightColorScheme(
    primary = CyanAqua,
    onPrimary = Color.Black,
    secondary = MagentaNeon,
    onSecondary = Color.White,
    tertiary = VioletNeon,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = TextSecondaryLight,
    outline = GlassBorderLight
)

@Composable
fun ShadowSwitchTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    val extendedColors = if (isDark) {
        ExtendedColors(
            isDark = true,
            leftPrimary = CyanNeon,
            leftGlow = CyanGlow,
            leftDeep = NavyDark,
            rightPrimary = MagentaNeon,
            rightGlow = MagentaGlow,
            rightDeep = PurpleDark,
            seamColor = SeamCore,
            glassBg = GlassSurface,
            glassBorder = GlassBorder,
            surfaceElevated = DarkSurfaceElevated,
            textPrimary = TextPrimaryDark,
            textSecondary = TextSecondaryDark,
            textTertiary = TextTertiaryDark,
            gold = GoldTrophy,
            perfect = PerfectGreen,
            danger = DangerRed
        )
    } else {
        ExtendedColors(
            isDark = false,
            leftPrimary = CyanAqua,
            leftGlow = CyanGlowSoft,
            leftDeep = LightLeftBg,
            rightPrimary = MagentaNeon,
            rightGlow = MagentaGlowSoft,
            rightDeep = LightRightBg,
            seamColor = CyanNeon,
            glassBg = GlassSurfaceLight,
            glassBorder = GlassBorderLight,
            surfaceElevated = LightSurfaceElevated,
            textPrimary = TextPrimaryLight,
            textSecondary = TextSecondaryLight,
            textTertiary = TextTertiaryLight,
            gold = GoldTrophy,
            perfect = PerfectGreen,
            danger = DangerRed
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !isDark
                controller.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object ShadowSwitchTheme {
    val colors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}