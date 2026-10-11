package com.example.shadowswitch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.shadowswitch.audio.SoundManager
import com.example.shadowswitch.data.PreferencesManager
import com.example.shadowswitch.game.GameStatus
import com.example.shadowswitch.haptics.HapticFeedbackManager
import com.example.shadowswitch.ui.navigation.Screen
import com.example.shadowswitch.ui.screens.AboutScreen
import com.example.shadowswitch.ui.screens.GameScreen
import com.example.shadowswitch.ui.screens.HomeScreen
import com.example.shadowswitch.ui.screens.ResultScreen
import com.example.shadowswitch.ui.screens.SettingsScreen
import com.example.shadowswitch.ui.screens.SplashScreen
import com.example.shadowswitch.ui.theme.ShadowSwitchTheme
import com.example.shadowswitch.viewmodel.GameViewModel
import com.example.shadowswitch.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var soundManager: SoundManager
    private lateinit var hapticFeedbackManager: HapticFeedbackManager
    private lateinit var gameViewModel: GameViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferencesManager = PreferencesManager(applicationContext)
        soundManager = SoundManager(applicationContext)
        hapticFeedbackManager = HapticFeedbackManager(applicationContext)

        gameViewModel = GameViewModel(
            preferencesManager = preferencesManager,
            soundManager = soundManager,
            hapticManager = hapticFeedbackManager
        )

        settingsViewModel = SettingsViewModel(
            preferencesManager = preferencesManager
        )

        setContent {
            val prefs by settingsViewModel.preferences.collectAsState()

            ShadowSwitchTheme(themeMode = prefs.themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ShadowSwitchApp(
                        gameViewModel = gameViewModel,
                        settingsViewModel = settingsViewModel,
                        onExitApp = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun ShadowSwitchApp(
    gameViewModel: GameViewModel,
    settingsViewModel: SettingsViewModel,
    onExitApp: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }

    gameViewModel.onGameOverNavigate = {
        currentScreen = Screen.RESULT
    }

    // Hardware Back Button Handling
    BackHandler {
        when (currentScreen) {
            Screen.SPLASH -> onExitApp()
            Screen.HOME -> onExitApp()
            Screen.GAME -> {
                if (gameViewModel.engine.status == GameStatus.PLAYING) {
                    gameViewModel.pause()
                } else {
                    currentScreen = Screen.HOME
                }
            }
            Screen.RESULT -> currentScreen = Screen.HOME
            Screen.SETTINGS -> currentScreen = Screen.HOME
            Screen.ABOUT -> currentScreen = Screen.HOME
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
        },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            Screen.SPLASH -> {
                SplashScreen(
                    onSplashFinished = {
                        currentScreen = Screen.HOME
                    }
                )
            }

            Screen.HOME -> {
                HomeScreen(
                    bestScore = gameViewModel.bestScore,
                    onPlayClick = {
                        gameViewModel.startNewGame()
                        currentScreen = Screen.GAME
                    },
                    onNavigate = { target ->
                        currentScreen = target
                    }
                )
            }

            Screen.GAME -> {
                GameScreen(
                    viewModel = gameViewModel,
                    onHomeClick = {
                        currentScreen = Screen.HOME
                    }
                )
            }

            Screen.RESULT -> {
                ResultScreen(
                    runSummary = gameViewModel.runSummary,
                    onPlayAgainClick = {
                        gameViewModel.startNewGame()
                        currentScreen = Screen.GAME
                    },
                    onHomeClick = {
                        currentScreen = Screen.HOME
                    }
                )
            }

            Screen.SETTINGS -> {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigate = { target ->
                        currentScreen = target
                    }
                )
            }

            Screen.ABOUT -> {
                AboutScreen(
                    onNavigate = { target ->
                        currentScreen = target
                    }
                )
            }
        }
    }
}