package com.example.shadowswitch.game

import kotlin.math.max
import kotlin.random.Random

class ObstacleGenerator {

    private var nextObstacleId = 1L
    private val plannedQueue = ArrayDeque<Pair<ObstacleSide, ObstacleType>>()
    private var distanceUntilNextSpawn = 0f
    private val random = Random(System.currentTimeMillis())

    fun reset() {
        nextObstacleId = 1L
        plannedQueue.clear()
        distanceUntilNextSpawn = 200f // initial safe lead-in distance
    }

    /**
     * Updates spawning logic.
     * @param deltaDistance distance travelled this frame in pixels
     * @param score current score
     * @param laneWidth width of one side (left or right)
     * @param onSpawn callback when an obstacle is ready to spawn
     */
    fun update(
        deltaDistance: Float,
        score: Int,
        laneWidth: Float,
        currentSpeed: Float,
        onSpawn: (Obstacle) -> Unit
    ) {
        distanceUntilNextSpawn -= deltaDistance

        if (distanceUntilNextSpawn <= 0f) {
            if (plannedQueue.isEmpty()) {
                generatePatternBatch(score)
            }

            if (plannedQueue.isNotEmpty()) {
                val (side, type) = plannedQueue.removeFirst()

                // Calculate dimensions tailored to laneWidth and obstacle type
                val width = when (type) {
                    ObstacleType.ENERGY_WALL -> laneWidth * 0.72f
                    ObstacleType.LOW_BLOCK -> laneWidth * 0.78f
                    ObstacleType.LASER_GATE -> laneWidth * 0.90f
                    ObstacleType.SPIKE_ZONE -> laneWidth * 0.65f
                    ObstacleType.CLOSING_BARRIER -> laneWidth * 0.82f
                }

                val height = when (type) {
                    ObstacleType.ENERGY_WALL -> 52f
                    ObstacleType.LOW_BLOCK -> 60f
                    ObstacleType.LASER_GATE -> 38f
                    ObstacleType.SPIKE_ZONE -> 46f
                    ObstacleType.CLOSING_BARRIER -> 54f
                }

                val obstacle = Obstacle(
                    id = nextObstacleId++,
                    side = side,
                    type = type,
                    y = -height - 10f,
                    width = width,
                    height = height
                )
                onSpawn(obstacle)

                // Calculate next gap based on difficulty, maintaining fairness
                distanceUntilNextSpawn = calculateGap(score, currentSpeed)
            }
        }
    }

    private fun calculateGap(score: Int, currentSpeed: Float): Float {
        // Enforce guaranteed fair reaction time: min 0.42s at high score, up to 1.1s at start
        val minReactionTime = when {
            score < 20 -> 1.05f
            score < 50 -> 0.85f
            score < 100 -> 0.65f
            else -> 0.46f
        }

        val baseGap = currentSpeed * minReactionTime
        // Add random jitter to make rhythm interesting
        val jitter = random.nextFloat() * (currentSpeed * 0.22f)
        return max(240f, baseGap + jitter)
    }

    private fun generatePatternBatch(score: Int) {
        // Determine available types according to score
        val availableTypes = mutableListOf(ObstacleType.ENERGY_WALL, ObstacleType.LOW_BLOCK)
        if (score >= 20) availableTypes.add(ObstacleType.LASER_GATE)
        if (score >= 50) availableTypes.add(ObstacleType.SPIKE_ZONE)
        if (score >= 80) availableTypes.add(ObstacleType.CLOSING_BARRIER)

        fun randomType(): ObstacleType = availableTypes[random.nextInt(availableTypes.size)]

        when {
            score < 20 -> {
                // Score 0-20: Mostly single side with generous pacing
                val side = if (random.nextBoolean()) ObstacleSide.LEFT else ObstacleSide.RIGHT
                plannedQueue.add(side to randomType())
            }
            score < 50 -> {
                // Score 21-50: Alternating patterns L->R, R->L, with occasional single
                val roll = random.nextInt(100)
                if (roll < 35) {
                    val s = if (random.nextBoolean()) ObstacleSide.LEFT else ObstacleSide.RIGHT
                    plannedQueue.add(s to randomType())
                } else if (roll < 70) {
                    plannedQueue.add(ObstacleSide.LEFT to randomType())
                    plannedQueue.add(ObstacleSide.RIGHT to randomType())
                } else {
                    plannedQueue.add(ObstacleSide.RIGHT to randomType())
                    plannedQueue.add(ObstacleSide.LEFT to randomType())
                }
            }
            score < 100 -> {
                // Score 51-100: Triple cadences & fast switches
                val roll = random.nextInt(100)
                when {
                    roll < 25 -> {
                        val s = if (random.nextBoolean()) ObstacleSide.LEFT else ObstacleSide.RIGHT
                        plannedQueue.add(s to randomType())
                    }
                    roll < 60 -> {
                        val s1 = if (random.nextBoolean()) ObstacleSide.LEFT else ObstacleSide.RIGHT
                        val s2 = if (s1 == ObstacleSide.LEFT) ObstacleSide.RIGHT else ObstacleSide.LEFT
                        plannedQueue.add(s1 to randomType())
                        plannedQueue.add(s2 to randomType())
                    }
                    else -> {
                        // Triple rhythm: L -> R -> L or R -> L -> R
                        val start = if (random.nextBoolean()) ObstacleSide.LEFT else ObstacleSide.RIGHT
                        val opp = if (start == ObstacleSide.LEFT) ObstacleSide.RIGHT else ObstacleSide.LEFT
                        plannedQueue.add(start to randomType())
                        plannedQueue.add(opp to randomType())
                        plannedQueue.add(start to randomType())
                    }
                }
            }
            else -> {
                // Score 100+: High tempo arcade patterns, still strictly fair!
                val roll = random.nextInt(100)
                when {
                    roll < 40 -> {
                        // Fast alternating
                        val s1 = if (random.nextBoolean()) ObstacleSide.LEFT else ObstacleSide.RIGHT
                        val s2 = if (s1 == ObstacleSide.LEFT) ObstacleSide.RIGHT else ObstacleSide.LEFT
                        plannedQueue.add(s1 to randomType())
                        plannedQueue.add(s2 to randomType())
                    }
                    roll < 80 -> {
                        // Triple sequence
                        val s1 = if (random.nextBoolean()) ObstacleSide.LEFT else ObstacleSide.RIGHT
                        val s2 = if (s1 == ObstacleSide.LEFT) ObstacleSide.RIGHT else ObstacleSide.LEFT
                        plannedQueue.add(s1 to randomType())
                        plannedQueue.add(s2 to randomType())
                        plannedQueue.add(s1 to randomType())
                    }
                    else -> {
                        // Quad sequence: L -> R -> L -> R
                        plannedQueue.add(ObstacleSide.LEFT to randomType())
                        plannedQueue.add(ObstacleSide.RIGHT to randomType())
                        plannedQueue.add(ObstacleSide.LEFT to randomType())
                        plannedQueue.add(ObstacleSide.RIGHT to randomType())
                    }
                }
            }
        }
    }
}
