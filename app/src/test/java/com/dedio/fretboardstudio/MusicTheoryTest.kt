package com.dedio.fretboardstudio

import com.dedio.fretboardstudio.model.BadgeDisplayMode
import com.dedio.fretboardstudio.model.GuitarConstants
import com.dedio.fretboardstudio.model.GuitarTuning
import com.dedio.fretboardstudio.model.NoteNotation
import com.dedio.fretboardstudio.model.Pitch
import com.dedio.fretboardstudio.model.ScaleType
import com.dedio.fretboardstudio.model.TriadCalculator
import com.dedio.fretboardstudio.model.TriadInversion
import com.dedio.fretboardstudio.model.TriadRole
import com.dedio.fretboardstudio.model.TriadType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicTheoryTest {

    @Test
    fun testNoteNotationItalianAndEnglish() {
        val cPitch = Pitch(0)
        assertEquals("DO", cPitch.getDisplayName(NoteNotation.ITALIAN))
        assertEquals("C", cPitch.getDisplayName(NoteNotation.ENGLISH))

        val fSharpPitch = Pitch(6)
        assertEquals("FA#", fSharpPitch.getDisplayName(NoteNotation.ITALIAN, preferFlat = false))
        assertEquals("F#", fSharpPitch.getDisplayName(NoteNotation.ENGLISH, preferFlat = false))
        assertEquals("SOLb", fSharpPitch.getDisplayName(NoteNotation.ITALIAN, preferFlat = true))
        assertEquals("Gb", fSharpPitch.getDisplayName(NoteNotation.ENGLISH, preferFlat = true))
    }

    @Test
    fun testCMajorTriadNotes() {
        val root = Pitch(0) // C / DO
        val triadType = TriadType.MAJOR

        val allNotes = TriadCalculator.calculateAllFretNotes(
            rootPitch = root,
            triadType = triadType,
            selectedBaseString = 5
        )

        // C on 5th string (A string) must be at fret 3
        val cOnString5 = allNotes.firstOrNull { it.stringNumber == 5 && it.fret == 3 }
        assertNotNull(cOnString5)
        assertEquals(TriadRole.ROOT, cOnString5?.triadRole)
        assertTrue(cOnString5?.isHighlightedRoot == true)

        // E (Third) on 4th string (D string) must be at fret 2
        val eOnString4 = allNotes.firstOrNull { it.stringNumber == 4 && it.fret == 2 }
        assertNotNull(eOnString4)
        assertEquals(TriadRole.THIRD, eOnString4?.triadRole)

        // G (Fifth) on 3rd string (G string) must be at fret 0 (open string)
        val gOnString3 = allNotes.firstOrNull { it.stringNumber == 3 && it.fret == 0 }
        assertNotNull(gOnString3)
        assertEquals(TriadRole.FIFTH, gOnString3?.triadRole)
    }

    @Test
    fun testTriadVoicingsOnStrings123() {
        val root = Pitch(0) // C Major
        val triadType = TriadType.MAJOR
        val stringSet = listOf(3, 2, 1) // G, B, High E

        val voicings = TriadCalculator.findVoicings(
            rootPitch = root,
            triadType = triadType,
            stringSet = stringSet
        )

        // There should be multiple playable voicings across frets 0..15 (Root pos, 1st inv, 2nd inv)
        assertTrue(voicings.isNotEmpty())

        val rootPos = voicings.firstOrNull { it.inversion == TriadInversion.ROOT_POSITION }
        val firstInv = voicings.firstOrNull { it.inversion == TriadInversion.FIRST_INVERSION }
        val secondInv = voicings.firstOrNull { it.inversion == TriadInversion.SECOND_INVERSION }

        assertNotNull("Root position voicing should exist", rootPos)
        assertNotNull("1st inversion voicing should exist", firstInv)
        assertNotNull("2nd inversion voicing should exist", secondInv)
    }

    @Test
    fun scaleAndModeIntervalsMatchTheirFormulas() {
        val expectedIntervals = mapOf(
            ScaleType.MAJOR to listOf(0, 2, 4, 5, 7, 9, 11),
            ScaleType.MINOR_NATURAL to listOf(0, 2, 3, 5, 7, 8, 10),
            ScaleType.MAJOR_PENTATONIC to listOf(0, 2, 4, 7, 9),
            ScaleType.MINOR_PENTATONIC to listOf(0, 3, 5, 7, 10),
            ScaleType.BLUES to listOf(0, 3, 5, 6, 7, 10),
            ScaleType.HARMONIC_MINOR to listOf(0, 2, 3, 5, 7, 8, 11),
            ScaleType.MELODIC_MINOR to listOf(0, 2, 3, 5, 7, 9, 11),
            ScaleType.DORIAN to listOf(0, 2, 3, 5, 7, 9, 10),
            ScaleType.PHRYGIAN to listOf(0, 1, 3, 5, 7, 8, 10),
            ScaleType.LYDIAN to listOf(0, 2, 4, 6, 7, 9, 11),
            ScaleType.MIXOLYDIAN to listOf(0, 2, 4, 5, 7, 9, 10),
            ScaleType.LOCRIAN to listOf(0, 1, 3, 5, 6, 8, 10),
            ScaleType.WHOLE_TONE to listOf(0, 2, 4, 6, 8, 10),
            ScaleType.DIMINISHED_HALF_WHOLE to listOf(0, 1, 3, 4, 6, 7, 9, 10),
            ScaleType.DOUBLE_HARMONIC_MAJOR to listOf(0, 1, 4, 5, 7, 8, 11)
        )
        val expectedDegrees = mapOf(
            ScaleType.MAJOR to listOf("1", "2", "3", "4", "5", "6", "7"),
            ScaleType.MINOR_NATURAL to listOf("1", "2", "♭3", "4", "5", "♭6", "♭7"),
            ScaleType.MAJOR_PENTATONIC to listOf("1", "2", "3", "5", "6"),
            ScaleType.MINOR_PENTATONIC to listOf("1", "♭3", "4", "5", "♭7"),
            ScaleType.BLUES to listOf("1", "♭3", "4", "♭5", "5", "♭7"),
            ScaleType.HARMONIC_MINOR to listOf("1", "2", "♭3", "4", "5", "♭6", "7"),
            ScaleType.MELODIC_MINOR to listOf("1", "2", "♭3", "4", "5", "6", "7"),
            ScaleType.DORIAN to listOf("1", "2", "♭3", "4", "5", "6", "♭7"),
            ScaleType.PHRYGIAN to listOf("1", "♭2", "♭3", "4", "5", "♭6", "♭7"),
            ScaleType.LYDIAN to listOf("1", "2", "3", "♯4", "5", "6", "7"),
            ScaleType.MIXOLYDIAN to listOf("1", "2", "3", "4", "5", "6", "♭7"),
            ScaleType.LOCRIAN to listOf("1", "♭2", "♭3", "4", "♭5", "♭6", "♭7"),
            ScaleType.WHOLE_TONE to listOf("1", "2", "3", "♯4", "♯5", "♭7"),
            ScaleType.DIMINISHED_HALF_WHOLE to listOf("1", "♭2", "♭3", "3", "♭5", "5", "6", "♭7"),
            ScaleType.DOUBLE_HARMONIC_MAJOR to listOf("1", "♭2", "3", "4", "5", "♭6", "7")
        )

        assertEquals(ScaleType.entries.size, expectedIntervals.size)
        assertEquals(ScaleType.entries.size, expectedDegrees.size)
        expectedIntervals.forEach { (scale, intervals) ->
            assertEquals("Wrong intervals for ${scale.name}", intervals, scale.intervals)
            assertEquals("Wrong degree labels for ${scale.name}", expectedDegrees.getValue(scale), scale.degreeLabels)
            assertEquals("Wrong degree count for ${scale.name}", intervals.size, scale.degreeLabels.size)
            intervals.forEachIndexed { index, interval ->
                assertEquals(
                    "Wrong degree for ${scale.name} at semitone $interval",
                    scale.degreeLabels[index],
                    scale.getDegreeLabelForInterval(interval)
                )
            }
        }
    }

    @Test
    fun calculatedFretNotesMatchSelectedScale() {
        ScaleType.entries.forEach { scale ->
            val notes = TriadCalculator.calculateAllFretNotes(
                rootPitch = Pitch(0),
                triadType = TriadType.MAJOR,
                scaleType = scale
            )

            notes.forEach { note ->
                val interval = (note.pitch.semitone - Pitch(0).semitone + 12) % 12
                assertEquals(
                    "Incorrect scale membership for ${scale.name} at fret ${note.fret}",
                    interval in scale.intervals,
                    note.isInScale
                )
                assertEquals(
                    "Incorrect scale degree for ${scale.name} at fret ${note.fret}",
                    scale.getDegreeLabelForInterval(interval),
                    note.scaleDegreeLabel
                )
            }
        }
    }

    @Test
    fun selectedPentatonicBoxesAreAdditiveAndRepeatAcrossTheNeck() {
        val root = Pitch(7) // G minor pentatonic; low E root at fret 3.
        val notes = TriadCalculator.calculateAllFretNotes(
            rootPitch = root,
            triadType = TriadType.MINOR,
            scaleType = ScaleType.MINOR_PENTATONIC
        )
        val firstBoxRepeated = notes.first { it.stringNumber == 6 && it.fret == 15 }
        val thirdBoxNote = notes.first { it.stringNumber == 6 && it.fret == 10 }

        fun isInBox(note: com.dedio.fretboardstudio.model.FretNote, position: Int) =
            TriadCalculator.isNoteInScalePosition(
                note = note,
                rootPitch = root,
                scaleType = ScaleType.MINOR_PENTATONIC,
                tuning = GuitarTuning.STANDARD,
                baseString = 6,
                position = position
            )

        assertTrue(isInBox(firstBoxRepeated, position = 1))
        assertTrue(isInBox(thirdBoxNote, position = 3))
        assertTrue(
            TriadCalculator.isNoteInAnyScalePosition(
                note = firstBoxRepeated,
                rootPitch = root,
                scaleType = ScaleType.MINOR_PENTATONIC,
                tuning = GuitarTuning.STANDARD,
                baseString = 6,
                positions = setOf(1, 3)
            )
        )
        assertTrue(
            TriadCalculator.isNoteInAnyScalePosition(
                note = thirdBoxNote,
                rootPitch = root,
                scaleType = ScaleType.MINOR_PENTATONIC,
                tuning = GuitarTuning.STANDARD,
                baseString = 6,
                positions = setOf(1, 3)
            )
        )
    }

    @Test
    fun testFormulaExplanation() {
        val formula = TriadCalculator.buildFormulaExplanation(
            rootPitch = Pitch(0),
            triadType = TriadType.MAJOR,
            notation = NoteNotation.ITALIAN
        )

        assertEquals("DO", formula.rootName)
        assertEquals("MI", formula.thirdName)
        assertEquals("SOL", formula.fifthName)
        assertTrue(formula.summary.contains("DO Maggiore"))
    }
}
