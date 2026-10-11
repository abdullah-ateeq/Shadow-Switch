package com.example.shadowswitch.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.shadowswitch.ui.theme.ShadowSwitchTheme

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "CONFIRM",
    cancelText: String = "CANCEL",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val extendedColors = ShadowSwitchTheme.colors

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = title,
                    color = extendedColors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    color = extendedColors.textSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    NeonButton(
                        text = cancelText,
                        onClick = onDismiss,
                        style = NeonButtonStyle.GHOST,
                        height = 44.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    NeonButton(
                        text = confirmText,
                        onClick = onConfirm,
                        style = NeonButtonStyle.MAGENTA_PRIMARY,
                        height = 44.dp
                    )
                }
            }
        }
    }
}
