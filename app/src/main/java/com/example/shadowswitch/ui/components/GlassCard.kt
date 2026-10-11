package com.example.shadowswitch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.shadowswitch.ui.theme.ShadowSwitchTheme

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color? = null,
    backgroundColor: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val extendedColors = ShadowSwitchTheme.colors
    val shape = RoundedCornerShape(cornerRadius)

    val actualBg = backgroundColor ?: extendedColors.glassBg
    val actualBorder = borderColor ?: extendedColors.glassBorder

    Box(
        modifier = modifier
            .clip(shape)
            .background(actualBg)
            .border(borderWidth, actualBorder, shape)
            .padding(16.dp),
        content = content
    )
}

@Composable
fun NeonBorderGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    isLeftFocused: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val extendedColors = ShadowSwitchTheme.colors
    val shape = RoundedCornerShape(cornerRadius)

    val borderBrush = Brush.horizontalGradient(
        colors = listOf(
            extendedColors.leftPrimary.copy(alpha = if (isLeftFocused) 0.8f else 0.3f),
            extendedColors.rightPrimary.copy(alpha = if (!isLeftFocused) 0.8f else 0.3f)
        )
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(extendedColors.glassBg)
            .border(1.5.dp, borderBrush, shape)
            .padding(20.dp),
        content = content
    )
}
