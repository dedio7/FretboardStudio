package com.dedio.fretboardstudio.audio

/** Time signatures supported by the metronome. The pulse is a quarter note except in 6/8, where it is an eighth. */
enum class MetronomeMeter(val label: String, val beatsPerBar: Int) {
    FOUR_FOUR("4/4", 4),
    THREE_FOUR("3/4", 3),
    SIX_EIGHT("6/8", 6)
}

/** State owned exclusively by [MetronomeViewModel], kept separate from scale/triad selections. */
data class MetronomeUiState(
    val bpm: Int = DEFAULT_BPM,
    val meter: MetronomeMeter = MetronomeMeter.FOUR_FOUR,
    /** Zero-based beat to accent, or null for no accent. Kept in range by [MetronomeViewModel.setMeter]. */
    val accentBeat: Int? = 0,
    val isPlaying: Boolean = false,
    /** Index of the lit beat, or -1 while stopped. */
    val currentBeat: Int = -1,
    val isExpanded: Boolean = false,
    /** False while the floating bar is hidden; the clock keeps running. */
    val isVisible: Boolean = true,
) {
    companion object {
        const val MIN_BPM = 40
        const val MAX_BPM = 240
        const val DEFAULT_BPM = 100
    }
}
