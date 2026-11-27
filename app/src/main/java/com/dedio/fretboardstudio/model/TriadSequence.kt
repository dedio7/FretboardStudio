package com.dedio.fretboardstudio.model

import androidx.compose.ui.graphics.Color

/**
 * Palette dedicated exclusively to triad-sequence slots.
 *
 * These hues deliberately avoid the tonic-defining [TriadRole] colors (root red,
 * third green, fifth blue) and the gold used for chord targets, so a slot marker
 * can never be mistaken for a note role on the fretboard.
 */
private val TRIAD_SLOT_COLOR_HEXES = longArrayOf(
    0xFF00D7E8, // slot 1: app default/base cyan
    0xFFB967FF, // slot 2: violet
    0xFFFF4FA1, // slot 3: magenta
    0xFFFFA62B, // slot 4: amber
    0xFFB8FF2C, // slot 5: lime
)

fun getSlotColorHex(index: Int): Long {
    val size = TRIAD_SLOT_COLOR_HEXES.size
    return TRIAD_SLOT_COLOR_HEXES[((index % size) + size) % size]
}

data class ChordSlot(
    val id: String = java.util.UUID.randomUUID().toString(),
    val root: Pitch = Pitch(0), // C / DO
    val quality: TriadType = TriadType.MAJOR,
    val inversion: TriadInversion = TriadInversion.ALL,
    val baseString: Int = 5,
    val isVisible: Boolean = true,
    val slotIndex: Int = 0
) {
    val colorHex: Long get() = getSlotColorHex(slotIndex)
    val color: Color get() = Color(colorHex)

    /** Single source of truth for the chord wording used in headers, cards and badges. */
    fun displayTitle(notation: NoteNotation, language: UiLanguage): String {
        val qualityName = if (language == UiLanguage.ITALIAN) quality.title else quality.englishTitle
        return "${root.getDisplayName(notation)} $qualityName"
    }
}

data class ChordOccupant(
    val slotId: String,
    val slotIndex: Int,
    val colorHex: Long,
    val root: Pitch,
    val quality: TriadType,
    val role: TriadRole,
    val degreeLabel: String,
    val chordTitle: String,
    /** True for the chord the user currently has selected in the sequence panel. */
    val isSelected: Boolean = false
) {
    val color: Color get() = Color(colorHex)
}

data class ChordPreset(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val slots: List<ChordSlot>
)

object TriadSequenceAggregator {

    /**
     * Builds the fretboard notes for the active slots.
     *
     * @param isolatedSlotId when set, only this chord is returned so a single voicing can
     *   be isolated while studying voice leading.
     */
    fun aggregate(
        slots: List<ChordSlot>,
        tuning: GuitarTuning = GuitarTuning.STANDARD,
        maxFret: Int = GuitarConstants.TOTAL_FRETS,
        notation: NoteNotation = NoteNotation.ENGLISH,
        language: UiLanguage = UiLanguage.ITALIAN,
        isolatedSlotId: String? = null,
        selectedSlotId: String? = null
    ): List<FretNote> {
        val positionMap = mutableMapOf<Pair<Int, Int>, MutableList<ChordOccupant>>()

        slots.filter { it.isVisible && (isolatedSlotId == null || it.id == isolatedSlotId) }
            .forEach { slot ->
                val stringSet = TriadCalculator.getStringSetForBaseString(slot.baseString)
                val voicings = TriadCalculator.findVoicings(
                    rootPitch = slot.root,
                    triadType = slot.quality,
                    stringSet = stringSet,
                    tuning = tuning,
                    maxFret = maxFret
                )

                // Plotting every voicing of every inversion floods the neck with five active
                // chords. "Tutti" therefore shows one representative (lowest, tightest)
                // voicing per inversion, while a specific inversion keeps all of its
                // positions so shapes can still be compared along the neck.
                val selectedVoicings = when (slot.inversion) {
                    TriadInversion.ALL -> TriadInversion.entries
                        .filter { it != TriadInversion.ALL }
                        .mapNotNull { inversion -> voicings.filter { it.inversion == inversion }.bestVoicing() }
                    else -> voicings.filter { it.inversion == slot.inversion }
                }

                selectedVoicings.forEach { voicing ->
                    voicing.notes.forEach { note ->
                        val key = note.stringNumber to note.fret
                        val role = note.triadRole ?: TriadRole.ROOT
                        val degree = role.getDegreeLabel(slot.quality)

                        val occupant = ChordOccupant(
                            slotId = slot.id,
                            slotIndex = slot.slotIndex,
                            colorHex = slot.colorHex,
                            root = slot.root,
                            quality = slot.quality,
                            role = role,
                            degreeLabel = degree,
                            chordTitle = slot.displayTitle(notation, language),
                            isSelected = slot.id == selectedSlotId
                        )

                        if (positionMap.getOrPut(key) { mutableListOf() }.none { it.slotId == slot.id }) {
                            positionMap[key]!!.add(occupant)
                        }
                    }
                }
            }

        val result = mutableListOf<FretNote>()
        positionMap.forEach { (key, occupants) ->
            val (stringNum, fret) = key
            val stringInfo = tuning.getStringInfoList().first { it.stringNumber == stringNum }
            val currentSemitone = (stringInfo.openPitch.semitone + fret) % 12
            val currentPitch = Pitch(currentSemitone)
            val midi = stringInfo.openMidiNote + fret

            result.add(
                FretNote(
                    stringNumber = stringNum,
                    fret = fret,
                    pitch = currentPitch,
                    midiNote = midi,
                    triadRole = occupants.firstOrNull()?.role,
                    isInScale = true,
                    scaleDegreeLabel = occupants.firstOrNull()?.degreeLabel,
                    sharedOccupants = occupants
                )
            )
        }
        return result
    }

    private fun List<TriadVoicing>.bestVoicing(): TriadVoicing? =
        minWithOrNull(compareBy<TriadVoicing> { it.minFret }.thenBy { it.maxFret - it.minFret })
}
