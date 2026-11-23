package com.dedio.fretboardstudio.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

/**
 * Synthesized PCM clicks driven by a single-threaded clock, fully decoupled from Compose.
 * Each click uses a short-lived MODE_STATIC AudioTrack: the whole sample is queued before play(),
 * so long gaps at low BPM cannot underrun the stream and swallow the next click.
 */
open class MetronomeEngine(
    var onBeat: (Int) -> Unit = {},
    private val sampleRate: Int = 44_100,
) {
    private val clockExecutor: ExecutorService =
        Executors.newSingleThreadExecutor { Thread(it, "metronome-clock") }
    private val clockDispatcher = clockExecutor.asCoroutineDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + clockDispatcher)

    private val normalClickBytes = clickBytes(accent = false)
    private val accentClickBytes = clickBytes(accent = true)
    private val activeClicks = Collections.synchronizedSet(mutableSetOf<AudioTrack>())

    @Volatile private var currentBpm = MetronomeUiState.DEFAULT_BPM
    @Volatile private var currentMeter = MetronomeMeter.FOUR_FOUR
    @Volatile private var currentAccentBeat: Int? = 0

    private var beatJob: Job? = null
    @Volatile private var running = false
    private var released = false
    private var audioUnavailable = false

    /** Safe to call while the clock is running; the next beat picks up the new settings. */
    open fun update(bpm: Int, meter: MetronomeMeter, accentBeat: Int?) {
        currentBpm = bpm.coerceIn(MetronomeUiState.MIN_BPM, MetronomeUiState.MAX_BPM)
        currentMeter = meter
        currentAccentBeat = accentBeat
    }

    open fun start(bpm: Int, meter: MetronomeMeter, accentBeat: Int?) {
        if (released) return
        update(bpm, meter, accentBeat)
        if (beatJob?.isActive == true) return

        stop()
        running = true
        beatJob = scope.launch {
            var nextBeatNs = System.nanoTime()
            var beat = 0
            while (isActive) {
                val beatsInBar = currentMeter.beatsPerBar
                if (beat >= beatsInBar) beat = 0
                playClick(accent = currentAccentBeat == beat)
                onBeat(beat)

                beat = (beat + 1) % beatsInBar
                val intervalNs = 60_000_000_000L / currentBpm
                nextBeatNs += intervalNs
                val remainingNs = nextBeatNs - System.nanoTime()
                if (remainingNs > 0) {
                    val delayMs = ((remainingNs + 999_999L) / 1_000_000L).coerceAtLeast(1L)
                    delay(delayMs.milliseconds)
                } else {
                    // Re-anchor after a stall instead of emitting a burst of catch-up clicks.
                    nextBeatNs = System.nanoTime() + intervalNs
                    delay((intervalNs / 1_000_000L).milliseconds)
                }
            }
        }
    }

    /** Cancels the clock and cuts off any click that is still sounding. */
    open fun stop() {
        running = false
        beatJob?.cancel()
        beatJob = null
        releaseAllClicks()
    }

    open fun release() {
        if (released) return
        stop()
        scope.cancel()
        runCatching { clockDispatcher.close() }
        clockExecutor.shutdownNow()
        released = true
    }

    private fun playClick(accent: Boolean) {
        if (released || !running || audioUnavailable) return
        val bytes = if (accent) accentClickBytes else normalClickBytes
        val click = try {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                )
                .setBufferSizeInBytes(bytes.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
        } catch (_: Exception) {
            audioUnavailable = true
            return
        }

        val written = try {
            click.write(bytes, 0, bytes.size)
        } catch (_: Exception) {
            runCatching { click.release() }
            audioUnavailable = true
            return
        }
        if (written < bytes.size) {
            runCatching { click.release() }
            audioUnavailable = true
            return
        }

        if (!running || released) {
            runCatching { click.release() }
            return
        }
        activeClicks.add(click)
        click.play()
        val releaseAfterMs = (((bytes.size / 2) * 1_000L) / sampleRate) + RELEASE_MARGIN_MS
        scope.launch {
            delay(releaseAfterMs.milliseconds)
            releaseClick(click)
        }
    }

    private fun releaseClick(click: AudioTrack) {
        if (activeClicks.remove(click)) {
            runCatching { click.release() }
        }
    }

    private fun releaseAllClicks() {
        synchronized(activeClicks) {
            activeClicks.forEach { click -> runCatching { click.release() } }
            activeClicks.clear()
        }
    }

    /** Short sine blip with a squared fade so there is no click at the buffer edges. */
    private fun clickBytes(accent: Boolean): ByteArray {
        val durationSeconds = if (accent) 0.075 else 0.045
        val frequency = if (accent) 1_800.0 else 1_100.0
        val sampleCount = (sampleRate * durationSeconds).toInt()
        val bytes = ByteArray(sampleCount * 2)
        for (index in 0 until sampleCount) {
            val progress = index.toDouble() / sampleCount
            val envelope = (1.0 - progress) * (1.0 - progress)
            val wave = sin(2.0 * PI * frequency * (index.toDouble() / sampleRate))
            val sample = (wave * envelope * Short.MAX_VALUE * 0.55).toInt().toShort()
            bytes[index * 2] = sample.toByte()
            bytes[(index * 2) + 1] = (sample.toInt() shr 8).toByte()
        }
        return bytes
    }

    private companion object {
        const val RELEASE_MARGIN_MS = 40L
    }
}
