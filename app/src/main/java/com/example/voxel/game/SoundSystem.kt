package com.example.voxel.game

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

/**
 * Procedural retro sound synthesizer for voxel game audio.
 * Generates instant crisp audio for block breaking, placement, footsteps, hits, and chimes.
 */
class SoundSystem {

    private val sampleRate = 22050
    private val scope = CoroutineScope(Dispatchers.Default)
    var volume: Float = 0.8f
    var enabled: Boolean = true

    fun playBlockBreak() {
        if (!enabled) return
        scope.launch {
            // Crunch noise burst
            val duration = 0.12f
            val samples = (sampleRate * duration).toInt()
            val buffer = ShortArray(samples)
            for (i in 0 until samples) {
                val decay = (1.0f - i.toFloat() / samples)
                val noise = (Random.nextFloat() * 2f - 1f) * decay * 0.7f
                val tone = sin(2.0 * Math.PI * 140.0 * i / sampleRate).toFloat() * decay * 0.3f
                val sample = ((noise + tone) * Short.MAX_VALUE * volume).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcm(buffer)
        }
    }

    fun playBlockPlace() {
        if (!enabled) return
        scope.launch {
            // Crisp wooden / stone tap
            val duration = 0.08f
            val samples = (sampleRate * duration).toInt()
            val buffer = ShortArray(samples)
            for (i in 0 until samples) {
                val decay = (1.0f - i.toFloat() / samples) * (1.0f - i.toFloat() / samples)
                val tone = sin(2.0 * Math.PI * 220.0 * i / sampleRate).toFloat() * decay
                val noise = (Random.nextFloat() * 2f - 1f) * decay * 0.2f
                val sample = ((tone + noise) * Short.MAX_VALUE * volume).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcm(buffer)
        }
    }

    fun playFootstep() {
        if (!enabled) return
        scope.launch {
            val duration = 0.05f
            val samples = (sampleRate * duration).toInt()
            val buffer = ShortArray(samples)
            for (i in 0 until samples) {
                val decay = (1.0f - i.toFloat() / samples)
                val noise = (Random.nextFloat() * 2f - 1f) * decay * 0.25f
                val sample = (noise * Short.MAX_VALUE * volume * 0.5f).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcm(buffer)
        }
    }

    fun playPlayerHurt() {
        if (!enabled) return
        scope.launch {
            val duration = 0.2f
            val samples = (sampleRate * duration).toInt()
            val buffer = ShortArray(samples)
            for (i in 0 until samples) {
                val progress = i.toFloat() / samples
                val freq = 200.0 - progress * 90.0
                val tone = sin(2.0 * Math.PI * freq * i / sampleRate).toFloat() * (1f - progress)
                val sample = (tone * Short.MAX_VALUE * volume * 0.9f).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcm(buffer)
        }
    }

    fun playZombieGroan() {
        if (!enabled) return
        scope.launch {
            val duration = 0.4f
            val samples = (sampleRate * duration).toInt()
            val buffer = ShortArray(samples)
            for (i in 0 until samples) {
                val progress = i.toFloat() / samples
                val mod = sin(2.0 * Math.PI * 8.0 * i / sampleRate).toFloat()
                val freq = 85.0 + mod * 20.0
                val tone = sin(2.0 * Math.PI * freq * i / sampleRate).toFloat() * (1f - progress)
                val noise = (Random.nextFloat() * 2f - 1f) * 0.15f * (1f - progress)
                val sample = ((tone + noise) * Short.MAX_VALUE * volume * 0.7f).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcm(buffer)
        }
    }

    fun playAnimalSound(type: String) {
        if (!enabled) return
        scope.launch {
            val duration = 0.25f
            val samples = (sampleRate * duration).toInt()
            val buffer = ShortArray(samples)
            val baseFreq = when (type) {
                "cow" -> 90.0
                "sheep" -> 280.0
                else -> 350.0 // pig
            }
            for (i in 0 until samples) {
                val progress = i.toFloat() / samples
                val decay = (1.0f - progress)
                val freq = baseFreq + sin(2.0 * Math.PI * 6.0 * i / sampleRate) * 20.0
                val tone = sin(2.0 * Math.PI * freq * i / sampleRate).toFloat() * decay
                val sample = (tone * Short.MAX_VALUE * volume * 0.6f).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcm(buffer)
        }
    }

    fun playCraftSuccess() {
        if (!enabled) return
        scope.launch {
            val duration = 0.25f
            val samples = (sampleRate * duration).toInt()
            val buffer = ShortArray(samples)
            for (i in 0 until samples) {
                val progress = i.toFloat() / samples
                val freq = if (progress < 0.5f) 523.25 else 659.25 // C5 then E5
                val decay = (1f - (progress % 0.5f) * 2f)
                val tone = sin(2.0 * Math.PI * freq * i / sampleRate).toFloat() * decay
                val sample = (tone * Short.MAX_VALUE * volume * 0.7f).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcm(buffer)
        }
    }

    private fun playPcm(buffer: ShortArray) {
        try {
            val track = AudioTrack.Builder()
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
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            // Clean up after playback
            scope.launch {
                kotlinx.coroutines.delay((buffer.size * 1000L / sampleRate) + 50L)
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }
}
