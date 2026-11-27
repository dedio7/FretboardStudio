package com.dedio.fretboardstudio.model

enum class ChordCategory(val title: String) {
    TRIADS("Triadi Base"),
    SEVENTHS("Accordi di 7ª"),
    EXTENDED("Estesi & Sesti"),
    POWERCHORDS("Powerchords")
}

data class ChordTypeInfo(
    val id: String,
    val name: String,
    val symbol: String,
    val category: ChordCategory,
    val intervals: List<Int>,
    val description: String
)

data class ChordDiagram(
    val title: String,
    val baseFret: Int,
    // 6 integers representing frets for strings 6 to 1 (Low E to High E):
    // -1 = Muted (X), 0 = Open (O), >0 = Fret relative or absolute
    val stringFrets: List<Int>,
    // 6 integers for finger numbers (1=Index, 2=Middle, 3=Ring, 4=Pinky, 0=None)
    val fingerHints: List<Int>,
    val barreFret: Int? = null,
    val cagedShape: CagedShape? = null,
    val positionLabel: String = ""
)

object ChordDictionaryRepository {

    val ALL_TYPES = listOf(
        // Triads
        ChordTypeInfo("maj", "Maggiore", "", ChordCategory.TRIADS, listOf(0, 4, 7), "Tonica + 3M + 5G"),
        ChordTypeInfo("min", "Minore", "m", ChordCategory.TRIADS, listOf(0, 3, 7), "Tonica + 3m + 5G"),
        ChordTypeInfo("dim", "Diminuita", "dim", ChordCategory.TRIADS, listOf(0, 3, 6), "Tonica + 3m + 5♭"),
        ChordTypeInfo("aug", "Aumentata", "aug", ChordCategory.TRIADS, listOf(0, 4, 8), "Tonica + 3M + 5♯"),
        ChordTypeInfo("sus2", "Sus2", "sus2", ChordCategory.TRIADS, listOf(0, 2, 7), "Tonica + 2M + 5G"),
        ChordTypeInfo("sus4", "Sus4", "sus4", ChordCategory.TRIADS, listOf(0, 5, 7), "Tonica + 4G + 5G"),

        // Sevenths
        ChordTypeInfo("dom7", "Dominante 7", "7", ChordCategory.SEVENTHS, listOf(0, 4, 7, 10), "Tonica + 3M + 5G + 7m"),
        ChordTypeInfo("maj7", "Maggiore 7", "Maj7", ChordCategory.SEVENTHS, listOf(0, 4, 7, 11), "Tonica + 3M + 5G + 7M"),
        ChordTypeInfo("m7", "Minore 7", "m7", ChordCategory.SEVENTHS, listOf(0, 3, 7, 10), "Tonica + 3m + 5G + 7m"),
        ChordTypeInfo("m7b5", "Semidiminuito", "m7♭5", ChordCategory.SEVENTHS, listOf(0, 3, 6, 10), "Tonica + 3m + 5♭ + 7m"),
        ChordTypeInfo("dim7", "Diminuito 7", "dim7", ChordCategory.SEVENTHS, listOf(0, 3, 6, 9), "Tonica + 3m + 5♭ + 7dim"),
        ChordTypeInfo("mMaj7", "Minore-Maggiore 7", "mMaj7", ChordCategory.SEVENTHS, listOf(0, 3, 7, 11), "Tonica + 3m + 5G + 7M"),

        // Extended & Sixths
        ChordTypeInfo("add9", "Add9", "add9", ChordCategory.EXTENDED, listOf(0, 4, 7, 14), "Tonica + 3M + 5G + 9M"),
        ChordTypeInfo("6", "Sesta", "6", ChordCategory.EXTENDED, listOf(0, 4, 7, 9), "Tonica + 3M + 5G + 6M"),
        ChordTypeInfo("m6", "Minore 6", "m6", ChordCategory.EXTENDED, listOf(0, 3, 7, 9), "Tonica + 3m + 5G + 6M"),
        ChordTypeInfo("9", "Nona Dominante", "9", ChordCategory.EXTENDED, listOf(0, 4, 7, 10, 14), "Tonica + 3M + 5G + 7m + 9M"),
        ChordTypeInfo("maj9", "Maggiore 9", "Maj9", ChordCategory.EXTENDED, listOf(0, 4, 7, 11, 14), "Tonica + 3M + 5G + 7M + 9M"),
        ChordTypeInfo("m9", "Minore 9", "m9", ChordCategory.EXTENDED, listOf(0, 3, 7, 10, 14), "Tonica + 3m + 5G + 7m + 9M"),

        // Powerchords
        ChordTypeInfo("power", "Powerchord", "5", ChordCategory.POWERCHORDS, listOf(0, 7), "Tonica + 5ª Giusta")
    )

    fun getDiagramsForChord(root: Pitch, typeInfo: ChordTypeInfo, notation: NoteNotation, tuning: GuitarTuning = GuitarTuning.STANDARD): List<ChordDiagram> {
        if (typeInfo.id == "power") {
            return getPowerchordDiagrams(root, tuning, notation)
        }

        val rootName = root.getDisplayName(notation)
        val fullTitle = "$rootName ${typeInfo.symbol}".trim()
        val shift = root.semitone
        val chordId = typeInfo.id
        val isMinor = typeInfo.intervals.getOrNull(1) == 3

        val diagrams = mutableListOf<ChordDiagram>()

        // Check if there is an open position for C, A, E, G, D shapes:
        if (shift == 0) { // C (DO)
            val openFrets = when (chordId) {
                "min" -> listOf(-1, 3, 5, 5, 4, 3)
                "dim" -> listOf(-1, 3, 4, 5, 4, -1)
                "aug" -> listOf(-1, 3, 2, 1, 1, 0)
                "sus2" -> listOf(-1, 3, 0, 0, 1, 3)
                "sus4" -> listOf(-1, 3, 3, 0, 1, 1)
                else -> listOf(-1, 3, 2, 0, 1, 0)
            }
            val posLabel = if (chordId == "min") "Posizione Aperta (Cm-Shape)" else if (chordId != "maj") "Posizione Aperta ($chordId-Shape)" else "Posizione Aperta (C-Shape)"
            diagrams.add(
                ChordDiagram(
                    title = fullTitle,
                    baseFret = 1,
                    stringFrets = openFrets,
                    fingerHints = listOf(0, 3, 2, 0, 1, 0),
                    cagedShape = CagedShape.C_SHAPE,
                    positionLabel = posLabel
                )
            )
        } else if (shift == 9) { // A (LA)
            val openFrets = when (chordId) {
                "min" -> listOf(-1, 0, 2, 2, 1, 0)
                "dim" -> listOf(-1, 0, 1, 2, 1, 0)
                "aug" -> listOf(-1, 0, 3, 2, 2, 1)
                "sus2" -> listOf(-1, 0, 2, 2, 0, 0)
                "sus4" -> listOf(-1, 0, 2, 2, 3, 0)
                else -> listOf(-1, 0, 2, 2, 2, 0)
            }
            val posLabel = if (chordId == "min") "Posizione Aperta (Am-Shape)" else if (chordId != "maj") "Posizione Aperta ($chordId-Shape)" else "Posizione Aperta (A-Shape)"
            diagrams.add(
                ChordDiagram(
                    title = fullTitle,
                    baseFret = 1,
                    stringFrets = openFrets,
                    fingerHints = listOf(0, 0, 1, 2, 3, 0),
                    cagedShape = CagedShape.A_SHAPE,
                    positionLabel = posLabel
                )
            )
        } else if (shift == 4) { // E (MI)
            val openFrets = when (chordId) {
                "min" -> listOf(0, 2, 2, 0, 0, 0)
                "dim" -> listOf(0, 1, 2, 3, 2, 0)
                "aug" -> listOf(0, 3, 2, 1, 1, 0)
                "sus2" -> listOf(0, 2, 4, 4, 0, 0)
                "sus4" -> listOf(0, 2, 2, 2, 0, 0)
                else -> listOf(0, 2, 2, 1, 0, 0)
            }
            val posLabel = if (chordId == "min") "Posizione Aperta (Em-Shape)" else if (chordId != "maj") "Posizione Aperta ($chordId-Shape)" else "Posizione Aperta (E-Shape)"
            diagrams.add(
                ChordDiagram(
                    title = fullTitle,
                    baseFret = 1,
                    stringFrets = openFrets,
                    fingerHints = listOf(0, 2, 3, 1, 0, 0),
                    cagedShape = CagedShape.E_SHAPE,
                    positionLabel = posLabel
                )
            )
        } else if (shift == 7) { // G (SOL)
            val openFrets = when (chordId) {
                "min" -> listOf(3, 1, 0, 0, 3, 3)
                "dim" -> listOf(3, 4, 5, 6, 5, 3)
                "aug" -> listOf(3, 2, 1, 0, 0, 3)
                "sus2" -> listOf(3, 0, 0, 0, 3, 3)
                "sus4" -> listOf(3, 3, 0, 0, 1, 3)
                else -> listOf(3, 2, 0, 0, 0, 3)
            }
            val posLabel = if (chordId == "min") "Posizione Aperta (Gm-Shape)" else if (chordId != "maj") "Posizione Aperta ($chordId-Shape)" else "Posizione Aperta (G-Shape)"
            diagrams.add(
                ChordDiagram(
                    title = fullTitle,
                    baseFret = 1,
                    stringFrets = openFrets,
                    fingerHints = listOf(2, 1, 0, 0, 0, 3),
                    cagedShape = CagedShape.G_SHAPE,
                    positionLabel = posLabel
                )
            )
        } else if (shift == 2) { // D (RE)
            val openFrets = when (chordId) {
                "min" -> listOf(-1, -1, 0, 2, 3, 1)
                "dim" -> listOf(-1, -1, 0, 1, 3, 1)
                "aug" -> listOf(-1, -1, 0, 3, 3, 2)
                "sus2" -> listOf(-1, -1, 0, 2, 3, 0)
                "sus4" -> listOf(-1, -1, 0, 2, 3, 3)
                else -> listOf(-1, -1, 0, 2, 3, 2)
            }
            val posLabel = if (chordId == "min") "Posizione Aperta (Dm-Shape)" else if (chordId != "maj") "Posizione Aperta ($chordId-Shape)" else "Posizione Aperta (D-Shape)"
            diagrams.add(
                ChordDiagram(
                    title = fullTitle,
                    baseFret = 1,
                    stringFrets = openFrets,
                    fingerHints = listOf(0, 0, 0, 1, 3, 2),
                    cagedShape = CagedShape.D_SHAPE,
                    positionLabel = posLabel
                )
            )
        }

        // CAGED Barre Shapes for all keys:

        // 1. A-Shape Barre (Root on 5th string A)
        val aFret = (shift - 9 + 12) % 12
        if (aFret in 1..12) {
            val aFrets = when (chordId) {
                "min" -> listOf(-1, aFret, aFret + 2, aFret + 2, aFret + 1, aFret)
                "dim" -> listOf(-1, aFret, aFret + 1, aFret + 2, aFret + 1, aFret)
                "aug" -> listOf(-1, aFret, aFret + 3, aFret + 2, aFret + 1, aFret)
                "sus2" -> listOf(-1, aFret, aFret + 2, aFret + 2, aFret, aFret)
                "sus4" -> listOf(-1, aFret, aFret + 2, aFret + 2, aFret + 3, aFret)
                else -> listOf(-1, aFret, aFret + 2, aFret + 2, aFret + 2, aFret)
            }
            diagrams.add(
                ChordDiagram(
                    title = fullTitle,
                    baseFret = aFret,
                    stringFrets = aFrets,
                    fingerHints = listOf(0, 1, 2, 3, if (isMinor) 2 else 4, 1),
                    barreFret = aFret,
                    cagedShape = CagedShape.A_SHAPE,
                    positionLabel = "Barrè Tasto $aFret (Forma A)"
                )
            )
        }

        // 2. E-Shape Barre (Root on 6th string E)
        val eFret = (shift - 4 + 12) % 12
        if (eFret in 1..12) {
            val eFrets = when (chordId) {
                "min" -> listOf(eFret, eFret + 2, eFret + 2, eFret, eFret, eFret)
                "dim" -> listOf(eFret, eFret + 2, eFret + 2, eFret, eFret - 1, eFret)
                "aug" -> listOf(eFret, eFret + 3, eFret + 2, eFret + 1, eFret + 1, eFret)
                "sus2" -> listOf(eFret, eFret + 2, eFret + 2, eFret + 2, eFret, eFret)
                "sus4" -> listOf(eFret, eFret + 2, eFret + 2, eFret + 2, eFret, eFret)
                else -> listOf(eFret, eFret + 2, eFret + 2, eFret + 1, eFret, eFret)
            }
            diagrams.add(
                ChordDiagram(
                    title = fullTitle,
                    baseFret = eFret,
                    stringFrets = eFrets,
                    fingerHints = listOf(1, 3, 4, if (isMinor) 1 else 2, 1, 1),
                    barreFret = eFret,
                    cagedShape = CagedShape.E_SHAPE,
                    positionLabel = "Barrè Tasto $eFret (Forma E)"
                )
            )
        }

        // 3. C-Shape Barre (Root on 5th string)
        val cFret = (shift - 0 + 12) % 12
        if (cFret in 1..12 && chordId == "maj") {
            diagrams.add(
                ChordDiagram(
                    title = fullTitle,
                    baseFret = cFret,
                    stringFrets = listOf(-1, cFret + 3, cFret + 2, cFret, cFret + 1, cFret),
                    fingerHints = listOf(0, 4, 3, 1, 2, 1),
                    barreFret = cFret,
                    cagedShape = CagedShape.C_SHAPE,
                    positionLabel = "Barrè Tasto $cFret (Forma C)"
                )
            )
        }

        // 4. D-Shape Barre (Root on 4th string)
        val dFret = (shift - 2 + 12) % 12
        if (dFret in 1..12) {
            val dFrets = when (chordId) {
                "min" -> listOf(-1, -1, dFret, dFret + 2, dFret + 3, dFret + 1)
                "dim" -> listOf(-1, -1, dFret, dFret + 1, dFret + 3, dFret + 1)
                "aug" -> listOf(-1, -1, dFret, dFret + 3, dFret + 3, dFret + 2)
                "sus2" -> listOf(-1, -1, dFret, dFret + 2, dFret + 3, dFret)
                "sus4" -> listOf(-1, -1, dFret, dFret + 2, dFret + 3, dFret + 3)
                else -> listOf(-1, -1, dFret, dFret + 2, dFret + 3, dFret + 2)
            }
            diagrams.add(
                ChordDiagram(
                    title = fullTitle,
                    baseFret = dFret,
                    stringFrets = dFrets,
                    fingerHints = listOf(0, 0, 1, 2, 4, if (isMinor) 1 else 3),
                    cagedShape = CagedShape.D_SHAPE,
                    positionLabel = "Posizione Tasto $dFret (Forma D)"
                )
            )
        }

        return diagrams.distinctBy { it.positionLabel }
    }

    private fun getPowerchordDiagrams(root: Pitch, tuning: GuitarTuning, notation: NoteNotation): List<ChordDiagram> {
        val rootName = root.getDisplayName(notation)
        val fullTitle = "$rootName 5"
        val rootSemi = root.semitone
        val fifthSemi = (rootSemi + 7) % 12

        val stringInfoList = tuning.getStringInfoList()
        val diagrams = mutableListOf<ChordDiagram>()

        val stringPairs = listOf(
            Pair(6, 5),
            Pair(5, 4),
            Pair(4, 3),
            Pair(3, 2),
            Pair(2, 1)
        )

        for ((lowStrNum, highStrNum) in stringPairs) {
            val lowString = stringInfoList.firstOrNull { it.stringNumber == lowStrNum } ?: continue
            val highString = stringInfoList.firstOrNull { it.stringNumber == highStrNum } ?: continue

            for (fret in 0..15) {
                val lowPitch = (lowString.openPitch.semitone + fret) % 12
                if (lowPitch == rootSemi) {
                    for (highFret in (fret - 1).coerceAtLeast(0)..(fret + 2).coerceAtMost(22)) {
                        val highPitch = (highString.openPitch.semitone + highFret) % 12
                        if (highPitch == fifthSemi) {
                            val frets = mutableListOf(-1, -1, -1, -1, -1, -1)
                            val lowArrIdx = 6 - lowString.stringNumber
                            val highArrIdx = 6 - highString.stringNumber
                            frets[lowArrIdx] = fret
                            frets[highArrIdx] = highFret

                            if (lowStrNum > 2) {
                                val octaveString = stringInfoList.firstOrNull { it.stringNumber == lowStrNum - 1 }
                                if (octaveString != null) {
                                    val octArrIdx = 6 - octaveString.stringNumber
                                    val octFret = highFret + 2
                                    if (octFret <= 22) {
                                        val octPitch = (octaveString.openPitch.semitone + octFret) % 12
                                        if (octPitch == rootSemi) {
                                            frets[octArrIdx] = octFret
                                        }
                                    }
                                }
                            }

                            val baseFret = frets.filter { it >= 0 }.minOrNull() ?: 1
                            val label = "Powerchord (Corde ${lowString.stringNumber}-${highString.stringNumber}, Tasto $baseFret)"

                            diagrams.add(
                                ChordDiagram(
                                    title = fullTitle,
                                    baseFret = baseFret.coerceAtLeast(1),
                                    stringFrets = frets,
                                    fingerHints = frets.map { if (it > 0) 1 else 0 },
                                    positionLabel = label
                                )
                            )
                        }
                    }
                }
            }
        }

        return diagrams.distinctBy { it.stringFrets }
            .sortedWith(compareBy({
                val playedStrings = it.stringFrets.mapIndexed { idx, f -> if (f >= 0) 6 - idx else -1 }.filter { s -> s > 0 }
                -(playedStrings.maxOrNull() ?: 0)
            }, { it.baseFret }))
    }
}
