package com.dedio.fretboardstudio.ui.main

import com.dedio.fretboardstudio.model.DotLabelMode
import com.dedio.fretboardstudio.model.NoteNotation
import com.dedio.fretboardstudio.model.Pitch
import com.dedio.fretboardstudio.model.ScaleType
import com.dedio.fretboardstudio.model.TriadType
import com.dedio.fretboardstudio.ui.TriadViewModel
import com.dedio.fretboardstudio.ui.VisualizerMode
import com.dedio.fretboardstudio.ui.selectedChordSlot
import com.dedio.fretboardstudio.ui.visibleChordSlots
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
        assertEquals(24 * 6 + 6, state.allFretNotes.size)
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

    @Test
    fun selectingAndEditingASlotKeepsTheChordStateInSync() = runTest {
        val viewModel = TriadViewModel()
        viewModel.setVisualizerMode(VisualizerMode.TRIADS)
        viewModel.addChordSlot()

        val secondId = viewModel.uiState.first().selectedSlotId
        viewModel.updateChordSlot(secondId) { it.copy(root = Pitch(7), quality = TriadType.MINOR) }

        val edited = viewModel.uiState.first()
        assertEquals(Pitch(7), edited.selectedRoot)
        assertEquals(TriadType.MINOR, edited.selectedTriadType)
        assertEquals(Pitch(7), edited.triadSequenceSlots.first { it.id == secondId }.root)

        // Switching back to the first slot must immediately update the displayed chord.
        viewModel.selectSlot(edited.triadSequenceSlots.first().id)
        val switched = viewModel.uiState.first()
        assertEquals(Pitch(0), switched.selectedRoot)
        assertEquals(TriadType.MAJOR, switched.selectedTriadType)
    }

    @Test
    fun triadCardCanBeCollapsedWithoutLosingSelection() = runTest {
        val viewModel = TriadViewModel()
        viewModel.setVisualizerMode(VisualizerMode.TRIADS)
        val firstId = viewModel.uiState.first().selectedSlotId
        assertEquals(firstId, viewModel.uiState.first().expandedSlotId)

        viewModel.toggleSlotExpanded(firstId)
        assertNull(viewModel.uiState.first().expandedSlotId)
        assertEquals(firstId, viewModel.uiState.first().selectedSlotId)

        viewModel.addChordSlot()
        val secondId = viewModel.uiState.first().selectedSlotId
        assertEquals(secondId, viewModel.uiState.first().expandedSlotId)

        viewModel.toggleSlotExpanded(secondId)
        assertNull(viewModel.uiState.first().expandedSlotId)
        assertEquals(secondId, viewModel.uiState.first().selectedSlotId)
    }

    @Test
    fun isolationShowsOnlyTheSelectedChord() = runTest {
        val viewModel = TriadViewModel()
        viewModel.addChordSlot()
        val secondId = viewModel.uiState.first().selectedSlotId
        viewModel.updateChordSlot(secondId) { it.copy(root = Pitch(7), quality = TriadType.MINOR) }

        val allNotes = viewModel.uiState.first().allFretNotes
        viewModel.setSlotIsolation(true)
        val isolatedNotes = viewModel.uiState.first().allFretNotes

        assertTrue(isolatedNotes.isNotEmpty())
        assertTrue(isolatedNotes.all { it.sharedOccupants.all { o -> o.slotId == secondId } })
        assertTrue(isolatedNotes.size < allNotes.size)

        viewModel.setSlotIsolation(false)
        assertEquals(allNotes.size, viewModel.uiState.first().allFretNotes.size)
    }

    @Test
    fun visibilityToggleRemovesAndRestoresAChordFromTheNeck() = runTest {
        val viewModel = TriadViewModel()
        viewModel.setVisualizerMode(VisualizerMode.TRIADS)
        viewModel.addChordSlot()
        val firstId = viewModel.uiState.first().triadSequenceSlots.first().id
        val secondId = viewModel.uiState.first().selectedSlotId

        val allNotes = viewModel.uiState.first().allFretNotes
        assertTrue(allNotes.any { it.sharedOccupants.any { o -> o.slotId == firstId } })
        assertTrue(allNotes.any { it.sharedOccupants.any { o -> o.slotId == secondId } })

        // Turn the second chord off: its notes disappear, shared markers keep only the first.
        viewModel.toggleSlotVisibility(secondId)
        val withoutSecond = viewModel.uiState.first().allFretNotes
        assertTrue(withoutSecond.none { it.sharedOccupants.any { o -> o.slotId == secondId } })
        assertTrue(withoutSecond.any { it.sharedOccupants.any { o -> o.slotId == firstId } })

        // Turn it back on: the chord reappears with its own slot colour.
        viewModel.toggleSlotVisibility(secondId)
        val restored = viewModel.uiState.first().allFretNotes
        assertEquals(allNotes.size, restored.size)
        assertTrue(restored.any { it.sharedOccupants.any { o -> o.slotId == secondId } })
    }

    @Test
    fun tonicHighlightCanBeToggledWithoutAffectingNotes() = runTest {
        val viewModel = TriadViewModel()
        assertFalse(viewModel.uiState.first().highlightTonic)

        val notesBefore = viewModel.uiState.first().allFretNotes
        viewModel.setHighlightTonic(true)
        assertTrue(viewModel.uiState.first().highlightTonic)
        assertEquals(notesBefore, viewModel.uiState.first().allFretNotes)

        viewModel.setHighlightTonic(false)
        assertFalse(viewModel.uiState.first().highlightTonic)
    }

    @Test
    fun headerAndSelectionFollowVisibleChordsWhenTheSelectedOneIsHidden() = runTest {
        val viewModel = TriadViewModel()
        viewModel.setVisualizerMode(VisualizerMode.TRIADS)
        viewModel.addChordSlot()
        val firstId = viewModel.uiState.first().triadSequenceSlots.first().id
        val secondId = viewModel.uiState.first().selectedSlotId

        viewModel.toggleSlotVisibility(secondId)
        val state = viewModel.uiState.first()

        assertEquals(1, state.visibleChordSlots.size)
        assertEquals(firstId, state.selectedChordSlot?.id)
        assertEquals(Pitch(0), state.selectedRoot)
    }

    @Test
    fun soloNeverLeavesAnEmptyNeckWhenTheSelectedChordIsHidden() = runTest {
        val viewModel = TriadViewModel()
        viewModel.setVisualizerMode(VisualizerMode.TRIADS)
        viewModel.addChordSlot()
        val firstId = viewModel.uiState.first().triadSequenceSlots.first().id
        val secondId = viewModel.uiState.first().selectedSlotId

        viewModel.toggleSlotVisibility(secondId)
        viewModel.setSlotIsolation(true)
        val afterSolo = viewModel.uiState.first()
        assertTrue(afterSolo.isolateSelectedSlot)
        assertEquals(firstId, afterSolo.selectedSlotId)
        assertTrue(afterSolo.allFretNotes.isNotEmpty())
        assertTrue(afterSolo.allFretNotes.all { it.sharedOccupants.all { o -> o.slotId == firstId } })

        // Hiding the isolated chord again must also keep at least one visible chord.
        viewModel.toggleSlotVisibility(firstId)
        val afterHidingAll = viewModel.uiState.first()
        assertFalse(afterHidingAll.isolateSelectedSlot)
        assertTrue(afterHidingAll.allFretNotes.isEmpty())
    }

    @Test
    fun addingAChordSyncsHeaderFieldsEvenFromAnotherMode() = runTest {
        val viewModel = TriadViewModel()
        viewModel.selectRoot(Pitch(9))
        viewModel.addChordSlot()

        val state = viewModel.uiState.first()
        val newSlot = state.triadSequenceSlots.last()
        assertEquals(newSlot.id, state.selectedSlotId)
        assertEquals(newSlot.id, state.expandedSlotId)
        assertEquals(newSlot.root, state.selectedRoot)
        assertEquals(newSlot.quality, state.selectedTriadType)
    }

    @Test
    fun movingASlotWithAnInvalidIndexIsIgnored() = runTest {
        val viewModel = TriadViewModel()
        viewModel.setVisualizerMode(VisualizerMode.TRIADS)
        val before = viewModel.uiState.first()

        viewModel.moveSlot(-1, -1)
        viewModel.moveSlot(99, 1)
        viewModel.moveSlot(0, 5)

        assertEquals(before.triadSequenceSlots, viewModel.uiState.first().triadSequenceSlots)
        assertEquals(before.allFretNotes, viewModel.uiState.first().allFretNotes)
    }
}
