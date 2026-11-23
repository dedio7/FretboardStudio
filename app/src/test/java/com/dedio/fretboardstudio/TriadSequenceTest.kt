package com.dedio.fretboardstudio

import com.dedio.fretboardstudio.model.ChordSlot
import com.dedio.fretboardstudio.model.GuitarTuning
import com.dedio.fretboardstudio.model.NoteNotation
import com.dedio.fretboardstudio.model.Pitch
import com.dedio.fretboardstudio.model.TriadRole
import com.dedio.fretboardstudio.model.TriadSequenceAggregator
import com.dedio.fretboardstudio.model.TriadType
import com.dedio.fretboardstudio.model.UiLanguage
import com.dedio.fretboardstudio.model.getSlotColorHex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TriadSequenceTest {

    private val fiveSlots = listOf(
        ChordSlot(id = "s1", root = Pitch(0), quality = TriadType.MAJOR, slotIndex = 0),
        ChordSlot(id = "s2", root = Pitch(2), quality = TriadType.MINOR, slotIndex = 1),
        ChordSlot(id = "s3", root = Pitch(4), quality = TriadType.MINOR, slotIndex = 2),
        ChordSlot(id = "s4", root = Pitch(5), quality = TriadType.MAJOR, slotIndex = 3),
        ChordSlot(id = "s5", root = Pitch(7), quality = TriadType.MAJOR, slotIndex = 4)
    )

    @Test
    fun testSlotColorsAssignedCorrectly() {
        assertEquals(0xFF00D7E8, getSlotColorHex(0))
        assertEquals(0xFFB967FF, getSlotColorHex(1))
        assertEquals(0xFFFF4FA1, getSlotColorHex(2))
        assertEquals(0xFFFFA62B, getSlotColorHex(3))
        assertEquals(0xFFB8FF2C, getSlotColorHex(4))
        // The palette wraps; exactly five slots are allowed in a sequence.
        assertEquals(0xFF00D7E8, getSlotColorHex(5))
    }

    @Test
    fun slotColorsAreDistinctFromTonicRoleColors() {
        val tonicColors = TriadRole.entries.map { it.colorHex }.toSet()
        (0..4).forEach { index ->
            assertFalse("Slot colour must not reuse a tonic-defining note colour", tonicColors.contains(getSlotColorHex(index)))
        }
    }

    @Test
    fun sharedPositionsExposeOneColourPerChord() {
        val sharedNotes = TriadSequenceAggregator.aggregate(fiveSlots)
            .filter { it.sharedOccupants.size >= 2 }

        assertTrue("Expected shared positions with five chords", sharedNotes.isNotEmpty())
        sharedNotes.forEach { note ->
            val colors = note.sharedOccupants.map { it.colorHex }
            assertEquals("Each chord needs its own sector colour", colors.size, colors.toSet().size)
            assertEquals(
                "Sectors must follow stable slot order",
                note.sharedOccupants.map { it.slotIndex },
                note.sharedOccupants.map { it.slotIndex }.sorted()
            )
        }
    }

    @Test
    fun testAggregationTwoTriadsSharedPositions() {
        val slots = listOf(
            ChordSlot(id = "c_maj", root = Pitch(0), quality = TriadType.MAJOR, slotIndex = 0),
            ChordSlot(id = "g_maj", root = Pitch(7), quality = TriadType.MAJOR, slotIndex = 1)
        )

        val aggregatedNotes = TriadSequenceAggregator.aggregate(slots, GuitarTuning.STANDARD)
        assertTrue(aggregatedNotes.isNotEmpty())

        aggregatedNotes.forEach { note ->
            note.sharedOccupants.forEach { occupant ->
                assertTrue(occupant.slotId == "c_maj" || occupant.slotId == "g_maj")
            }
        }
    }

    @Test
    fun testAggregationThreeTriadsSharedPositions() {
        assertTrue(TriadSequenceAggregator.aggregate(fiveSlots.take(3), GuitarTuning.STANDARD).isNotEmpty())
    }

    @Test
    fun allInversionStaysReadableWithFiveChords() {
        val aggregatedNotes = TriadSequenceAggregator.aggregate(fiveSlots, GuitarTuning.STANDARD)
        assertTrue(aggregatedNotes.isNotEmpty())
        // One representative voicing per inversion (3) x 3 notes x 5 chords.
        assertTrue("Expected a bounded number of badges, got ${aggregatedNotes.size}", aggregatedNotes.size <= 45)

        val allOccupantSlotIds = aggregatedNotes.flatMap { it.sharedOccupants }.map { it.slotId }.toSet()
        assertTrue(allOccupantSlotIds.size <= 5)
    }

    @Test
    fun selectedSlotIsMarkedOnFretboardNotes() {
        val notes = TriadSequenceAggregator.aggregate(fiveSlots, selectedSlotId = "s3")
        val selected = notes.flatMap { it.sharedOccupants }.filter { it.isSelected }

        assertTrue(selected.isNotEmpty())
        assertTrue("Only the selected chord should be emphasised", selected.all { it.slotId == "s3" })
    }

    @Test
    fun isolatingASlotReturnsOnlyItsNotes() {
        val isolated = TriadSequenceAggregator.aggregate(fiveSlots, isolatedSlotId = "s3")
        assertTrue(isolated.isNotEmpty())
        assertTrue(isolated.all { note -> note.sharedOccupants.all { it.slotId == "s3" } })
    }

    @Test
    fun testSlotVisibilityToggleExcludesHiddenSlots() {
        val slots = listOf(
            ChordSlot(id = "s1", root = Pitch(0), quality = TriadType.MAJOR, slotIndex = 0, isVisible = true),
            ChordSlot(id = "s2", root = Pitch(7), quality = TriadType.MAJOR, slotIndex = 1, isVisible = false)
        )

        val aggregatedNotes = TriadSequenceAggregator.aggregate(slots, GuitarTuning.STANDARD)
        val occupantSlotIds = aggregatedNotes.flatMap { it.sharedOccupants }.map { it.slotId }.toSet()

        assertTrue(occupantSlotIds.contains("s1"))
        assertFalse(occupantSlotIds.contains("s2"))
    }

    @Test
    fun chordTitleFollowsNotationAndLanguage() {
        val slot = ChordSlot(root = Pitch(0), quality = TriadType.MINOR)
        assertEquals("DO Minore", slot.displayTitle(NoteNotation.ITALIAN, UiLanguage.ITALIAN))
        assertEquals("C Minor", slot.displayTitle(NoteNotation.ENGLISH, UiLanguage.ENGLISH))
    }
}
