package com.example.shadowswitch.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shadowswitch.audio.SoundManager
import com.example.shadowswitch.audio.SoundType
import com.example.shadowswitch.data.PreferencesManager
import com.example.shadowswitch.game.GameEngine
import com.example.shadowswitch.game.GameStatus
import com.example.shadowswitch.game.PlayerSide
import com.example.shadowswitch.haptics.HapticFeedbackManager
import com.example.shadowswitch.haptics.HapticType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class RunSummary(
    val score: Int = 0,
    val bestScore: Int = 0,
    val isNewBest: Boolean = false,
    val switches: Int = 0,
    val perfectSwitches: Int = 0,
    val obstaclesCleared: Int = 0,
    val maxCombo: Int = 1
)

class GameViewModel(
    private val preferencesManager: PreferencesManager,
    val soundManager: SoundManager,
    val hapticManager: HapticFeedbackManager
) : ViewModel() {

    var bestScore by mutableIntStateOf(0)
        private set

    var isTutorialCompleted by mutableStateOf(true)
        private set

    var runSummary by mutableStateOf(RunSummary())
        private set

    var onGameOverNavigate: () -> Unit = {}

    val engine = GameEngine(
        onSwitch = { isPerfect ->
            if (isPerfect) {
                soundManager.play(SoundType.PERFECT_SWITCH, 0.9f)
                hapticManager.trigger(HapticType.PERFECT_SWITCH)
            } else {
                soundManager.play(SoundType.SWITCH, 0.8f)
                hapticManager.trigger(HapticType.SWITCH)
            }
        },
        onObstacleCleared = {
            soundManager.play(SoundType.OBSTACLE_CLEAR, 0.75f)
        },
        onCollision = {
            soundManager.play(SoundType.COLLISION, 1.0f)
            hapticManager.trigger(HapticType.COLLISION)
        },
        onMilestone = {
            soundManager.play(SoundType.MILESTONE, 0.85f)
        },
        onGameOverFinished = {
            finalizeGameRun()
        }
    )

    init {
        viewModelScope.launch {
            preferencesManager.preferencesFlow.collect { prefs ->
                bestScore = prefs.bestScore
                engine.bestScore = prefs.bestScore
                engine.particlesEnabled = prefs.particlesEnabled
                engine.reducedEffects = prefs.reducedEffectsEnabled
                soundManager.isEnabled = prefs.soundEnabled
                hapticManager.isEnabled = prefs.hapticsEnabled
                isTutorialCompleted = prefs.tutorialCompleted
            }
        }
    }

    fun startNewGame() {
        engine.bestScore = bestScore
        engine.startCountdown()
    }

    fun handlePlayfieldTap(width: Float, height: Float) {
        val oldSwitches = engine.switchesCount
        engine.handleTap(width, height)
        // If tutorial is showing and player switched successfully 3 times, mark tutorial done
        if (!isTutorialCompleted && engine.switchesCount >= 3) {
            completeTutorial()
        }
    }

    fun pause() {
        soundManager.play(SoundType.BUTTON_CLICK, 0.5f)
        engine.pause()
    }

    fun resume() {
        soundManager.play(SoundType.BUTTON_CLICK, 0.5f)
        engine.resume()
    }

    fun restart() {
        soundManager.play(SoundType.BUTTON_CLICK, 0.5f)
        engine.restart()
    }

    fun completeTutorial() {
        isTutorialCompleted = true
        viewModelScope.launch {
            preferencesManager.setTutorialCompleted(true)
        }
    }

    private fun finalizeGameRun() {
        viewModelScope.launch {
            val finalScore = engine.score
            val isNewBest = finalScore > bestScore && finalScore > 0

            runSummary = RunSummary(
                score = finalScore,
                bestScore = maxOf(finalScore, bestScore),
                isNewBest = isNewBest,
                switches = engine.switchesCount,
                perfectSwitches = engine.perfectSwitchesCount,
                obstaclesCleared = engine.obstaclesClearedCount,
                maxCombo = engine.maxCombo
            )

            preferencesManager.recordGameRun(
                score = finalScore,
                switches = engine.switchesCount,
                perfectSwitches = engine.perfectSwitchesCount,
                obstaclesCleared = engine.obstaclesClearedCount
            )

            if (isNewBest) {
                bestScore = finalScore
            }

            onGameOverNavigate()
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
