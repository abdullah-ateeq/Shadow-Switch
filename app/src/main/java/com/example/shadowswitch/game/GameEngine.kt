package com.example.shadowswitch.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.example.shadowswitch.ui.theme.CyanBright
import com.example.shadowswitch.ui.theme.CyanNeon
import com.example.shadowswitch.ui.theme.MagentaNeon
import com.example.shadowswitch.ui.theme.PinkSoft
import com.example.shadowswitch.ui.theme.SeamCore
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class GameEngine(
    var particlesEnabled: Boolean = true,
    var reducedEffects: Boolean = false,
    private val onSwitch: (isPerfect: Boolean) -> Unit = {},
    private val onObstacleCleared: () -> Unit = {},
    private val onCollision: () -> Unit = {},
    private val onMilestone: () -> Unit = {},
    private val onGameOverFinished: () -> Unit = {}
) {
    var status: GameStatus by mutableStateOf(GameStatus.READY)
        private set

    var activeSide: PlayerSide by mutableStateOf(PlayerSide.ACTIVE_LEFT)
        private set

    var score: Int by mutableIntStateOf(0)
        private set

    var bestScore: Int by mutableIntStateOf(0)

    var combo: Int by mutableIntStateOf(1)
        private set

    var maxCombo: Int by mutableIntStateOf(1)
        private set

    var switchesCount: Int by mutableIntStateOf(0)
        private set

    var perfectSwitchesCount: Int by mutableIntStateOf(0)
        private set

    var obstaclesClearedCount: Int by mutableIntStateOf(0)
        private set

    var countdownText: String by mutableStateOf("3")
        private set

    var frameTick: Long by mutableLongStateOf(0L)
        private set

    private var countdownTimerMs: Float = 0f

    var worldSpeed: Float = 480f // px per sec
        private set

    var activeCharacterScale: Float = 1f
        private set

    var switchEnergyPulse: Float = 0f
        private set

    var perspectiveGridOffset: Float = 0f
        private set

    var runStrideCycle: Float = 0f
        private set

    var shakeOffsetX: Float = 0f
        private set
    var shakeOffsetY: Float = 0f
        private set

    val obstacles = mutableListOf<Obstacle>()
    val particles = mutableListOf<Particle>()
    val phaseRipples = mutableListOf<PhaseRipple>()
    val shockwaves = mutableListOf<Shockwave>()
    val floatingNotices = mutableListOf<FloatingNotice>()

    var gameOverState: GameOverAnimationState? = null
        private set

    private val obstacleGenerator = ObstacleGenerator()
    private val random = Random(System.currentTimeMillis())
    private var survivalScoreTimer: Float = 0f
    private var lastMilestoneScore: Int = 0
    private var floatingNoticeId = 0L

    fun startCountdown() {
        status = GameStatus.COUNTDOWN
        countdownTimerMs = 0f
        countdownText = "3"
        resetGameStats()
    }

    fun startPlayingImmediately() {
        status = GameStatus.PLAYING
        resetGameStats()
    }

    private fun resetGameStats() {
        score = 0
        combo = 1
        maxCombo = 1
        switchesCount = 0
        perfectSwitchesCount = 0
        obstaclesClearedCount = 0
        survivalScoreTimer = 0f
        lastMilestoneScore = 0
        activeSide = PlayerSide.ACTIVE_LEFT
        activeCharacterScale = 1f
        switchEnergyPulse = 0f
        shakeOffsetX = 0f
        shakeOffsetY = 0f
        gameOverState = null
        obstacles.clear()
        particles.clear()
        phaseRipples.clear()
        shockwaves.clear()
        floatingNotices.clear()
        obstacleGenerator.reset()
    }

    fun pause() {
        if (status == GameStatus.PLAYING) {
            status = GameStatus.PAUSED
        }
    }

    fun resume() {
        if (status == GameStatus.PAUSED) {
            status = GameStatus.PLAYING
        }
    }

    fun restart() {
        startCountdown()
    }

    fun handleTap(screenWidth: Float, screenHeight: Float) {
        when (status) {
            GameStatus.READY -> {
                startCountdown()
            }
            GameStatus.COUNTDOWN -> {
                // Ignore taps during countdown
            }
            GameStatus.PLAYING -> {
                performSwitch(screenWidth, screenHeight)
            }
            GameStatus.PAUSED, GameStatus.GAME_OVER -> {
                // Ignore tap on playfield
            }
        }
    }

    private fun performSwitch(screenWidth: Float, screenHeight: Float) {
        val oldSide = activeSide
        val newSide = oldSide.toggle()

        val characterY = screenHeight * 0.72f
        val charWidth = 46f
        val charHeight = 56f
        val charX = if (oldSide == PlayerSide.ACTIVE_LEFT) screenWidth * 0.25f else screenWidth * 0.75f

        // Check if any obstacle on the old active side was dangerously close (Critical reaction window)
        // Obstacle Y is top of obstacle. Front of obstacle reaching character is `obstacle.y + obstacle.height`.
        var isPerfect = false
        val checkRangeMin = -15f
        val checkRangeMax = 165f

        for (obs in obstacles) {
            val obsSideAsPlayerSide = if (obs.side == ObstacleSide.LEFT) PlayerSide.ACTIVE_LEFT else PlayerSide.ACTIVE_RIGHT
            if (obsSideAsPlayerSide == oldSide && !obs.cleared) {
                val distanceToPlayer = characterY - (obs.y + obs.height)
                if (distanceToPlayer in checkRangeMin..checkRangeMax) {
                    isPerfect = true
                    break
                }
            }
        }

        // Instant side switch!
        activeSide = newSide
        switchesCount++
        activeCharacterScale = 1.22f
        switchEnergyPulse = 1f

        shockwaves.add(
            Shockwave(
                fromSide = oldSide,
                progress = 0f
            )
        )

        // Spawn switch burst particles
        if (particlesEnabled) {
            val burstColor = if (newSide == PlayerSide.ACTIVE_LEFT) CyanNeon else MagentaNeon
            val targetX = if (newSide == PlayerSide.ACTIVE_LEFT) screenWidth * 0.25f else screenWidth * 0.75f
            val count = if (reducedEffects) 6 else 14
            for (i in 0 until count) {
                val angle = random.nextFloat() * 2f * PI.toFloat()
                val speed = random.nextFloat() * 180f + 60f
                particles.add(
                    Particle(
                        x = targetX,
                        y = characterY,
                        vx = cos(angle) * speed,
                        vy = sin(angle) * speed,
                        size = random.nextFloat() * 5f + 3f,
                        alpha = 1f,
                        maxLife = 0.35f,
                        life = 0.35f,
                        color = burstColor
                    )
                )
            }
        }

        if (isPerfect) {
            combo++
            if (combo > maxCombo) maxCombo = combo
            score += 1
            perfectSwitchesCount++

            val noticeX = if (newSide == PlayerSide.ACTIVE_LEFT) screenWidth * 0.25f else screenWidth * 0.75f
            floatingNotices.add(
                FloatingNotice(
                    id = floatingNoticeId++,
                    text = if (combo > 1) "PERFECT x$combo!" else "PERFECT SWITCH!",
                    x = noticeX,
                    y = characterY - 70f,
                    alpha = 1f,
                    scale = 1.25f,
                    color = if (newSide == PlayerSide.ACTIVE_LEFT) CyanBright else PinkSoft
                )
            )
            onSwitch(true)
        } else {
            // Normal switch
            onSwitch(false)
        }
    }

    fun update(deltaSeconds: Float, screenWidth: Float, screenHeight: Float) {
        val dt = deltaSeconds.coerceIn(0.001f, 0.05f)

        when (status) {
            GameStatus.READY -> {
                // Ambient idle movement
                runStrideCycle = (runStrideCycle + dt * 6f) % (2f * PI.toFloat())
                perspectiveGridOffset = (perspectiveGridOffset + dt * 0.4f) % 1f
                updateAmbientParticles(dt, screenWidth, screenHeight)
            }
            GameStatus.COUNTDOWN -> {
                updateCountdown(dt)
            }
            GameStatus.PLAYING -> {
                updatePlaying(dt, screenWidth, screenHeight)
            }
            GameStatus.PAUSED -> {
                // Loop frozen
            }
            GameStatus.GAME_OVER -> {
                updateGameOver(dt, screenWidth, screenHeight)
            }
        }

        frameTick++
    }

    private fun updateCountdown(dt: Float) {
        countdownTimerMs += dt * 1000f
        when {
            countdownTimerMs < 600f -> countdownText = "3"
            countdownTimerMs < 1200f -> countdownText = "2"
            countdownTimerMs < 1800f -> countdownText = "1"
            countdownTimerMs < 2300f -> countdownText = "SWITCH!"
            else -> {
                status = GameStatus.PLAYING
            }
        }
    }

    private fun updatePlaying(dt: Float, screenWidth: Float, screenHeight: Float) {
        // Dynamic speed based on difficulty curve
        val baseSpeed = 460f
        val addedSpeed = (score * 3.2f).coerceAtMost(360f)
        worldSpeed = baseSpeed + addedSpeed

        // Update perspective & runner stride cycle
        perspectiveGridOffset = (perspectiveGridOffset + (worldSpeed / 400f) * dt) % 1f
        runStrideCycle = (runStrideCycle + (worldSpeed / 75f) * dt) % (2f * PI.toFloat())

        // Recovery springs for switch animations
        if (activeCharacterScale > 1f) {
            activeCharacterScale = (activeCharacterScale - dt * 1.5f).coerceAtLeast(1f)
        }
        if (switchEnergyPulse > 0f) {
            switchEnergyPulse = (switchEnergyPulse - dt * 4.5f).coerceAtLeast(0f)
        }

        // Periodic survival score (+1 every ~1.3s of survival)
        survivalScoreTimer += dt
        if (survivalScoreTimer >= 1.25f) {
            survivalScoreTimer = 0f
            score += 1
            checkMilestone()
        }

        val characterY = screenHeight * 0.72f
        val charWidth = 44f
        val charHeight = 56f
        val leftCharX = screenWidth * 0.25f
        val rightCharX = screenWidth * 0.75f
        val activeCharX = if (activeSide == PlayerSide.ACTIVE_LEFT) leftCharX else rightCharX
        val shadowCharX = if (activeSide == PlayerSide.ACTIVE_LEFT) rightCharX else leftCharX

        val activeCharRect = Rect(
            left = activeCharX - charWidth * 0.5f + 7f, // 7f forgiving margin
            top = characterY - charHeight * 0.5f + 7f,
            right = activeCharX + charWidth * 0.5f - 7f,
            bottom = characterY + charHeight * 0.5f - 7f
        )

        val laneWidth = screenWidth * 0.5f

        // Update obstacles
        val deltaDistance = worldSpeed * dt
        val iterator = obstacles.iterator()

        while (iterator.hasNext()) {
            val obs = iterator.next()
            obs.y += deltaDistance

            // Animate closing barrier
            if (obs.type == ObstacleType.CLOSING_BARRIER) {
                val dist = (characterY - obs.y).coerceAtLeast(0f)
                obs.closingProgress = (1f - (dist / (screenHeight * 0.6f))).coerceIn(0f, 1f)
            }

            val obsCenterX = if (obs.side == ObstacleSide.LEFT) screenWidth * 0.25f else screenWidth * 0.75f
            val obsRect = Rect(
                left = obsCenterX - obs.width * 0.5f,
                top = obs.y,
                right = obsCenterX + obs.width * 0.5f,
                bottom = obs.y + obs.height
            )

            val obsSideAsPlayerSide = if (obs.side == ObstacleSide.LEFT) PlayerSide.ACTIVE_LEFT else PlayerSide.ACTIVE_RIGHT

            // 1. COLLISION DETECTION: ONLY for Active character + Obstacle on active side!
            if (obsSideAsPlayerSide == activeSide) {
                if (activeCharRect.overlaps(obsRect)) {
                    // Collision!
                    triggerGameOver(activeCharX, characterY, shadowCharX)
                    return
                }
            } else {
                // 2. SHADOW PHASE PASS: Shadow character safely passes through obstacle
                if (!obs.phasePassTriggered && (obs.y + obs.height >= characterY - 20f)) {
                    obs.phasePassTriggered = true
                    // Add phase distortion ripple!
                    phaseRipples.add(
                        PhaseRipple(
                            x = shadowCharX,
                            y = characterY,
                            currentRadius = 10f,
                            maxRadius = 60f,
                            alpha = 0.85f,
                            color = if (activeSide == PlayerSide.ACTIVE_LEFT) MagentaNeon else CyanNeon
                        )
                    )
                }
            }

            // 3. CLEARANCE CHECK: when obstacle successfully passes player Y
            if (!obs.cleared && obs.y > characterY + charHeight * 0.5f) {
                obs.cleared = true
                obstaclesClearedCount++
                score += 2
                checkMilestone()
                onObstacleCleared()
            }

            // Remove offscreen
            if (obs.y > screenHeight + 90f) {
                iterator.remove()
            }
        }

        // Procedural obstacle generation
        obstacleGenerator.update(
            deltaDistance = deltaDistance,
            score = score,
            laneWidth = laneWidth,
            currentSpeed = worldSpeed
        ) { newObs ->
            obstacles.add(newObs)
        }

        // Update shockwaves
        val shockIter = shockwaves.iterator()
        while (shockIter.hasNext()) {
            val sw = shockIter.next()
            sw.progress += (dt * 1000f) / sw.durationMs
            if (sw.progress >= 1f) shockIter.remove()
        }

        // Update phase ripples
        val rippleIter = phaseRipples.iterator()
        while (rippleIter.hasNext()) {
            val pr = rippleIter.next()
            pr.currentRadius += dt * 110f
            pr.alpha = (1f - (pr.currentRadius / pr.maxRadius)).coerceAtLeast(0f)
            if (pr.currentRadius >= pr.maxRadius || pr.alpha <= 0f) {
                rippleIter.remove()
            }
        }

        // Update floating notices
        val noticeIter = floatingNotices.iterator()
        while (noticeIter.hasNext()) {
            val fn = noticeIter.next()
            fn.elapsedMs += dt * 1000f
            fn.y -= dt * 70f
            fn.alpha = (1f - (fn.elapsedMs / fn.lifeMs)).coerceIn(0f, 1f)
            fn.scale = 1f + 0.25f * (1f - fn.alpha)
            if (fn.elapsedMs >= fn.lifeMs) {
                noticeIter.remove()
            }
        }

        // Runner particles
        updateAmbientParticles(dt, screenWidth, screenHeight)
    }

    private fun checkMilestone() {
        if (score >= lastMilestoneScore + 25) {
            lastMilestoneScore = (score / 25) * 25
            onMilestone()
        }
    }

    private fun triggerGameOver(activeX: Float, activeY: Float, shadowX: Float) {
        status = GameStatus.GAME_OVER
        combo = 1
        gameOverState = GameOverAnimationState()

        onCollision()

        // Break active character into 35 glowing cyber particles
        if (particlesEnabled) {
            val burstColor = if (activeSide == PlayerSide.ACTIVE_LEFT) CyanNeon else MagentaNeon
            val particleCount = if (reducedEffects) 16 else 36
            for (i in 0 until particleCount) {
                val angle = random.nextFloat() * 2f * PI.toFloat()
                val speed = random.nextFloat() * 320f + 80f
                particles.add(
                    Particle(
                        x = activeX,
                        y = activeY,
                        vx = cos(angle) * speed,
                        vy = sin(angle) * speed,
                        size = random.nextFloat() * 7f + 3f,
                        alpha = 1f,
                        maxLife = 0.8f,
                        life = 0.8f,
                        color = if (i % 3 == 0) SeamCore else burstColor
                    )
                )
            }
        }
    }

    private fun updateGameOver(dt: Float, screenWidth: Float, screenHeight: Float) {
        val state = gameOverState ?: return
        state.elapsedMs += dt * 1000f

        // 1. Brief freeze in first 50ms
        if (state.elapsedMs < 50f) {
            return
        }

        // 2. Shake camera decay
        val progress = (state.elapsedMs / state.totalDurationMs).coerceIn(0f, 1f)
        val currentShake = (1f - progress) * (if (reducedEffects) 6f else 15f)
        shakeOffsetX = (random.nextFloat() * 2f - 1f) * currentShake
        shakeOffsetY = (random.nextFloat() * 2f - 1f) * currentShake

        // Flash and shadow fade
        state.flashAlpha = (1f - (state.elapsedMs / 250f)).coerceAtLeast(0f)
        state.shadowAlpha = (0.4f * (1f - (state.elapsedMs / 400f))).coerceAtLeast(0f)

        // Update existing burst particles
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.life -= dt
            p.alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
            if (p.life <= 0f) pIter.remove()
        }

        if (state.elapsedMs >= state.totalDurationMs) {
            shakeOffsetX = 0f
            shakeOffsetY = 0f
            onGameOverFinished()
        }
    }

    private fun updateAmbientParticles(dt: Float, screenWidth: Float, screenHeight: Float) {
        if (!particlesEnabled) {
            particles.clear()
            return
        }

        val characterY = screenHeight * 0.72f
        val leftX = screenWidth * 0.25f
        val rightX = screenWidth * 0.75f
        val seamX = screenWidth * 0.5f

        // Trail particles from runners
        val trailChance = if (reducedEffects) 0.3f else 0.7f
        if (random.nextFloat() < trailChance) {
            // Left runner trail
            val isLeftActive = (activeSide == PlayerSide.ACTIVE_LEFT)
            particles.add(
                Particle(
                    x = leftX + (random.nextFloat() * 16f - 8f),
                    y = characterY + 22f,
                    vx = (random.nextFloat() * 20f - 10f),
                    vy = random.nextFloat() * 80f + 40f,
                    size = if (isLeftActive) 3.5f else 2f,
                    alpha = if (isLeftActive) 0.8f else 0.35f,
                    maxLife = 0.32f,
                    life = 0.32f,
                    color = CyanNeon
                )
            )

            // Right runner trail
            val isRightActive = (activeSide == PlayerSide.ACTIVE_RIGHT)
            particles.add(
                Particle(
                    x = rightX + (random.nextFloat() * 16f - 8f),
                    y = characterY + 22f,
                    vx = (random.nextFloat() * 20f - 10f),
                    vy = random.nextFloat() * 80f + 40f,
                    size = if (isRightActive) 3.5f else 2f,
                    alpha = if (isRightActive) 0.8f else 0.35f,
                    maxLife = 0.32f,
                    life = 0.32f,
                    color = MagentaNeon
                )
            )
        }

        // Seam ambient particles
        if (!reducedEffects && random.nextFloat() < 0.25f) {
            particles.add(
                Particle(
                    x = seamX + (random.nextFloat() * 6f - 3f),
                    y = screenHeight * random.nextFloat(),
                    vx = (random.nextFloat() * 14f - 7f),
                    vy = (random.nextFloat() * 30f - 15f),
                    size = random.nextFloat() * 2.5f + 1f,
                    alpha = 0.7f,
                    maxLife = 0.5f,
                    life = 0.5f,
                    color = SeamCore
                )
            )
        }

        // Update all particles
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.life -= dt
            p.alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
            if (p.life <= 0f) pIter.remove()
        }
    }
}
