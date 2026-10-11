package com.example.shadowswitch.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

enum class HapticType {
    SWITCH,
    PERFECT_SWITCH,
    COLLISION,
    BUTTON_TAP
}

class HapticFeedbackManager(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var isEnabled: Boolean = true

    fun trigger(type: HapticType) {
        if (!isEnabled || vibrator == null || !vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                when (type) {
                    HapticType.SWITCH -> {
                        // Snappy, subtle tick
                        vibrator.vibrate(
                            VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                        )
                    }
                    HapticType.BUTTON_TAP -> {
                        vibrator.vibrate(
                            VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                        )
                    }
                    HapticType.PERFECT_SWITCH -> {
                        // Double impulse
                        vibrator.vibrate(
                            VibrationEffect.createWaveform(
                                longArrayOf(0, 20, 40, 30),
                                intArrayOf(0, 180, 0, 240),
                                -1
                            )
                        )
                    }
                    HapticType.COLLISION -> {
                        // Heavy collision rumble
                        vibrator.vibrate(
                            VibrationEffect.createWaveform(
                                longArrayOf(0, 60, 30, 90),
                                intArrayOf(0, 255, 0, 200),
                                -1
                            )
                        )
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                when (type) {
                    HapticType.SWITCH, HapticType.BUTTON_TAP -> vibrator.vibrate(15)
                    HapticType.PERFECT_SWITCH -> vibrator.vibrate(longArrayOf(0, 20, 30, 30), -1)
                    HapticType.COLLISION -> vibrator.vibrate(longArrayOf(0, 60, 30, 90), -1)
                }
            }
        } catch (_: Exception) {
            // Graceful fallback if device restricts vibration
        }
    }
}
