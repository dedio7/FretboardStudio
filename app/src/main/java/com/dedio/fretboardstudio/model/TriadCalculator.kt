package com.dedio.fretboardstudio.model

object TriadCalculator {

    /**
     * Returns an approximate five-box position window anchored to the selected root.
     * Position 0 means the full neck; positions 1–5 overlap by two frets.
     */
    fun getPositionFretRange(
        rootPitch: Pitch,
        tuning: GuitarTuning,
        baseString: Int,
        position: Int,
        maxFret: Int = GuitarConstants.TOTAL_FRETS
    ): IntRange? {
        if (position == 0) return null
        require(position in 1..5)
        val openPitch = tuning.getStringInfoList().first { it.stringNumber == baseString }.openPitch.semitone
        val rootFret = (rootPitch.semitone - openPitch + 12) % 12
        val positionOffsets = listOf(0, 2, 4, 5, 7)
        var start = rootFret + positionOffsets[position - 1] - 1
        while (start > maxFret) start -= 12
        start = start.coerceAtLeast(0)
        return start..(start + 4).coerceAtMost(maxFret)
    }

    /**
     * Tests whether a fret belongs to any selected box/position. An empty selection means
     * the full neck. Each position repeats every octave across the playable fret range.
     */
    fun isNoteInAnyScalePosition(
        note: FretNote,
        rootPitch: Pitch,
        scaleType: ScaleType,
        tuning: GuitarTuning,
        baseString: Int,
        positions: Set<Int>,
        maxFret: Int = GuitarConstants.TOTAL_FRETS
    ): Boolean = positions.isEmpty() || positions.any { position ->
        isNoteInScalePosition(note, rootPitch, scaleType, tuning, baseString, position, maxFret)
    }

    fun isNoteInScalePosition(
        note: FretNote,
        rootPitch: Pitch,
        scaleType: ScaleType,
        tuning: GuitarTuning,
        baseString: Int,
        position: Int,
        maxFret: Int = GuitarConstants.TOTAL_FRETS
    ): Boolean {
        if (position == 0) return true
        require(position in 1..5)

        val pentatonicTypes = setOf(
            ScaleType.MAJOR_PENTATONIC,
            ScaleType.MINOR_PENTATONIC,
            ScaleType.BLUES
        )
        val isStandardTuning = tuning.openMidiNotes.map { it % 12 } ==
            GuitarTuning.STANDARD.openMidiNotes.map { it % 12 }

        val baseRange = if (scaleType in pentatonicTypes && isStandardTuning) {
            val boxRoot = if (scaleType == ScaleType.MAJOR_PENTATONIC) {
                Pitch.fromSemitone(rootPitch.semitone - 3)
            } else {
                rootPitch
            }
            val lowEOpenPitch = tuning.getStringInfoList().first { it.stringNumber == 6 }.openPitch.semitone
            val lowEAnchor = (boxRoot.semitone - lowEOpenPitch + 12) % 12
            val boxOffsetsByStringFromLowE = listOf(
                listOf(0..3, 0..2, 0..2, 0..2, 0..3, 0..3),
                listOf(3..5, 2..5, 2..5, 2..4, 3..5, 3..5),
                listOf(5..8, 5..7, 5..7, 4..7, 5..8, 5..8),
                listOf(7..10, 7..10, 7..10, 7..9, 8..10, 7..10),
                listOf(-2..0, -2..0, -3..0, -3..0, -2..0, -2..0)
            )
            val stringIndex = 6 - note.stringNumber
            val offsets = boxOffsetsByStringFromLowE[position - 1][stringIndex]
            (lowEAnchor + offsets.first)..(lowEAnchor + offsets.last)
        } else {
            getPositionFretRange(rootPitch, tuning, baseString, position, maxFret) ?: return true
        }

        return (-2..2).any { octave ->
            val fretShift = octave * 12
            note.fret in (baseRange.first + fretShift)..(baseRange.last + fretShift)
        }
    }

    /**
     * Generates all fret notes across the entire fretboard (strings 1..6, frets 0..maxFret),
     * calculating both the background scale notes and highlighted triad notes.
     */
    fun calculateAllFretNotes(
        rootPitch: Pitch,
        triadType: TriadType? = null,
        scaleType: ScaleType? = ScaleType.MAJOR,
        tuning: GuitarTuning = GuitarTuning.STANDARD,
        selectedBaseString: Int = 5,
        maxFret: Int = GuitarConstants.TOTAL_FRETS
    ): List<FretNote> {
        val rootSemitone = rootPitch.semitone
        val stringList = tuning.getStringInfoList()
        val result = mutableListOf<FretNote>()

        for (stringInfo in stringList) {
            val stringNum = stringInfo.stringNumber
            val openPitch = stringInfo.openPitch.semitone

            for (fret in 0..maxFret) {
                val currentSemitone = (openPitch + fret) % 12
                val currentPitch = Pitch(currentSemitone)
                val midi = stringInfo.openMidiNote + fret
                val offsetFromRoot = (currentSemitone - rootSemitone + 12) % 12
                val role = triadType?.getRoleForInterval(offsetFromRoot)
                val isInScale = scaleType?.intervals?.contains(offsetFromRoot) ?: true
                val scaleDegree = scaleType?.getDegreeLabelForInterval(offsetFromRoot)
                val isHighlightedRoot = (stringNum == selectedBaseString && role == TriadRole.ROOT)

                result.add(
                    FretNote(
                        stringNumber = stringNum,
                        fret = fret,
                        pitch = currentPitch,
                        midiNote = midi,
                        triadRole = role,
                        isHighlightedRoot = isHighlightedRoot,
                        isInScale = isInScale,
                        scaleDegreeLabel = scaleDegree
                    )
                )
            }
        }
        return result
    }

    /**
     * Gets the 3-string set associated with a given base string (the lowest sounding string of the set).
     */
    fun getStringSetForBaseString(baseString: Int): List<Int> {
        return when (baseString) {
            6 -> listOf(6, 5, 4)
            5 -> listOf(5, 4, 3)
            4 -> listOf(4, 3, 2)
            3 -> listOf(3, 2, 1)
            2 -> listOf(3, 2, 1)
            1 -> listOf(3, 2, 1)
            else -> listOf(3, 2, 1)
        }
    }

    /**
     * Finds playable, ergonomic 3-note close voicings on the specified 3-string set.
     */
    fun findVoicings(
        rootPitch: Pitch,
        triadType: TriadType,
        stringSet: List<Int>, // ordered low string to high string, e.g. [6, 5, 4]
        tuning: GuitarTuning = GuitarTuning.STANDARD,
        maxFret: Int = GuitarConstants.TOTAL_FRETS
    ): List<TriadVoicing> {
        require(stringSet.size == 3) { "String set must contain exactly 3 strings" }
        val sLow = stringSet[0]
        val sMid = stringSet[1]
        val sHigh = stringSet[2]

        val rootSemi = rootPitch.semitone
        val thirdSemi = (rootSemi + triadType.intervals[1]) % 12
        val fifthSemi = (rootSemi + triadType.intervals[2]) % 12

        val voicings = mutableListOf<TriadVoicing>()

        val inversionConfigs = listOf(
            Triple(TriadInversion.ROOT_POSITION, listOf(rootSemi, thirdSemi, fifthSemi), listOf(TriadRole.ROOT, TriadRole.THIRD, TriadRole.FIFTH)),
            Triple(TriadInversion.FIRST_INVERSION, listOf(thirdSemi, fifthSemi, rootSemi), listOf(TriadRole.THIRD, TriadRole.FIFTH, TriadRole.ROOT)),
            Triple(TriadInversion.SECOND_INVERSION, listOf(fifthSemi, rootSemi, thirdSemi), listOf(TriadRole.FIFTH, TriadRole.ROOT, TriadRole.THIRD))
        )

        for ((inv, targetSemis, roles) in inversionConfigs) {
            val fretsLow = getFretsForPitch(sLow, targetSemis[0], tuning, maxFret)
            val fretsMid = getFretsForPitch(sMid, targetSemis[1], tuning, maxFret)
            val fretsHigh = getFretsForPitch(sHigh, targetSemis[2], tuning, maxFret)

            for (fL in fretsLow) {
                for (fM in fretsMid) {
                    for (fH in fretsHigh) {
                        val frets = listOf(fL, fM, fH)
                        val minF = frets.minOrNull() ?: 0
                        val maxF = frets.maxOrNull() ?: 0

                        val nonZeroFrets = frets.filter { it > 0 }
                        val span = if (nonZeroFrets.isNotEmpty()) nonZeroFrets.max() - nonZeroFrets.min() else 0

                        if (span <= 3 && (maxF - minF <= 4 || minF == 0 && maxF <= 4)) {
                            val notes = listOf(
                                createFretNote(sLow, fL, roles[0], tuning),
                                createFretNote(sMid, fM, roles[1], tuning),
                                createFretNote(sHigh, fH, roles[2], tuning)
                            )
                            voicings.add(
                                TriadVoicing(
                                    inversion = inv,
                                    baseStringNumber = sLow,
                                    notes = notes,
                                    minFret = minF,
                                    maxFret = maxF
                                )
                            )
                        }
                    }
                }
            }
        }

        return voicings.sortedBy { it.minFret }
    }

    /**
     * Finds ALL playable 3-note close triad voicings across ALL 4 string sets
     * ([6, 5, 4], [5, 4, 3], [4, 3, 2], [3, 2, 1]) on the entire fretboard.
     */
    fun findAllVoicingsOnAllStrings(
        rootPitch: Pitch,
        triadType: TriadType,
        tuning: GuitarTuning = GuitarTuning.STANDARD,
        maxFret: Int = GuitarConstants.TOTAL_FRETS
    ): List<TriadVoicing> {
        val allSets = listOf(
            listOf(6, 5, 4),
            listOf(5, 4, 3),
            listOf(4, 3, 2),
            listOf(3, 2, 1)
        )
        return allSets.flatMap { set ->
            findVoicings(rootPitch, triadType, set, tuning, maxFret)
        }.sortedWith(compareBy({ it.minFret }, { -it.baseStringNumber }))
    }

    private fun getFretsForPitch(stringNum: Int, targetSemi: Int, tuning: GuitarTuning, maxFret: Int): List<Int> {
        val stringInfo = tuning.getStringInfoList().first { it.stringNumber == stringNum }
        val openSemi = stringInfo.openPitch.semitone
        val baseFret = (targetSemi - openSemi + 12) % 12

        val result = mutableListOf<Int>()
        var fret = baseFret
        while (fret <= maxFret) {
            result.add(fret)
            fret += 12
        }
        return result
    }

    private fun createFretNote(stringNum: Int, fret: Int, role: TriadRole, tuning: GuitarTuning): FretNote {
        val stringInfo = tuning.getStringInfoList().first { it.stringNumber == stringNum }
        val semi = (stringInfo.openPitch.semitone + fret) % 12
        val midi = stringInfo.openMidiNote + fret
        return FretNote(
            stringNumber = stringNum,
            fret = fret,
            pitch = Pitch(semi),
            midiNote = midi,
            triadRole = role,
            isInSelectedVoicing = true
        )
    }

    /**
     * Builds an educational explanation of the triad formula.
     */
    fun buildFormulaExplanation(
        rootPitch: Pitch,
        triadType: TriadType,
        notation: NoteNotation
    ): TriadFormulaDetails {
        val rootName = rootPitch.getDisplayName(notation)
        val thirdPitch = Pitch.fromSemitone(rootPitch.semitone + triadType.intervals[1])
        val fifthPitch = Pitch.fromSemitone(rootPitch.semitone + triadType.intervals[2])

        val thirdName = thirdPitch.getDisplayName(notation)
        val fifthName = fifthPitch.getDisplayName(notation)

        val thirdIntervalSt = triadType.intervals[1]
        val fifthIntervalSt = triadType.intervals[2] - triadType.intervals[1]

        val summary = "$rootName ${triadType.title} ($rootName - $thirdName - $fifthName)"

        val interval1Text = "$rootName + $thirdIntervalSt semitoni = $thirdName (${if (triadType.isMinorThird) "3ª Minore" else "3ª Maggiore"})"
        val interval2Text = "$thirdName + $fifthIntervalSt semitoni = $fifthName (${when {
            triadType.isDiminishedFifth -> "5ª Diminuita"
            triadType.isAugmentedFifth -> "5ª Aumentata"
            else -> "5ª Giusta"
        }})"

        return TriadFormulaDetails(
            rootName = rootName,
            thirdName = thirdName,
            fifthName = fifthName,
            triadTitle = "$rootName ${triadType.title}",
            summary = summary,
            firstIntervalDescription = interval1Text,
            secondIntervalDescription = interval2Text,
            overallDescription = triadType.description
        )
    }
}

data class TriadFormulaDetails(
    val rootName: String,
    val thirdName: String,
    val fifthName: String,
    val triadTitle: String,
    val summary: String,
    val firstIntervalDescription: String,
    val secondIntervalDescription: String,
    val overallDescription: String
)
