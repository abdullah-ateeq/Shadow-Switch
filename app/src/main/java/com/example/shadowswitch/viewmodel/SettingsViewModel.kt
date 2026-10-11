package com.example.shadowswitch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shadowswitch.data.GamePreferences
import com.example.shadowswitch.data.PreferencesManager
import com.example.shadowswitch.ui.theme.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val preferences: StateFlow<GamePreferences> = preferencesManager.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GamePreferences()
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setSoundEnabled(enabled)
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setHapticsEnabled(enabled)
        }
    }

    fun setParticlesEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setParticlesEnabled(enabled)
        }
    }

    fun setReducedEffectsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setReducedEffectsEnabled(enabled)
        }
    }

    fun resetBestScore() {
        viewModelScope.launch {
            preferencesManager.resetBestScore()
        }
    }
}
