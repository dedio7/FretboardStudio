package com.dedio.fretboardstudio.model

enum class ChordCategory(val title: String) {
    TRIADS("Triadi Base"),
    SEVENTHS("Accordi di 7ª"),
    EXTENDED("Estesi & Sesti")
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
        ChordTypeInfo("m9", "Minore 9", "m9", ChordCategory.EXTENDED, listOf(0, 3, 7, 10, 14), "Tonica + 3m + 5G + 7m + 9M")
    )

    fun getDiagramsForChord(root: Pitch, typeInfo: ChordTypeInfo, notation: NoteNotation): List<ChordDiagram> {
        val rootName = root.getDisplayName(notation)
        val fullTitle = "$rootName ${typeInfo.symbol}".trim()
        val shift = root.semitone
        val isMinor = typeInfo.intervals.getOrNull(1) == 3

        val diagrams = mutableListOf<ChordDiagram>()

        // Check if there is an open position for C, A, E, G, D shapes:
        if (shift == 0) { // C (DO)
            if (!isMinor) {
                // Open C Major: [-1, 3, 2, 0, 1, 0]
                diagrams.add(
                    ChordDiagram(
                        title = fullTitle,
                        baseFret = 1,
                        stringFrets = listOf(-1, 3, 2, 0, 1, 0),
                        fingerHints = listOf(0, 3, 2, 0, 1, 0),
                        cagedShape = CagedShape.C_SHAPE,
                        positionLabel = "Posizione Aperta (C-Shape)"
                    )
                )
            }
        } else if (shift == 9) { // A (LA)
            if (!isMinor) {
                diagrams.add(
                    ChordDiagram(
                        title = fullTitle,
                        baseFret = 1,
                        stringFrets = listOf(-1, 0, 2, 2, 2, 0),
                        fingerHints = listOf(0, 0, 1, 2, 3, 0),
                        cagedShape = CagedShape.A_SHAPE,
                        positionLabel = "Posizione Aperta (A-Shape)"
                    )
                )
            } else {
                diagrams.add(
                    ChordDiagram(
                        title = fullTitle,
                        baseFret = 1,
                        stringFrets = listOf(-1, 0, 2, 2, 1, 0),
                        fingerHints = listOf(0, 0, 2, 3, 1, 0),
                        cagedShape = CagedShape.A_SHAPE,
                        positionLabel = "Posizione Aperta (Am-Shape)"
                    )
                )
            }
        } else if (shift == 4) { // E (MI)
            if (!isMinor) {
                diagrams.add(
                    ChordDiagram(
                        title = fullTitle,
                        baseFret = 1,
                        stringFrets = listOf(0, 2, 2, 1, 0, 0),
                        fingerHints = listOf(0, 2, 3, 1, 0, 0),
                        cagedShape = CagedShape.E_SHAPE,
                        positionLabel = "Posizione Aperta (E-Shape)"
                    )
                )
            } else {
                diagrams.add(
                    ChordDiagram(
                        title = fullTitle,
                        baseFret = 1,
                        stringFrets = listOf(0, 2, 2, 0, 0, 0),
                        fingerHints = listOf(0, 2, 3, 0, 0, 0),
                        cagedShape = CagedShape.E_SHAPE,
                        positionLabel = "Posizione Aperta (Em-Shape)"
                    )
                )
            }
        } else if (shift == 7) { // G (SOL)
            if (!isMinor) {
                diagrams.add(
                    ChordDiagram(
                        title = fullTitle,
                        baseFret = 1,
                        stringFrets = listOf(3, 2, 0, 0, 0, 3),
                        fingerHints = listOf(2, 1, 0, 0, 0, 3),
                        cagedShape = CagedShape.G_SHAPE,
                        positionLabel = "Posizione Aperta (G-Shape)"
                    )
                )
            }
        } else if (shift == 2) { // D (RE)
            if (!isMinor) {
                diagrams.add(
                    ChordDiagram(
                        title = fullTitle,
                        baseFret = 1,
                        stringFrets = listOf(-1, -1, 0, 2, 3, 2),
                        fingerHints = listOf(0, 0, 0, 1, 3, 2),
                        cagedShape = CagedShape.D_SHAPE,
                        positionLabel = "Posizione Aperta (D-Shape)"
                    )
                )
            } else {
                diagrams.add(
                    ChordDiagram(
                        title = fullTitle,
                        baseFret = 1,
                        stringFrets = listOf(-1, -1, 0, 2, 3, 1),
                        fingerHints = listOf(0, 0, 0, 2, 3, 1),
                        cagedShape = CagedShape.D_SHAPE,
                        positionLabel = "Posizione Aperta (Dm-Shape)"
                    )
                )
            }
        }

        // CAGED Barre Shapes for all keys:

        // 1. A-Shape Barre (Root on 5th string A)
        val aFret = (shift - 9 + 12) % 12
        if (aFret in 1..12) {
            val aFrets = if (isMinor) {
                listOf(-1, aFret, aFret + 2, aFret + 2, aFret + 1, aFret)
            } else {
                listOf(-1, aFret, aFret + 2, aFret + 2, aFret + 2, aFret)
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
            val eFrets = if (isMinor) {
                listOf(eFret, eFret + 2, eFret + 2, eFret, eFret, eFret)
            } else {
                listOf(eFret, eFret + 2, eFret + 2, eFret + 1, eFret, eFret)
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
        if (cFret in 1..12 && !isMinor) {
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
            val dFrets = if (isMinor) {
                listOf(-1, -1, dFret, dFret + 2, dFret + 3, dFret + 1)
            } else {
                listOf(-1, -1, dFret, dFret + 2, dFret + 3, dFret + 2)
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
}
