# Shadow Switch ⚡

[![Android SDK](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-blue.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**Shadow Switch** is a high-speed, dual-reality neon arcade game built natively for Android using **Jetpack Compose** and modern Android architecture. Players control a phase-shifting runner capable of switching between two light dimensions—**Cyan (Left)** and **Magenta (Right)**—to navigate through an ever-accelerating obstacle corridor.

---

## 🌟 Features

* ⚡ **Dual-Phase Gameplay Mechanics**: Tap anywhere on the screen to instantly phase shift between active sides. Match or dodge obstacles in real time as the world speed scales continuously.
* 🎯 **Combo & Perfect Switch System**: Precise phase shifting right before obstacle collision triggers **Perfect Switches**, building combo multipliers and boosting total score.
* 🎨 **Neon Glassmorphism UI**: High-tech visual design featuring custom Compose graphics, split-screen animated gradient backgrounds, glassmorphism card surfaces (`GlassCard`), and glowing neon controls (`NeonButton`).
* 💥 **Custom Visual Effects Engine**: Built with native Canvas rendering, supporting procedural particle effects, shockwaves, phase ripples, speed perspective grid lines, and camera shake.
* 🎵 **Adaptive Audio & Haptic Feedback**: Integrated SoundPool sound manager for zero-latency audio cues paired with fine-tuned device haptic feedback patterns.
* 🎨 **Dynamic Theme Engine**: Toggle between visual style modes including **Neon Cyan**, **Cyberpunk Pink**, and **OLED Dark**.
* ⚙️ **Customization & Accessibility**: Configure particle density, reduced motion preferences, sound effects, and haptic feedback.
* 💾 **Data Persistence**: Local high scores and user preferences saved seamlessly using **Jetpack DataStore Preferences**.

---

## 📱 Screenshots & Screens

The app comprises 6 fully animated screens:
1. **Splash Screen**: Animated neon branding entrance with glowing light ring effects.
2. **Home Screen**: Live split-background demo, best score trophy display, quick game start, and navigation links.
3. **Game Screen**: Real-time HUD (Score, Combo, Best Score, Pause Menu), Canvas-driven game rendering with particles, floating indicators, and shockwaves.
4. **Result Screen**: Detailed post-run summary covering score, combo count, switches, perfect switch ratio, and obstacles cleared.
5. **Settings Screen**: Complete customization suite for sound, vibration, themes, graphics performance, and high score resets.
6. **About Screen**: Gameplay rules, storyline, control instructions, and version details.

---

## 🛠️ Tech Stack & Architecture

* **Language**: [Kotlin](https://kotlinlang.org/)
* **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3, Custom Canvas graphics, Compose BOM)
* **Architecture**: MVVM (Model-View-ViewModel) + Clean Game Engine Separation
* **State Management**: Kotlin StateFlow, `remember` / `mutableStateOf` Compose state wrappers
* **Data Storage**: [Jetpack DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore)
* **Audio & Haptics**: Android `SoundPool`, `Vibrator` / `VibratorManager`
* **Build System**: Gradle with Kotlin DSL (`build.gradle.kts`)
* **Testing**: JUnit 4 for `GameEngine` unit testing & AndroidX Test runners

---

## 📂 Project Structure

```
ShadowSwitch/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/shadowswitch/
│   │   │   │   ├── audio/              # SoundManager with SoundPool audio effects
│   │   │   │   ├── data/               # PreferencesManager (DataStore integration)
│   │   │   │   ├── game/               # GameEngine, ObstacleGenerator, GameModels
│   │   │   │   ├── haptics/            # HapticFeedbackManager
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/     # NeonButton, GlassCard, AnimatedSplitBackground
│   │   │   │   │   ├── navigation/     # Screen routing & state navigation
│   │   │   │   │   ├── screens/        # Splash, Home, Game, Result, Settings, About
│   │   │   │   │   └── theme/          # Cyberpunk neon color palettes, typography, shapes
│   │   │   │   ├── viewmodel/          # GameViewModel & SettingsViewModel
│   │   │   │   └── MainActivity.kt     # App entry point & edge-to-edge layout
│   │   │   ├── res/                    # Drawables, mipmaps, launcher icons, strings
│   │   │   └── AndroidManifest.xml
│   │   └── test/                       # Unit tests for GameEngine logic
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 🚀 Getting Started

### Prerequisites
* **Android Studio**: Ladybug (2024.2.1) or newer recommended.
* **JDK**: Version 11 or higher.
* **Android Device / Emulator**: Running Android 7.0 (API level 24) or above.

### Build and Run

1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/Shadow-Switch.git
   cd Shadow-Switch
   ```

2. **Open in Android Studio**:
   Open the project folder in Android Studio and let Gradle sync complete.

3. **Build the Debug APK via CLI**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run Unit Tests**:
   ```bash
   ./gradlew test
   ```

---

## 🎮 How to Play

1. **Start Game**: Tap **PLAY NOW** on the main menu.
2. **Phase Switch**: Tap anywhere on the screen during gameplay to shift your runner between the **Left (Cyan)** and **Right (Magenta)** reality lanes.
3. **Avoid Obstacles**: Align your runner's active side to bypass incoming colored laser barriers and blocks.
4. **Build Combos**: Perform switches immediately prior to obstacle passing to score **Perfect Switches** and build high-score combo multipliers.

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

Developed with ❤️ by [Abdullah Ateeq](https://github.com/abdullah-ateeq).
