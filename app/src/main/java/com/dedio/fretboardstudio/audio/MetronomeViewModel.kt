package com.dedio.fretboardstudio.audio

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.roundToInt

/** Holds metronome state only; scale/triad state stays in [com.dedio.fretboardstudio.ui.TriadViewModel]. */
class MetronomeViewModel(
    private val engineFactory: () -> MetronomeEngine = { MetronomeEngine() },
) : ViewModel() {

    private val _uiState = MutableStateFlow(MetronomeUiState())
    val uiState: StateFlow<MetronomeUiState> = _uiState.asStateFlow()

    private var engine: MetronomeEngine? = null
    private val tapTimes = mutableListOf<Long>()

    fun setBpm(bpm: Int) {
        val clamped = bpm.coerceIn(MetronomeUiState.MIN_BPM, MetronomeUiState.MAX_BPM)
        val wasPlaying = _uiState.value.isPlaying
        _uiState.update { it.copy(bpm = clamped) }
        if (wasPlaying) syncEngine()
    }

    fun adjustBpm(delta: Int) = setBpm(_uiState.value.bpm + delta)

    fun setMeter(meter: MetronomeMeter) {
        val wasPlaying = _uiState.value.isPlaying
        _uiState.update { current ->
            current.copy(
                meter = meter,
                accentBeat = current.accentBeat?.coerceAtMost(meter.beatsPerBar - 1),
                currentBeat = -1,
            )
        }
        if (wasPlaying) syncEngine()
    }

    /** Pass null to turn the accent off; otherwise 0 is the first beat of the bar. */
    fun setAccentBeat(beat: Int?) {
        val wasPlaying = _uiState.value.isPlaying
        _uiState.update { current ->
            current.copy(accentBeat = beat?.coerceIn(0, current.meter.beatsPerBar - 1))
        }
        if (wasPlaying) syncEngine()
    }

    fun setExpanded(expanded: Boolean) {
        _uiState.update { it.copy(isExpanded = expanded) }
    }

    fun toggleExpanded() = setExpanded(!_uiState.value.isExpanded)

    /** Hiding the pill never pauses the clock. */
    fun setVisible(visible: Boolean) {
        _uiState.update { it.copy(isVisible = visible) }
    }

    fun toggleVisible() = setVisible(!_uiState.value.isVisible)

    /** Averages the last taps so an unsteady hand does not swing the tempo wildly. */
    fun tapTempo(nowMillis: Long = System.currentTimeMillis()) {
        tapTimes.lastOrNull()?.let { lastTap ->
            if ((nowMillis - lastTap) > TAP_RESET_MS) tapTimes.clear()
        }
        tapTimes.add(nowMillis)
        if (tapTimes.size > MAX_TAPS) tapTimes.removeAt(0)
        if (tapTimes.size < 2) return

        val averageInterval = tapTimes.asSequence().zipWithNext { first, second -> second - first }.average()
        if (averageInterval > 0) {
            setBpm((60_000.0 / averageInterval).roundToInt())
        }
    }

    fun togglePlay() {
        if (_uiState.value.isPlaying) {
            pause()
        } else {
            val metronome = engine ?: engineFactory().also { created ->
                created.onBeat = ::onBeat
                engine = created
            }
            val (bpm, meter, accentBeat, _, _, _, _) = _uiState.value
            metronome.start(bpm, meter, accentBeat)
            _uiState.update { it.copy(isPlaying = true, currentBeat = -1) }
        }
    }

    /** Called when the app goes to the background; the user must press play again on return. */
    fun pause() {
        engine?.stop()
        _uiState.update { it.copy(isPlaying = false, currentBeat = -1) }
    }

    fun release() {
        engine?.release()
        engine = null
        tapTimes.clear()
        _uiState.update { it.copy(isPlaying = false, currentBeat = -1) }
    }

    override fun onCleared() {
        release()
        super.onCleared()
    }

    /** Pushes the latest settings into an already running clock without restarting the beat. */
    private fun syncEngine() {
        engine?.let { metronome ->
            val (bpm, meter, accentBeat, _, _, _, _) = _uiState.value
            metronome.update(bpm, meter, accentBeat)
        }
    }

    private fun onBeat(beat: Int) {
        _uiState.update { current ->
            if (current.isPlaying) current.copy(currentBeat = beat) else current
        }
    }

    private companion object {
        const val TAP_RESET_MS = 2_000L
        const val MAX_TAPS = 6
    }
}
