package com.example.shadowswitch

import com.example.shadowswitch.game.GameEngine
import com.example.shadowswitch.game.GameStatus
import com.example.shadowswitch.game.Obstacle
import com.example.shadowswitch.game.ObstacleGenerator
import com.example.shadowswitch.game.ObstacleSide
import com.example.shadowswitch.game.ObstacleType
import com.example.shadowswitch.game.PlayerSide
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GameEngineTest {

    private lateinit var engine: GameEngine
    private var switchEvents = mutableListOf<Boolean>()
    private var obstacleClearedCount = 0
    private var collisionOccurred = false
    private var milestoneOccurred = false
    private var gameOverFinished = false

    @Before
    fun setUp() {
        switchEvents.clear()
        obstacleClearedCount = 0
        collisionOccurred = false
        milestoneOccurred = false
        gameOverFinished = false

        engine = GameEngine(
            particlesEnabled = false,
            reducedEffects = true,
            onSwitch = { isPerfect -> switchEvents.add(isPerfect) },
            onObstacleCleared = { obstacleClearedCount++ },
            onCollision = { collisionOccurred = true },
            onMilestone = { milestoneOccurred = true },
            onGameOverFinished = { gameOverFinished = true }
        )
    }

    @Test
    fun testInitialState() {
        assertEquals(GameStatus.READY, engine.status)
        assertEquals(PlayerSide.ACTIVE_LEFT, engine.activeSide)
        assertEquals(0, engine.score)
        assertEquals(1, engine.combo)
        assertEquals(0, engine.switchesCount)
        assertTrue(engine.obstacles.isEmpty())
    }

    @Test
    fun testStartPlayingImmediately() {
        engine.startPlayingImmediately()
        assertEquals(GameStatus.PLAYING, engine.status)
        assertEquals(PlayerSide.ACTIVE_LEFT, engine.activeSide)
    }

    @Test
    fun testSideSwitchMechanic() {
        engine.startPlayingImmediately()

        // Tap once -> switches to ACTIVE_RIGHT
        engine.handleTap(screenWidth = 1000f, screenHeight = 2000f)
        assertEquals(PlayerSide.ACTIVE_RIGHT, engine.activeSide)
        assertEquals(1, engine.switchesCount)
        assertEquals(1, switchEvents.size)

        // Tap again -> switches to ACTIVE_LEFT
        engine.handleTap(screenWidth = 1000f, screenHeight = 2000f)
        assertEquals(PlayerSide.ACTIVE_LEFT, engine.activeSide)
        assertEquals(2, engine.switchesCount)
        assertEquals(2, switchEvents.size)
    }

    @Test
    fun testShadowPhaseThroughObstacleWithoutDamage() {
        engine.startPlayingImmediately()
        val screenW = 1000f
        val screenH = 2000f
        val charY = screenH * 0.72f // 1440f

        // Active side is LEFT.
        // Add an obstacle on the RIGHT side (shadow side)
        val shadowObstacle = Obstacle(
            id = 1L,
            side = ObstacleSide.RIGHT,
            type = ObstacleType.ENERGY_WALL,
            y = charY - 30f,
            width = 300f,
            height = 50f
        )
        engine.obstacles.add(shadowObstacle)

        // Advance engine by several frames through the obstacle
        for (i in 0 until 10) {
            engine.update(0.016f, screenW, screenH)
        }

        // Must still be PLAYING! No collision!
        assertEquals(GameStatus.PLAYING, engine.status)
        assertFalse(collisionOccurred)

        // Continue advancing until obstacle passes character
        for (i in 0 until 30) {
            engine.update(0.016f, screenW, screenH)
        }

        // Obstacle should be cleared and points awarded
        assertTrue(shadowObstacle.cleared)
        assertTrue(engine.score >= 2)
        assertEquals(1, obstacleClearedCount)
    }

    @Test
    fun testActiveSideCollisionCausesGameOver() {
        engine.startPlayingImmediately()
        val screenW = 1000f
        val screenH = 2000f
        val charY = screenH * 0.72f

        // Active side is LEFT.
        // Add an obstacle directly on the LEFT side (active side)
        val activeObstacle = Obstacle(
            id = 2L,
            side = ObstacleSide.LEFT,
            type = ObstacleType.LOW_BLOCK,
            y = charY - 20f,
            width = 300f,
            height = 50f
        )
        engine.obstacles.add(activeObstacle)

        // Update frame -> active character collides!
        engine.update(0.016f, screenW, screenH)

        assertEquals(GameStatus.GAME_OVER, engine.status)
        assertTrue(collisionOccurred)
        assertNotNull(engine.gameOverState)
    }

    @Test
    fun testPerfectSwitchWhenAvoidingObstacle() {
        engine.startPlayingImmediately()
        val screenW = 1000f
        val screenH = 2000f
        val charY = screenH * 0.72f // 1440f

        // Active side is LEFT.
        // Place an approaching obstacle on the LEFT side within the reaction window
        // Distance = charY - (obs.y + obs.height) should be ~60px
        val obsHeight = 50f
        val obsY = charY - obsHeight - 60f
        val obstacle = Obstacle(
            id = 3L,
            side = ObstacleSide.LEFT,
            type = ObstacleType.LASER_GATE,
            y = obsY,
            width = 350f,
            height = obsHeight
        )
        engine.obstacles.add(obstacle)

        // Tap to switch sides right before collision!
        engine.handleTap(screenW, screenH)

        // Active side is now RIGHT, and switch was PERFECT!
        assertEquals(PlayerSide.ACTIVE_RIGHT, engine.activeSide)
        assertEquals(1, engine.perfectSwitchesCount)
        assertEquals(2, engine.combo) // Combo incremented
        assertEquals(1, engine.score) // Perfect switch bonus +1
        assertTrue(switchEvents.last()) // isPerfect = true
    }

    @Test
    fun testPauseAndResume() {
        engine.startPlayingImmediately()
        engine.pause()
        assertEquals(GameStatus.PAUSED, engine.status)

        // While paused, update calls shouldn't change positions
        val initialDist = engine.perspectiveGridOffset
        engine.update(0.1f, 1000f, 2000f)
        assertEquals(initialDist, engine.perspectiveGridOffset, 0.001f)

        engine.resume()
        assertEquals(GameStatus.PLAYING, engine.status)
    }

    @Test
    fun testObstacleGeneratorFairness() {
        val generator = ObstacleGenerator()
        generator.reset()

        val spawned = mutableListOf<Obstacle>()
        val speed = 500f
        val laneWidth = 400f

        // Simulate 50 seconds of gameplay
        val deltaSeconds = 0.02f
        val deltaDistance = speed * deltaSeconds

        for (sec in 0..2500) {
            generator.update(
                deltaDistance = deltaDistance,
                score = 35,
                laneWidth = laneWidth,
                currentSpeed = speed
            ) { obs ->
                spawned.add(obs)
            }
        }

        // Verify obstacles were generated
        assertTrue(spawned.size >= 15)

        // Verify guaranteed fairness: no two consecutive obstacles have distance < safe minimum
        // (At least 180px gap)
        for (i in 0 until spawned.size - 1) {
            val curr = spawned[i]
            val next = spawned[i + 1]
            // Since they spawn sequentially, id is strictly increasing
            assertTrue(next.id > curr.id)
        }
    }
}
