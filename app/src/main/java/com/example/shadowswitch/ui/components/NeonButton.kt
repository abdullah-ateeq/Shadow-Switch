package com.example.shadowswitch.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shadowswitch.ui.theme.CyanBright
import com.example.shadowswitch.ui.theme.CyanNeon
import com.example.shadowswitch.ui.theme.MagentaNeon
import com.example.shadowswitch.ui.theme.ShadowSwitchTheme

enum class NeonButtonStyle {
    CYAN_PRIMARY,
    MAGENTA_PRIMARY,
    SPLIT_GRADIENT,
    GHOST
}

@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: NeonButtonStyle = NeonButtonStyle.SPLIT_GRADIENT,
    icon: ImageVector? = null,
    height: Dp = 56.dp,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ButtonScale"
    )

    val shape = RoundedCornerShape(16.dp)
    val extendedColors = ShadowSwitchTheme.colors

    val backgroundBrush = when (style) {
        NeonButtonStyle.CYAN_PRIMARY -> Brush.horizontalGradient(
            listOf(extendedColors.leftPrimary, CyanBright)
        )
        NeonButtonStyle.MAGENTA_PRIMARY -> Brush.horizontalGradient(
            listOf(extendedColors.rightPrimary, Color(0xFFFF4081))
        )
        NeonButtonStyle.SPLIT_GRADIENT -> Brush.horizontalGradient(
            listOf(extendedColors.leftPrimary, extendedColors.rightPrimary)
        )
        NeonButtonStyle.GHOST -> Brush.horizontalGradient(
            listOf(Color.Transparent, Color.Transparent)
        )
    }

    val contentColor = when (style) {
        NeonButtonStyle.GHOST -> extendedColors.textPrimary
        NeonButtonStyle.CYAN_PRIMARY -> Color(0xFF04101A)
        NeonButtonStyle.MAGENTA_PRIMARY -> Color.White
        NeonButtonStyle.SPLIT_GRADIENT -> Color(0xFF070712)
    }

    val borderModifier = if (style == NeonButtonStyle.GHOST) {
        Modifier.border(
            1.5.dp,
            Brush.horizontalGradient(
                listOf(extendedColors.leftPrimary.copy(alpha = 0.6f), extendedColors.rightPrimary.copy(alpha = 0.6f))
            ),
            shape
        )
    } else {
        Modifier.border(1.dp, Color.White.copy(alpha = 0.35f), shape)
    }

    Box(
        modifier = modifier
            .scale(scale)
            .height(height)
            .clip(shape)
            .then(borderModifier)
            .background(brush = backgroundBrush)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = text,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                letterSpacing = 1.sp
            )
        }
    }
}
