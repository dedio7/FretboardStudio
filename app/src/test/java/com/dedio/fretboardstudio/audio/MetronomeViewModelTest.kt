package com.dedio.fretboardstudio.audio

import com.dedio.fretboardstudio.ui.TriadViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MetronomeViewModelTest {

    private class FakeEngine : MetronomeEngine() {
        var startCount = 0
        var updateCount = 0
        var stopCount = 0
        var releaseCount = 0
        var lastBpm = -1
        var lastMeter = MetronomeMeter.FOUR_FOUR
        var lastAccentBeat: Int? = 0

        override fun start(bpm: Int, meter: MetronomeMeter, accentBeat: Int?) {
            startCount++
            update(bpm, meter, accentBeat)
        }

        override fun update(bpm: Int, meter: MetronomeMeter, accentBeat: Int?) {
            updateCount++
            lastBpm = bpm
            lastMeter = meter
            lastAccentBeat = accentBeat
        }

        override fun stop() {
            stopCount++
        }

        override fun release() {
            releaseCount++
            super.release()
        }
    }

    @Test
    fun bpmStaysInsideTheSupportedRange() {
        val viewModel = MetronomeViewModel { FakeEngine() }

        viewModel.setBpm(10)
        assertEquals(MetronomeUiState.MIN_BPM, viewModel.uiState.value.bpm)

        viewModel.setBpm(999)
        assertEquals(MetronomeUiState.MAX_BPM, viewModel.uiState.value.bpm)

        viewModel.adjustBpm(-5)
        assertEquals(235, viewModel.uiState.value.bpm)
    }

    @Test
    fun tapTempoAveragesTheRecentIntervals() {
        val viewModel = MetronomeViewModel { FakeEngine() }

        viewModel.tapTempo(1_000L)
        assertEquals(MetronomeUiState.DEFAULT_BPM, viewModel.uiState.value.bpm)

        viewModel.tapTempo(1_500L)
        assertEquals(120, viewModel.uiState.value.bpm)

        // Average of 500 ms and 1000 ms = 750 ms => 80 BPM.
        viewModel.tapTempo(2_500L)
        assertEquals(80, viewModel.uiState.value.bpm)
    }

    @Test
    fun tapTempoResetsAfterALongPause() {
        val viewModel = MetronomeViewModel { FakeEngine() }

        viewModel.tapTempo(1_000L)
        viewModel.tapTempo(1_200L)
        assertEquals(MetronomeUiState.MAX_BPM, viewModel.uiState.value.bpm)

        // More than two seconds since the last tap: start a fresh measurement.
        viewModel.tapTempo(10_000L)
        assertEquals(MetronomeUiState.MAX_BPM, viewModel.uiState.value.bpm)
        viewModel.tapTempo(10_500L)
        assertEquals(120, viewModel.uiState.value.bpm)
    }

    @Test
    fun tempoChangesUpdateTheRunningClockWithoutRestartingIt() {
        val engine = FakeEngine()
        val viewModel = MetronomeViewModel { engine }

        viewModel.setBpm(150)
        viewModel.setMeter(MetronomeMeter.SIX_EIGHT)
        assertEquals(0, engine.startCount)
        assertEquals(0, engine.updateCount)

        viewModel.togglePlay()
        assertTrue(viewModel.uiState.value.isPlaying)
        assertEquals(1, engine.startCount)
        assertEquals(150, engine.lastBpm)
        assertEquals(MetronomeMeter.SIX_EIGHT, engine.lastMeter)

        viewModel.setBpm(90)
        viewModel.setAccentBeat(2)
        assertEquals(1, engine.startCount)
        assertEquals(3, engine.updateCount)
        assertEquals(90, engine.lastBpm)
        assertEquals(2, engine.lastAccentBeat)

        viewModel.pause()
        assertFalse(viewModel.uiState.value.isPlaying)
        assertEquals(-1, viewModel.uiState.value.currentBeat)
        assertTrue(engine.stopCount > 0)

        viewModel.togglePlay()
        assertEquals(2, engine.startCount)
        assertEquals(90, engine.lastBpm)
        assertEquals(2, engine.lastAccentBeat)

        viewModel.release()
        assertEquals(1, engine.releaseCount)
    }

    @Test
    fun accentBeatCanBeMovedDisabledAndClampedToTheMeter() {
        val engine = FakeEngine()
        val viewModel = MetronomeViewModel { engine }

        viewModel.setAccentBeat(2)
        assertEquals(2, viewModel.uiState.value.accentBeat)

        viewModel.setAccentBeat(5)
        assertEquals(3, viewModel.uiState.value.accentBeat)

        viewModel.setMeter(MetronomeMeter.THREE_FOUR)
        assertEquals(2, viewModel.uiState.value.accentBeat)

        viewModel.setMeter(MetronomeMeter.SIX_EIGHT)
        assertEquals(2, viewModel.uiState.value.accentBeat)

        viewModel.setAccentBeat(null)
        assertNull(viewModel.uiState.value.accentBeat)

        viewModel.togglePlay()
        assertNull(engine.lastAccentBeat)
    }

    @Test
    fun metronomeStateNeverTouchesTriadState() {
        val engine = FakeEngine()
        val viewModel = MetronomeViewModel { engine }
        val triadViewModel = TriadViewModel()
        val triadBefore = triadViewModel.uiState.value

        viewModel.togglePlay()
        viewModel.setBpm(180)
        viewModel.pause()
        viewModel.release()

        assertEquals(triadBefore, triadViewModel.uiState.value)
    }
}
