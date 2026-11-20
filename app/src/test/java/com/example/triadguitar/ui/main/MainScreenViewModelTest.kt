package com.example.triadguitar.ui.main

import com.example.triadguitar.model.DotLabelMode
import com.example.triadguitar.model.NoteNotation
import com.example.triadguitar.model.Pitch
import com.example.triadguitar.model.ScaleType
import com.example.triadguitar.ui.TriadViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MainScreenViewModelTest {

    @Test
    fun initialStateShowsTheFullMajorScale() = runTest {
        val viewModel = TriadViewModel()
        val state = viewModel.uiState.first()

        assertEquals(Pitch(0), state.selectedRoot)
        assertEquals(ScaleType.MAJOR, state.selectedScale)
        assertTrue(state.selectedPositions.isEmpty())
        assertEquals(22 * 6 + 6, state.allFretNotes.size)
    }

    @Test
    fun selectingRootAndScaleRecalculatesScaleNotes() = runTest {
        val viewModel = TriadViewModel()

        viewModel.selectRoot(Pitch(9))
        viewModel.selectScale(ScaleType.DORIAN)

        val state = viewModel.uiState.first()
        assertEquals(Pitch(9), state.selectedRoot)
        assertEquals(ScaleType.DORIAN, state.selectedScale)
        assertTrue(state.allFretNotes.any { it.isInScale && it.scaleDegreeLabel == "♭3" })
        assertNotNull(state.allFretNotes.firstOrNull { it.pitch == Pitch(9) })
    }

    @Test
    fun selectingBoxesAllowsMultiplePositions() = runTest {
        val viewModel = TriadViewModel()

        viewModel.togglePosition(1)
        viewModel.togglePosition(3)
        assertEquals(setOf(1, 3), viewModel.uiState.first().selectedPositions)

        viewModel.togglePosition(1)
        assertEquals(setOf(3), viewModel.uiState.first().selectedPositions)

        viewModel.clearPositionSelection()
        assertTrue(viewModel.uiState.first().selectedPositions.isEmpty())
    }

    @Test
    fun notationAndDotLabelModeCanBeChanged() = runTest {
        val viewModel = TriadViewModel()

        viewModel.setNotation(NoteNotation.ENGLISH)
        viewModel.setDotLabelMode(DotLabelMode.INTERVALS)

        val state = viewModel.uiState.first()
        assertEquals(NoteNotation.ENGLISH, state.notation)
        assertEquals(DotLabelMode.INTERVALS, state.dotLabelMode)
    }
}
