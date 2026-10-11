package com.example.shadowswitch.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class SoundType {
    SWITCH,
    OBSTACLE_CLEAR,
    PERFECT_SWITCH,
    COLLISION,
    BUTTON_CLICK,
    MILESTONE
}

class SoundManager(private val context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundMap = mutableMapOf<SoundType, Int>()
    private var isLoaded = false
    var isEnabled: Boolean = true

    init {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                generateAndLoadSounds()
            } catch (_: Exception) {
                // Audio synthesis fallback
            }
        }
    }

    private fun generateAndLoadSounds() {
        val sfxDir = File(context.cacheDir, "sfx")
        if (!sfxDir.exists()) sfxDir.mkdirs()

        val sampleRate = 44100

        // 1. Switch sound: snappy futuristic pitch sweep (300Hz -> 750Hz)
        val switchWav = File(sfxDir, "switch.wav")
        writeWavFile(switchWav, generateSwitchSound(sampleRate), sampleRate)
        soundMap[SoundType.SWITCH] = soundPool.load(switchWav.absolutePath, 1)

        // 2. Obstacle Cleared: crystal resonant ping
        val clearWav = File(sfxDir, "clear.wav")
        writeWavFile(clearWav, generateClearSound(sampleRate), sampleRate)
        soundMap[SoundType.OBSTACLE_CLEAR] = soundPool.load(clearWav.absolutePath, 1)

        // 3. Perfect Switch: high euphoric crystal arpeggio
        val perfectWav = File(sfxDir, "perfect.wav")
        writeWavFile(perfectWav, generatePerfectSound(sampleRate), sampleRate)
        soundMap[SoundType.PERFECT_SWITCH] = soundPool.load(perfectWav.absolutePath, 1)

        // 4. Collision: heavy electronic impact + sub thud
        val collisionWav = File(sfxDir, "collision.wav")
        writeWavFile(collisionWav, generateCollisionSound(sampleRate), sampleRate)
        soundMap[SoundType.COLLISION] = soundPool.load(collisionWav.absolutePath, 1)

        // 5. Button click: crisp UI micro-tick
        val clickWav = File(sfxDir, "click.wav")
        writeWavFile(clickWav, generateClickSound(sampleRate), sampleRate)
        soundMap[SoundType.BUTTON_CLICK] = soundPool.load(clickWav.absolutePath, 1)

        // 6. Milestone chime
        val milestoneWav = File(sfxDir, "milestone.wav")
        writeWavFile(milestoneWav, generateMilestoneSound(sampleRate), sampleRate)
        soundMap[SoundType.MILESTONE] = soundPool.load(milestoneWav.absolutePath, 1)

        isLoaded = true
    }

    fun play(type: SoundType, volume: Float = 0.85f) {
        if (!isEnabled || !isLoaded) return
        val soundId = soundMap[type] ?: return
        soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
    }

    fun release() {
        soundPool.release()
    }

    // --- Sound Synthesis Helpers ---

    private fun generateSwitchSound(sampleRate: Int): ShortArray {
        val duration = 0.12f // 120ms
        val totalSamples = (sampleRate * duration).toInt()
        val samples = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / sampleRate
            val progress = i.toFloat() / totalSamples
            // Exponential pitch rise from 320Hz to 750Hz
            val freq = 320.0 + 430.0 * (progress * progress)
            val envelope = exp(-progress * 5.0) * (1.0 - exp(-t * 800.0))
            val wave = 0.7 * sin(2.0 * PI * freq * t) + 0.3 * sin(4.0 * PI * freq * t)
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.7).toInt().coerceIn(-32768, 32767).toShort()
        }
        return samples
    }

    private fun generateClearSound(sampleRate: Int): ShortArray {
        val duration = 0.16f
        val totalSamples = (sampleRate * duration).toInt()
        val samples = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / sampleRate
            val progress = i.toFloat() / totalSamples
            val envelope = exp(-progress * 8.0) * (1.0 - exp(-t * 600.0))
            val wave = 0.75 * sin(2.0 * PI * 880.0 * t) + 0.25 * sin(2.0 * PI * 1760.0 * t)
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.6).toInt().coerceIn(-32768, 32767).toShort()
        }
        return samples
    }

    private fun generatePerfectSound(sampleRate: Int): ShortArray {
        val duration = 0.24f
        val totalSamples = (sampleRate * duration).toInt()
        val samples = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / sampleRate
            val progress = i.toFloat() / totalSamples
            val envelope = exp(-progress * 5.5) * (1.0 - exp(-t * 500.0))

            // Shimmering chord (C6 1046.5Hz, E6 1318.5Hz, G6 1567.9Hz)
            val note1 = sin(2.0 * PI * 1046.5 * t)
            val note2 = sin(2.0 * PI * 1318.5 * t)
            val note3 = sin(2.0 * PI * 1567.9 * t)
            val wave = (note1 * 0.4 + note2 * 0.35 + note3 * 0.25)
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.75).toInt().coerceIn(-32768, 32767).toShort()
        }
        return samples
    }

    private fun generateCollisionSound(sampleRate: Int): ShortArray {
        val duration = 0.35f
        val totalSamples = (sampleRate * duration).toInt()
        val samples = ShortArray(totalSamples)

        val random = java.util.Random(42)
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / sampleRate
            val progress = i.toFloat() / totalSamples
            val envelope = exp(-progress * 6.5)

            // Heavy pitch-down thud (140Hz -> 45Hz)
            val freq = 140.0 * (1.0 - progress * 0.68)
            val tone = sin(2.0 * PI * freq * t)
            val noise = (random.nextFloat() * 2.0 - 1.0) * exp(-progress * 14.0)
            val wave = 0.65 * tone + 0.35 * noise
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.9).toInt().coerceIn(-32768, 32767).toShort()
        }
        return samples
    }

    private fun generateClickSound(sampleRate: Int): ShortArray {
        val duration = 0.025f
        val totalSamples = (sampleRate * duration).toInt()
        val samples = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / sampleRate
            val progress = i.toFloat() / totalSamples
            val envelope = exp(-progress * 25.0)
            val wave = sin(2.0 * PI * 1400.0 * t)
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.5).toInt().coerceIn(-32768, 32767).toShort()
        }
        return samples
    }

    private fun generateMilestoneSound(sampleRate: Int): ShortArray {
        val duration = 0.28f
        val totalSamples = (sampleRate * duration).toInt()
        val samples = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / sampleRate
            val progress = i.toFloat() / totalSamples
            val envelope = exp(-progress * 4.5) * (1.0 - exp(-t * 400.0))

            // Two distinct chime intervals
            val f = if (progress < 0.4) 659.25 else 783.99
            val wave = sin(2.0 * PI * f * t) + 0.3 * sin(4.0 * PI * f * t)
            samples[i] = (wave * envelope * Short.MAX_VALUE * 0.65).toInt().coerceIn(-32768, 32767).toShort()
        }
        return samples
    }

    private fun writeWavFile(file: File, pcmData: ShortArray, sampleRate: Int) {
        val byteBuffer = ByteBuffer.allocate(pcmData.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        for (sample in pcmData) {
            byteBuffer.putShort(sample)
        }
        val audioBytes = byteBuffer.array()

        val totalDataLen = audioBytes.size + 36
        val byteRate = sampleRate * 1 * 2

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0 // Subchunk1Size
        header[20] = 1; header[21] = 0 // AudioFormat (PCM = 1)
        header[22] = 1; header[23] = 0 // NumChannels (1 = Mono)
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = 2; header[33] = 0 // BlockAlign
        header[34] = 16; header[35] = 0 // BitsPerSample
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (audioBytes.size and 0xff).toByte()
        header[41] = ((audioBytes.size shr 8) and 0xff).toByte()
        header[42] = ((audioBytes.size shr 16) and 0xff).toByte()
        header[43] = ((audioBytes.size shr 24) and 0xff).toByte()

        FileOutputStream(file).use { out ->
            out.write(header)
            out.write(audioBytes)
        }
    }
}
