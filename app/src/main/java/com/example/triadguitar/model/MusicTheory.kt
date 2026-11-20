package com.example.triadguitar.model

import kotlin.math.pow

enum class NoteNotation {
    ITALIAN,   // DO, RE, MI, FA, SOL, LA, SI
    ENGLISH    // C, D, E, F, G, A, B
}

enum class BadgeDisplayMode {
    BOTH,           // Mostra Nota e Grado (es: "DO" + "T")
    NOTE_ONLY,      // Mostra solo la Nota (es: "DO")
    DEGREE_ONLY     // Mostra solo il Grado (es: "T", "3ª", "5ª")
}

/** Controls what label is shown on each fretboard dot — mirrors Guitar Visualizer's display toggle. */
enum class DotLabelMode {
    NOTES,      // Note name on every dot (C, D#, SOL …)
    INTERVALS,  // Interval degree on every dot (1, ♭3, 5 …)
    CLEAN       // No label — dots only (colour speaks for itself)
}

enum class FretboardDisplayScope {
    SELECTED_VOICING,  // Mostra la forma concentrata sulla corda selezionata
    FULL_NECK          // Mostra tutte le note della triade su tutto il manico
}

enum class FretboardOrientation {
    HORIZONTAL,  // Vista Orizzontale (Landscape)
    VERTICAL     // Vista Verticale (Portrait)
}

data class Pitch(
    val semitone: Int // 0..11 (0 = C / DO)
) {
    init {
        require(semitone in 0..11) { "Semitone must be in 0..11, got $semitone" }
    }

    fun getDisplayName(notation: NoteNotation, preferFlat: Boolean = false): String {
        return when (notation) {
            NoteNotation.ENGLISH -> if (preferFlat) ENGLISH_FLAT_NAMES[semitone] else ENGLISH_SHARP_NAMES[semitone]
            NoteNotation.ITALIAN -> if (preferFlat) ITALIAN_FLAT_NAMES[semitone] else ITALIAN_SHARP_NAMES[semitone]
        }
    }

    companion object {
        val ALL = (0..11).map { Pitch(it) }

        val ENGLISH_SHARP_NAMES = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val ENGLISH_FLAT_NAMES = listOf("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B")

        val ITALIAN_SHARP_NAMES = listOf("DO", "DO#", "RE", "RE#", "MI", "FA", "FA#", "SOL", "SOL#", "LA", "LA#", "SI")
        val ITALIAN_FLAT_NAMES = listOf("DO", "REb", "RE", "MIb", "MI", "FA", "SOLb", "SOL", "LAb", "LA", "SIb", "SI")

        fun fromSemitone(semitone: Int): Pitch = Pitch((semitone % 12 + 12) % 12)
    }
}

enum class ScaleType(
    val title: String,
    val shortTitle: String,         // compact label for chips
    val englishTitle: String,       // English name
    val intervals: List<Int>,       // semitones from root
    val degreeLabels: List<String>,
    val category: String = "Major & Minor"
) {
    MAJOR(
        title = "Maggiore (Ionian)", shortTitle = "Major", englishTitle = "Major (Ionian)",
        intervals = listOf(0, 2, 4, 5, 7, 9, 11), degreeLabels = listOf("1", "2", "3", "4", "5", "6", "7")
    ),
    MINOR_NATURAL(
        title = "Minore Naturale (Aeolian)", shortTitle = "Minor", englishTitle = "Natural Minor (Aeolian)",
        intervals = listOf(0, 2, 3, 5, 7, 8, 10), degreeLabels = listOf("1", "2", "♭3", "4", "5", "♭6", "♭7")
    ),
    MAJOR_PENTATONIC(
        title = "Pentatonica Maggiore", shortTitle = "Penta Maj", englishTitle = "Major Pentatonic",
        intervals = listOf(0, 2, 4, 7, 9), degreeLabels = listOf("1", "2", "3", "5", "6"),
        category = "Pentatonic & Blues"
    ),
    MINOR_PENTATONIC(
        title = "Pentatonica Minore", shortTitle = "Penta Min", englishTitle = "Minor Pentatonic",
        intervals = listOf(0, 3, 5, 7, 10), degreeLabels = listOf("1", "♭3", "4", "5", "♭7"),
        category = "Pentatonic & Blues"
    ),
    BLUES(
        title = "Scala Blues", shortTitle = "Blues", englishTitle = "Blues Scale",
        intervals = listOf(0, 3, 5, 6, 7, 10), degreeLabels = listOf("1", "♭3", "4", "♭5", "5", "♭7"),
        category = "Pentatonic & Blues"
    ),
    HARMONIC_MINOR(
        title = "Minore Armonica", shortTitle = "Harm Min", englishTitle = "Harmonic Minor",
        intervals = listOf(0, 2, 3, 5, 7, 8, 11), degreeLabels = listOf("1", "2", "♭3", "4", "5", "♭6", "7"),
        category = "Major & Minor"
    ),
    MELODIC_MINOR(
        title = "Minore Melodica", shortTitle = "Mel Min", englishTitle = "Melodic Minor",
        intervals = listOf(0, 2, 3, 5, 7, 9, 11), degreeLabels = listOf("1", "2", "♭3", "4", "5", "6", "7"),
        category = "Major & Minor"
    ),

    // Modes
    DORIAN(
        title = "Modo Dorico", shortTitle = "Dorian", englishTitle = "Dorian",
        intervals = listOf(0, 2, 3, 5, 7, 9, 10), degreeLabels = listOf("1", "2", "♭3", "4", "5", "6", "♭7"),
        category = "Modes"
    ),
    PHRYGIAN(
        title = "Modo Frigio", shortTitle = "Phrygian", englishTitle = "Phrygian",
        intervals = listOf(0, 1, 3, 5, 7, 8, 10), degreeLabels = listOf("1", "♭2", "♭3", "4", "5", "♭6", "♭7"),
        category = "Modes"
    ),
    LYDIAN(
        title = "Modo Lidio", shortTitle = "Lydian", englishTitle = "Lydian",
        intervals = listOf(0, 2, 4, 6, 7, 9, 11), degreeLabels = listOf("1", "2", "3", "♯4", "5", "6", "7"),
        category = "Modes"
    ),
    MIXOLYDIAN(
        title = "Modo Misolidio", shortTitle = "Mixolydian", englishTitle = "Mixolydian",
        intervals = listOf(0, 2, 4, 5, 7, 9, 10), degreeLabels = listOf("1", "2", "3", "4", "5", "6", "♭7"),
        category = "Modes"
    ),
    LOCRIAN(
        title = "Modo Locrio", shortTitle = "Locrian", englishTitle = "Locrian",
        intervals = listOf(0, 1, 3, 5, 6, 8, 10), degreeLabels = listOf("1", "♭2", "♭3", "4", "♭5", "♭6", "♭7"),
        category = "Modes"
    ),
    WHOLE_TONE(
        title = "Scala a Toni Interi", shortTitle = "Whole Tone", englishTitle = "Whole Tone",
        intervals = listOf(0, 2, 4, 6, 8, 10), degreeLabels = listOf("1", "2", "3", "♯4", "♯5", "♭7"),
        category = "Other Scales"
    ),
    DIMINISHED_HALF_WHOLE(
        title = "Diminuita Semitono-Tono", shortTitle = "Half-Whole Diminished", englishTitle = "Half-Whole Diminished",
        intervals = listOf(0, 1, 3, 4, 6, 7, 9, 10), degreeLabels = listOf("1", "♭2", "♭3", "3", "♭5", "5", "6", "♭7"),
        category = "Other Scales"
    ),
    DOUBLE_HARMONIC_MAJOR(
        title = "Maggiore Doppia Armonica", shortTitle = "Double Harmonic", englishTitle = "Double Harmonic Major",
        intervals = listOf(0, 1, 4, 5, 7, 8, 11), degreeLabels = listOf("1", "♭2", "3", "4", "5", "♭6", "7"),
        category = "Other Scales"
    );

    fun getDegreeLabelForInterval(semitoneOffset: Int): String? {
        val idx = intervals.indexOf((semitoneOffset % 12 + 12) % 12)
        return if (idx >= 0) degreeLabels.getOrNull(idx) else null
    }
}

enum class GuitarTuning(
    val title: String,
    val openMidiNotes: List<Int> // 1 to 6 (High E to Low E)
) {
    STANDARD("Standard (EADGBE)", listOf(64, 59, 55, 50, 45, 40)),
    DROP_D("Drop D (DADGBE)", listOf(64, 59, 55, 50, 45, 38)),
    DROP_C("Drop C (CGCFAD)", listOf(62, 57, 53, 48, 43, 36)),
    HALF_STEP_DOWN("Half Step Down (E♭A♭D♭G♭B♭E♭)", listOf(63, 58, 54, 49, 44, 39)),
    WHOLE_STEP_DOWN("Whole Step Down (DGCFAD)", listOf(62, 57, 53, 48, 43, 38)),
    DROP_B("Drop B (BF♯BEG♯C♯)", listOf(61, 56, 52, 47, 42, 35)),
    DADGAD("DADGAD", listOf(62, 57, 55, 50, 45, 38)),
    OPEN_G("Open G (DGDGBD)", listOf(62, 59, 55, 50, 43, 38)),
    OPEN_D("Open D (DADF♯AD)", listOf(62, 57, 54, 50, 45, 38)),
    OPEN_E("Open E (EBEG♯BE)", listOf(64, 59, 56, 52, 47, 40)),
    OPEN_A("Open A (EAEAC♯E)", listOf(64, 61, 57, 52, 45, 40)),
    OPEN_C("Open C (CGCGCE)", listOf(64, 60, 55, 48, 43, 36)),
    OPEN_D_MINOR("Open D Minor (DADFAD)", listOf(62, 57, 53, 50, 45, 38)),
    NASHVILLE("Nashville High-Strung", listOf(76, 71, 67, 62, 57, 52));

    fun getStringInfoList(): List<GuitarStringInfo> {
        return openMidiNotes.mapIndexed { idx, midi ->
            val stringNum = idx + 1
            val semi = (midi % 12 + 12) % 12
            GuitarStringInfo(
                stringNumber = stringNum,
                openPitch = Pitch(semi),
                openMidiNote = midi,
                nameItalian = "${stringNum}ª - ${Pitch(semi).getDisplayName(NoteNotation.ITALIAN)}",
                nameEnglish = "${stringNum} - ${Pitch(semi).getDisplayName(NoteNotation.ENGLISH)}"
            )
        }
    }
}

enum class TriadRole(
    val shortLabel: String,
    val italianLabel: String,
    val englishLabel: String,
    val colorHex: Long
) {
    ROOT(
        shortLabel = "1",
        italianLabel = "Tonica",
        englishLabel = "Root",
        colorHex = 0xFFFF3B30 // Vivid Coral Red
    ),
    THIRD(
        shortLabel = "3",
        italianLabel = "Terza",
        englishLabel = "3rd",
        colorHex = 0xFF2ECC71 // Emerald Green
    ),
    FIFTH(
        shortLabel = "5",
        italianLabel = "Quinta",
        englishLabel = "5th",
        colorHex = 0xFF007AFF // Vibrant Royal/Azure Blue
    );

    fun getDegreeLabel(triadType: TriadType): String {
        return when (this) {
            ROOT -> "1"
            THIRD -> if (triadType.isMinorThird) "♭3" else "3"
            FIFTH -> when {
                triadType.isDiminishedFifth -> "♭5"
                triadType.isAugmentedFifth -> "♯5"
                else -> "5"
            }
        }
    }
}

enum class TriadType(
    val title: String,
    val englishTitle: String,
    val symbol: String,
    val intervals: List<Int>, // semitones from root
    val description: String,
    val isMinorThird: Boolean = false,
    val isDiminishedFifth: Boolean = false,
    val isAugmentedFifth: Boolean = false
) {
    MAJOR(
        title = "Maggiore",
        englishTitle = "Major",
        symbol = "☀️",
        intervals = listOf(0, 4, 7),
        description = "Tonica + 3ª Maggiore (4 st) + 5ª Giusta (7 st) • Suono brillante e aperto"
    ),
    MINOR(
        title = "Minore",
        englishTitle = "Minor",
        symbol = "🌙",
        intervals = listOf(0, 3, 7),
        description = "Tonica + 3ª Minore (3 st) + 5ª Giusta (7 st) • Suono caldo e malinconico",
        isMinorThird = true
    ),
    DIMINISHED(
        title = "Diminuita",
        englishTitle = "Diminished",
        symbol = "⚡",
        intervals = listOf(0, 3, 6),
        description = "Tonica + 3ª Minore (3 st) + 5ª Diminuita (6 st) • Tensione e mistero",
        isMinorThird = true,
        isDiminishedFifth = true
    ),
    AUGMENTED(
        title = "Aumentata",
        englishTitle = "Augmented",
        symbol = "✨",
        intervals = listOf(0, 4, 8),
        description = "Tonica + 3ª Maggiore (4 st) + 5ª Aumentata (8 st) • Suono sospeso ed etereo",
        isAugmentedFifth = true
    ),
    SUS4(
        title = "Sus4",
        englishTitle = "Sus4",
        symbol = "🌊",
        intervals = listOf(0, 5, 7),
        description = "Tonica + 4ª Giusta (5 st) + 5ª Giusta (7 st) • Sospensione aperta"
    ),
    SUS2(
        title = "Sus2",
        englishTitle = "Sus2",
        symbol = "🍃",
        intervals = listOf(0, 2, 7),
        description = "Tonica + 2ª Maggiore (2 st) + 5ª Giusta (7 st) • Spazioso e arioso"
    );

    fun getRoleForInterval(semitoneOffset: Int): TriadRole? {
        val normalized = (semitoneOffset % 12 + 12) % 12
        return when (normalized) {
            intervals[0] -> TriadRole.ROOT
            intervals[1] -> TriadRole.THIRD
            intervals[2] -> TriadRole.FIFTH
            else -> null
        }
    }
}

enum class TriadInversion(
    val title: String,
    val formula: String
) {
    ALL("Tutte", "Tutti i rivolti"),
    ROOT_POSITION("Fondamentale", "1 - 3 - 5"),
    FIRST_INVERSION("1° Rivolto", "3 - 5 - 1"),
    SECOND_INVERSION("2° Rivolto", "5 - 1 - 3")
}

data class GuitarStringInfo(
    val stringNumber: Int, // 1 to 6 (1 = High E, 6 = Low E)
    val openPitch: Pitch,
    val openMidiNote: Int,
    val nameItalian: String,
    val nameEnglish: String
)

object GuitarConstants {
    val STRINGS = GuitarTuning.STANDARD.getStringInfoList()

    const val TOTAL_FRETS = 22

    val SINGLE_DOT_FRETS = setOf(3, 5, 7, 9, 15, 17, 19, 21)
    val DOUBLE_DOT_FRETS = setOf(12)

    fun getMidiNote(stringNumber: Int, fret: Int, tuning: GuitarTuning = GuitarTuning.STANDARD): Int {
        val stringInfo = tuning.getStringInfoList().firstOrNull { it.stringNumber == stringNumber } ?: STRINGS.last()
        return stringInfo.openMidiNote + fret
    }

    fun midiToFrequency(midiNote: Int): Float {
        return (440.0 * 2.0.pow((midiNote - 69).toDouble() / 12.0)).toFloat()
    }
}

data class FretNote(
    val stringNumber: Int, // 1..6
    val fret: Int,          // 0..22
    val pitch: Pitch,
    val midiNote: Int,
    val triadRole: TriadRole?,
    val isHighlightedRoot: Boolean = false,
    val isInSelectedVoicing: Boolean = false,
    val isInScale: Boolean = false,
    val scaleDegreeLabel: String? = null,
    val isTargetNote: Boolean = false
) {
    val frequency: Float get() = GuitarConstants.midiToFrequency(midiNote)
}

data class TriadVoicing(
    val inversion: TriadInversion,
    val baseStringNumber: Int,
    val notes: List<FretNote>, // One note per string in the 3-string set
    val minFret: Int,
    val maxFret: Int
)
