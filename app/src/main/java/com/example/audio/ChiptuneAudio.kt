package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object ChiptuneAudio {
    var isSoundEnabled: Boolean = true
    var isMusicEnabled: Boolean = true
        set(value) {
            field = value
            if (!value) {
                stopBgm()
            } else if (isPlayingBgm) {
                startBgm()
            }
        }

    var musicVolume: Float = 0.18f // cozy tavern ambient volume
        set(value) {
            field = value.coerceIn(0f, 1f)
            musicTrack?.setVolume(field)
        }

    private val scope = CoroutineScope(Dispatchers.Default)
    private var musicTrack: AudioTrack? = null
    private var bgmJob: kotlinx.coroutines.Job? = null
    var isPlayingBgm: Boolean = false
        private set

    fun startBgm() {
        if (!isMusicEnabled) {
            isPlayingBgm = true
            return
        }
        if (bgmJob?.isActive == true) return
        isPlayingBgm = true

        bgmJob = scope.launch {
            try {
                // Generate cozy fantasy tavern lute/harp looping sequence
                // Notes in A minor / C major pentatonic & folk: A3, C4, E4, G4, A4, B4, C5, D5, E5
                val sampleRate = 22050
                val bpm = 96
                val beatMs = (60000 / bpm)
                val halfBeatMs = beatMs / 2

                // Tavern motif: gentle arpeggiated acoustic lute / music box tune
                // [Note, durationMs, waveType, volumeScale]
                data class MelodicNote(val freq: Float, val ms: Int, val wave: WaveType, val vol: Float = 1f)

                // Frequencies
                val A3 = 220.00f; val B3 = 246.94f; val C4 = 261.63f; val D4 = 293.66f
                val E4 = 329.63f; val F4 = 349.23f; val G4 = 392.00f; val A4 = 440.00f
                val B4 = 493.88f; val C5 = 523.25f; val D5 = 587.33f; val E5 = 659.25f

                val songMelody = listOf(
                    // Measure 1 - Cozy Hearth (Am)
                    MelodicNote(A3, halfBeatMs, WaveType.TRIANGLE, 0.9f),
                    MelodicNote(E4, halfBeatMs, WaveType.SINE, 0.7f),
                    MelodicNote(A4, beatMs, WaveType.TRIANGLE, 0.8f),
                    MelodicNote(C5, halfBeatMs, WaveType.SINE, 0.7f),
                    MelodicNote(B4, halfBeatMs, WaveType.SINE, 0.7f),
                    MelodicNote(A4, beatMs, WaveType.TRIANGLE, 0.8f),

                    // Measure 2 - Tavern Table (C major)
                    MelodicNote(C4, halfBeatMs, WaveType.TRIANGLE, 0.9f),
                    MelodicNote(G4, halfBeatMs, WaveType.SINE, 0.7f),
                    MelodicNote(C5, beatMs, WaveType.TRIANGLE, 0.8f),
                    MelodicNote(D5, halfBeatMs, WaveType.SINE, 0.7f),
                    MelodicNote(C5, halfBeatMs, WaveType.SINE, 0.7f),
                    MelodicNote(G4, beatMs, WaveType.TRIANGLE, 0.8f),

                    // Measure 3 - Wandering Minstrel (F major / Dm)
                    MelodicNote(F4, halfBeatMs, WaveType.TRIANGLE, 0.9f),
                    MelodicNote(A4, halfBeatMs, WaveType.SINE, 0.7f),
                    MelodicNote(D5, beatMs, WaveType.TRIANGLE, 0.8f),
                    MelodicNote(C5, halfBeatMs, WaveType.SINE, 0.7f),
                    MelodicNote(B4, halfBeatMs, WaveType.SINE, 0.7f),
                    MelodicNote(A4, beatMs, WaveType.TRIANGLE, 0.8f),

                    // Measure 4 - Rest by Fireplace (Em -> Am)
                    MelodicNote(E4, halfBeatMs, WaveType.TRIANGLE, 0.9f),
                    MelodicNote(B3, halfBeatMs, WaveType.SINE, 0.7f),
                    MelodicNote(E4, beatMs, WaveType.TRIANGLE, 0.8f),
                    MelodicNote(G4, halfBeatMs, WaveType.SINE, 0.7f),
                    MelodicNote(B4, halfBeatMs, WaveType.SINE, 0.7f),
                    MelodicNote(A4, beatMs * 2, WaveType.TRIANGLE, 0.85f),

                    // Measure 5 - Festive Mug Clink (G major)
                    MelodicNote(G4, halfBeatMs, WaveType.TRIANGLE, 0.8f),
                    MelodicNote(B4, halfBeatMs, WaveType.SINE, 0.7f),
                    MelodicNote(D5, beatMs, WaveType.TRIANGLE, 0.8f),
                    MelodicNote(E5, halfBeatMs, WaveType.SINE, 0.75f),
                    MelodicNote(D5, halfBeatMs, WaveType.SINE, 0.75f),
                    MelodicNote(B4, beatMs, WaveType.TRIANGLE, 0.8f),

                    // Measure 6 - Lullaby Resolve
                    MelodicNote(A3, halfBeatMs, WaveType.TRIANGLE, 0.9f),
                    MelodicNote(C4, halfBeatMs, WaveType.SINE, 0.7f),
                    MelodicNote(E4, beatMs, WaveType.TRIANGLE, 0.8f),
                    MelodicNote(A4, beatMs * 2, WaveType.TRIANGLE, 0.85f)
                )

                // Synthesize melody buffer
                var totalSamples = 0
                songMelody.forEach { note ->
                    totalSamples += (sampleRate * (note.ms / 1000.0)).toInt()
                }

                val fullBuffer = ShortArray(totalSamples)
                var writeIdx = 0
                for (note in songMelody) {
                    val pcm = generateTonePcm(note.freq, note.ms, note.wave, note.vol * 0.4f)
                    for (sample in pcm) {
                        if (writeIdx < fullBuffer.size) {
                            fullBuffer[writeIdx++] = sample
                        }
                    }
                }

                // AudioTrack in static loop mode
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(fullBuffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(fullBuffer, 0, fullBuffer.size)
                track.setVolume(musicVolume)
                track.setLoopPoints(0, fullBuffer.size, -1) // Infinite loop
                track.play()
                musicTrack = track
            } catch (e: Exception) {
                // Graceful fallback
            }
        }
    }

    fun pauseBgm() {
        isPlayingBgm = false
        stopBgm()
    }

    fun toggleBgm(): Boolean {
        if (isPlayingBgm) {
            pauseBgm()
            return false
        } else {
            startBgm()
            return true
        }
    }

    private fun stopBgm() {
        bgmJob?.cancel()
        bgmJob = null
        try {
            musicTrack?.stop()
            musicTrack?.release()
            musicTrack = null
        } catch (ignored: Exception) {}
    }

    fun playJump() {
        if (!isSoundEnabled) return
        scope.launch {
            // Frequency sweep upward: 220Hz to 660Hz in 120ms
            generateChirp(startFreq = 220f, endFreq = 660f, durationMs = 120, waveType = WaveType.SQUARE, volume = 0.25f)
        }
    }

    fun playCoin() {
        if (!isSoundEnabled) return
        scope.launch {
            // Classic two-tone coin: B5 (988Hz) for 60ms then E6 (1318Hz) for 140ms
            val tone1 = generateTonePcm(988f, 60, WaveType.SQUARE, 0.25f)
            val tone2 = generateTonePcm(1318f, 140, WaveType.SQUARE, 0.25f)
            playRawPcm(tone1 + tone2)
        }
    }

    fun playHit() {
        if (!isSoundEnabled) return
        scope.launch {
            // Downward buzz 150Hz to 60Hz
            generateChirp(startFreq = 150f, endFreq = 60f, durationMs = 180, waveType = WaveType.SAWTOOTH, volume = 0.35f)
        }
    }

    fun playHeal() {
        if (!isSoundEnabled) return
        scope.launch {
            // Magical upward cascade: C5(523Hz), E5(659Hz), G5(784Hz), C6(1046Hz)
            val p1 = generateTonePcm(523f, 60, WaveType.SINE, 0.25f)
            val p2 = generateTonePcm(659f, 60, WaveType.SINE, 0.25f)
            val p3 = generateTonePcm(784f, 60, WaveType.SINE, 0.25f)
            val p4 = generateTonePcm(1046f, 150, WaveType.SINE, 0.25f)
            playRawPcm(p1 + p2 + p3 + p4)
        }
    }

    fun playLevelUp() {
        if (!isSoundEnabled) return
        scope.launch {
            // Fanfare: C5, E5, G5, B5, C6 triumph
            val p1 = generateTonePcm(523f, 80, WaveType.SQUARE, 0.25f)
            val p2 = generateTonePcm(659f, 80, WaveType.SQUARE, 0.25f)
            val p3 = generateTonePcm(784f, 80, WaveType.SQUARE, 0.25f)
            val p4 = generateTonePcm(988f, 80, WaveType.SQUARE, 0.25f)
            val p5 = generateTonePcm(1046f, 260, WaveType.SQUARE, 0.28f)
            playRawPcm(p1 + p2 + p3 + p4 + p5)
        }
    }

    fun playClick() {
        if (!isSoundEnabled) return
        scope.launch {
            val pcm = generateTonePcm(880f, 25, WaveType.SINE, 0.15f)
            playRawPcm(pcm)
        }
    }

    fun playEggHatch() {
        if (!isSoundEnabled) return
        scope.launch {
            val p1 = generateTonePcm(440f, 60, WaveType.TRIANGLE, 0.25f)
            val p2 = generateTonePcm(554f, 60, WaveType.TRIANGLE, 0.25f)
            val p3 = generateTonePcm(659f, 90, WaveType.TRIANGLE, 0.25f)
            val p4 = generateTonePcm(880f, 200, WaveType.TRIANGLE, 0.28f)
            playRawPcm(p1 + p2 + p3 + p4)
        }
    }

    private enum class WaveType { SINE, SQUARE, SAWTOOTH, TRIANGLE }

    private fun generateTonePcm(frequency: Float, durationMs: Int, waveType: WaveType, volume: Float): ShortArray {
        val sampleRate = 22050
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = 1.0 - (i.toDouble() / numSamples) * 0.3 // slight natural decay
            val wave = when (waveType) {
                WaveType.SINE -> sin(2.0 * Math.PI * frequency * t)
                WaveType.SQUARE -> if (sin(2.0 * Math.PI * frequency * t) >= 0) 0.8 else -0.8
                WaveType.SAWTOOTH -> (2.0 * (t * frequency - Math.floor(t * frequency + 0.5)))
                WaveType.TRIANGLE -> (2.0 * Math.abs(2.0 * (t * frequency - Math.floor(t * frequency + 0.5))) - 1.0)
            }
            buffer[i] = (wave * Short.MAX_VALUE * volume * decay).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateChirp(startFreq: Float, endFreq: Float, durationMs: Int, waveType: WaveType, volume: Float) {
        val sampleRate = 22050
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)

        var phase = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            phase += 2.0 * Math.PI * currentFreq / sampleRate
            val decay = 1.0 - progress * 0.4
            val wave = when (waveType) {
                WaveType.SINE -> sin(phase)
                WaveType.SQUARE -> if (sin(phase) >= 0) 0.8 else -0.8
                WaveType.SAWTOOTH -> (2.0 * (phase / (2.0 * Math.PI) - Math.floor(phase / (2.0 * Math.PI) + 0.5)))
                WaveType.TRIANGLE -> (2.0 * Math.abs(2.0 * (phase / (2.0 * Math.PI) - Math.floor(phase / (2.0 * Math.PI) + 0.5))) - 1.0)
            }
            buffer[i] = (wave * Short.MAX_VALUE * volume * decay).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playRawPcm(buffer)
    }

    private fun playRawPcm(pcm: ShortArray) {
        try {
            val sampleRate = 22050
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(pcm.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(pcm, 0, pcm.size)
            audioTrack.play()
            // Clean up after playback
            scope.launch {
                val delayTime = (pcm.size.toDouble() / sampleRate * 1000).toLong() + 50
                kotlinx.coroutines.delay(delayTime)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (ignored: Exception) {}
            }
        } catch (e: Exception) {
            // Graceful fallback if AudioTrack fails on particular platform
        }
    }
}
