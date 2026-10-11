package com.example.shadowswitch.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.shadowswitch.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "shadow_switch_prefs")

data class GamePreferences(
    val bestScore: Int = 0,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val particlesEnabled: Boolean = true,
    val reducedEffectsEnabled: Boolean = false,
    val tutorialCompleted: Boolean = false,
    val totalSwitches: Int = 0,
    val totalPerfectSwitches: Int = 0,
    val totalObstaclesCleared: Int = 0,
    val totalGamesPlayed: Int = 0
)

class PreferencesManager(private val context: Context) {

    private object Keys {
        val BEST_SCORE = intPreferencesKey("best_score")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val PARTICLES_ENABLED = booleanPreferencesKey("particles_enabled")
        val REDUCED_EFFECTS_ENABLED = booleanPreferencesKey("reduced_effects_enabled")
        val TUTORIAL_COMPLETED = booleanPreferencesKey("tutorial_completed")
        val TOTAL_SWITCHES = intPreferencesKey("total_switches")
        val TOTAL_PERFECT_SWITCHES = intPreferencesKey("total_perfect_switches")
        val TOTAL_OBSTACLES_CLEARED = intPreferencesKey("total_obstacles_cleared")
        val TOTAL_GAMES_PLAYED = intPreferencesKey("total_games_played")
    }

    val preferencesFlow: Flow<GamePreferences> = context.dataStore.data.map { prefs ->
        val themeModeStr = prefs[Keys.THEME_MODE] ?: ThemeMode.DARK.name
        val themeMode = try {
            ThemeMode.valueOf(themeModeStr)
        } catch (_: Exception) {
            ThemeMode.DARK
        }

        GamePreferences(
            bestScore = prefs[Keys.BEST_SCORE] ?: 0,
            themeMode = themeMode,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            particlesEnabled = prefs[Keys.PARTICLES_ENABLED] ?: true,
            reducedEffectsEnabled = prefs[Keys.REDUCED_EFFECTS_ENABLED] ?: false,
            tutorialCompleted = prefs[Keys.TUTORIAL_COMPLETED] ?: false,
            totalSwitches = prefs[Keys.TOTAL_SWITCHES] ?: 0,
            totalPerfectSwitches = prefs[Keys.TOTAL_PERFECT_SWITCHES] ?: 0,
            totalObstaclesCleared = prefs[Keys.TOTAL_OBSTACLES_CLEARED] ?: 0,
            totalGamesPlayed = prefs[Keys.TOTAL_GAMES_PLAYED] ?: 0
        )
    }

    suspend fun saveBestScoreIfHigher(score: Int): Boolean {
        var isNewBest = false
        context.dataStore.edit { prefs ->
            val currentBest = prefs[Keys.BEST_SCORE] ?: 0
            if (score > currentBest) {
                prefs[Keys.BEST_SCORE] = score
                isNewBest = true
            }
        }
        return isNewBest
    }

    suspend fun resetBestScore() {
        context.dataStore.edit { prefs ->
            prefs[Keys.BEST_SCORE] = 0
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun setParticlesEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PARTICLES_ENABLED] = enabled
        }
    }

    suspend fun setReducedEffectsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.REDUCED_EFFECTS_ENABLED] = enabled
        }
    }

    suspend fun setTutorialCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TUTORIAL_COMPLETED] = completed
        }
    }

    suspend fun recordGameRun(
        score: Int,
        switches: Int,
        perfectSwitches: Int,
        obstaclesCleared: Int
    ) {
        context.dataStore.edit { prefs ->
            val currentBest = prefs[Keys.BEST_SCORE] ?: 0
            if (score > currentBest) {
                prefs[Keys.BEST_SCORE] = score
            }
            prefs[Keys.TOTAL_SWITCHES] = (prefs[Keys.TOTAL_SWITCHES] ?: 0) + switches
            prefs[Keys.TOTAL_PERFECT_SWITCHES] = (prefs[Keys.TOTAL_PERFECT_SWITCHES] ?: 0) + perfectSwitches
            prefs[Keys.TOTAL_OBSTACLES_CLEARED] = (prefs[Keys.TOTAL_OBSTACLES_CLEARED] ?: 0) + obstaclesCleared
            prefs[Keys.TOTAL_GAMES_PLAYED] = (prefs[Keys.TOTAL_GAMES_PLAYED] ?: 0) + 1
        }
    }
}
