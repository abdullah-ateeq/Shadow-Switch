package com.example.shadowswitch.game

import androidx.compose.ui.graphics.Color

enum class PlayerSide {
    ACTIVE_LEFT,
    ACTIVE_RIGHT;

    fun toggle(): PlayerSide = if (this == ACTIVE_LEFT) ACTIVE_RIGHT else ACTIVE_LEFT
}

enum class ObstacleSide {
    LEFT,
    RIGHT
}

enum class ObstacleType {
    ENERGY_WALL,    // Tall vertical energetic forcefield
    LOW_BLOCK,      // Cyber bunker block with hazard chevrons
    LASER_GATE,     // High-intensity horizontal laser with emitter pylons
    SPIKE_ZONE,     // Crystalline energy spikes
    CLOSING_BARRIER // Dual plates closing inward
}

data class Obstacle(
    val id: Long,
    val side: ObstacleSide,
    val type: ObstacleType,
    var y: Float, // Top Y position in px
    val width: Float, // Width in px
    val height: Float, // Height in px
    var cleared: Boolean = false,
    var phasePassTriggered: Boolean = false,
    var closingProgress: Float = 0f // 0f to 1f for closing barrier animation
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var alpha: Float,
    val maxLife: Float,
    var life: Float,
    val color: Color
)

data class PhaseRipple(
    val x: Float,
    val y: Float,
    var currentRadius: Float,
    val maxRadius: Float,
    var alpha: Float,
    val color: Color
)

data class Shockwave(
    val fromSide: PlayerSide,
    var progress: Float, // 0f to 1f
    val durationMs: Float = 180f
)

data class FloatingNotice(
    val id: Long,
    val text: String,
    val x: Float,
    var y: Float,
    var alpha: Float,
    var scale: Float,
    val color: Color,
    val lifeMs: Float = 700f,
    var elapsedMs: Float = 0f
)

enum class GameStatus {
    READY,
    COUNTDOWN,
    PLAYING,
    PAUSED,
    GAME_OVER
}

data class GameOverAnimationState(
    var elapsedMs: Float = 0f,
    val totalDurationMs: Float = 850f,
    var isFreezeDone: Boolean = false,
    var shakeIntensity: Float = 14f,
    var flashAlpha: Float = 1f,
    var shadowAlpha: Float = 0.4f
)
