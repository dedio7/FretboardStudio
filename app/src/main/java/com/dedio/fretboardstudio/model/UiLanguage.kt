package com.dedio.fretboardstudio.model

enum class UiLanguage(val displayName: String) {
    ITALIAN("Italiano"),
    ENGLISH("English")
}

class UiStrings(private val language: UiLanguage) {
    val settings get() = if (language == UiLanguage.ITALIAN) "Impostazioni" else "Settings"
    val guitar get() = if (language == UiLanguage.ITALIAN) "CHITARRA" else "GUITAR"
    val boxes get() = if (language == UiLanguage.ITALIAN) "BOX PENTATONICI" else "PENTATONIC BOXES"
    val scalePosition get() = if (language == UiLanguage.ITALIAN) "POSIZIONE SCALA" else "SCALE POSITION"
    val all get() = if (language == UiLanguage.ITALIAN) "Tutte" else "All"
    val display get() = if (language == UiLanguage.ITALIAN) "VISUALIZZAZIONE" else "DISPLAY"
    val clean get() = if (language == UiLanguage.ITALIAN) "Pulito" else "Clean"
    val notes get() = if (language == UiLanguage.ITALIAN) "Note" else "Notes"
    val intervals get() = if (language == UiLanguage.ITALIAN) "Intervalli" else "Intervals"
    val fretboardNotes get() = if (language == UiLanguage.ITALIAN) "NOTE DEL MANICO" else "FRETBOARD NOTES"
    val showTuningNotes get() = if (language == UiLanguage.ITALIAN) "Mostra tutte le note" else "Show all notes"
    val targetNotes get() = if (language == UiLanguage.ITALIAN) "NOTE TARGET" else "TARGET NOTES"
    val rootNote get() = if (language == UiLanguage.ITALIAN) "TONICA" else "ROOT NOTE"
    val visualTheme get() = if (language == UiLanguage.ITALIAN) "TEMA VISIVO" else "VISUAL THEME"
    val appLanguage get() = if (language == UiLanguage.ITALIAN) "LINGUA DELL’APP" else "APP LANGUAGE"
    val noteNotation get() = if (language == UiLanguage.ITALIAN) "NOMENCLATURA DELLE NOTE" else "NOTE NAMING"
    val italianNotation get() = if (language == UiLanguage.ITALIAN) "Italiana / Latina (DO, RE, MI...)" else "Italian / Latin (DO, RE, MI...)"
    val englishNotation get() = if (language == UiLanguage.ITALIAN) "Anglosassone (C, D, E...)" else "English (C, D, E...)"
    val close get() = if (language == UiLanguage.ITALIAN) "Chiudi" else "Close"
    val tuning get() = if (language == UiLanguage.ITALIAN) "ACCORDATURA" else "TUNING"
    val sequence get() = if (language == UiLanguage.ITALIAN) "SEQUENZA" else "SEQUENCE"
    val category get() = if (language == UiLanguage.ITALIAN) "CATEGORIA" else "CATEGORY"
    val chordType get() = if (language == UiLanguage.ITALIAN) "TIPO" else "TYPE"
    val inversion get() = if (language == UiLanguage.ITALIAN) "RIVOLTO" else "INVERSION"
    val fretboardStrings get() = if (language == UiLanguage.ITALIAN) "CORDE" else "STRINGS"
    val positions get() = if (language == UiLanguage.ITALIAN) "POSIZIONI" else "POSITIONS"
    val triadsTab get() = if (language == UiLanguage.ITALIAN) "TRIADI" else "TRIADS"
    val dictionaryTab get() = if (language == UiLanguage.ITALIAN) "ACCORDI" else "CHORDS"
    val allChords get() = if (language == UiLanguage.ITALIAN) "Tutti" else "All"
    val soloChord = "Solo"
    val highlightTonic get() = if (language == UiLanguage.ITALIAN) "TONICA" else "TONIC"
    val moveUp get() = if (language == UiLanguage.ITALIAN) "SU" else "UP"
    val moveDown get() = if (language == UiLanguage.ITALIAN) "GIÙ" else "DOWN"
    val addChord get() = if (language == UiLanguage.ITALIAN) "Aggiungi accordo" else "Add chord"
    val removeChord get() = if (language == UiLanguage.ITALIAN) "Rimuovi accordo" else "Remove chord"
    val expandChord get() = if (language == UiLanguage.ITALIAN) "Apri dettagli accordo" else "Expand chord details"
    val collapseChord get() = if (language == UiLanguage.ITALIAN) "Chiudi dettagli accordo" else "Collapse chord details"
    val hideChord get() = if (language == UiLanguage.ITALIAN) "Nascondi accordo" else "Hide chord"
    val showChord get() = if (language == UiLanguage.ITALIAN) "Mostra accordo" else "Show chord"
    val closeChordPanel get() = if (language == UiLanguage.ITALIAN) "Chiudi pannello accordi" else "Close chord panel"
    val openChordPanel get() = if (language == UiLanguage.ITALIAN) "Apri pannello accordi" else "Open chord panel"
    val closeScalesPanel get() = if (language == UiLanguage.ITALIAN) "Chiudi pannello scale" else "Close scales panel"
    val openScalesPanel get() = if (language == UiLanguage.ITALIAN) "Apri pannello scale" else "Open scales panel"
    val showTuningNotesDescription get() =
        if (language == UiLanguage.ITALIAN) "Mostra tutte le note dell’accordatura" else "Show all notes of the tuning"
    val metronome get() = if (language == UiLanguage.ITALIAN) "METRONOMO" else "METRONOME"
    val playMetronome get() = if (language == UiLanguage.ITALIAN) "Avvia metronomo" else "Start metronome"
    val stopMetronome get() = if (language == UiLanguage.ITALIAN) "Ferma metronomo" else "Stop metronome"
    val tapTempo = "TAP TEMPO"
    val tapTempoDescription get() =
        if (language == UiLanguage.ITALIAN) "Batta almeno due volte a tempo per calcolare il BPM" else "Tap at least twice in time to calculate the BPM"
    val accent get() = if (language == UiLanguage.ITALIAN) "ACCENTO" else "ACCENT"
    val accentOff = "OFF"
    val accentOffDescription get() =
        if (language == UiLanguage.ITALIAN) "Disattiva accento" else "Disable accent"
    fun accentBeatDescription(beat: Int) =
        if (language == UiLanguage.ITALIAN) "Accento sul battito $beat" else "Accent on beat $beat"
    val expandMetronome get() = if (language == UiLanguage.ITALIAN) "Apri controlli metronomo" else "Expand metronome controls"
    val collapseMetronome get() = if (language == UiLanguage.ITALIAN) "Chiudi controlli metronomo" else "Collapse metronome controls"
    val decreaseBpm get() = if (language == UiLanguage.ITALIAN) "Diminuisci BPM" else "Decrease BPM"
    val increaseBpm get() = if (language == UiLanguage.ITALIAN) "Aumenta BPM" else "Increase BPM"

    fun scaleTitle(scale: ScaleType) =
        if (language == UiLanguage.ITALIAN) scale.title else scale.englishTitle

    fun scaleCategory(category: String) =
        if (language == UiLanguage.ITALIAN) {
            when (category) {
                "Major & Minor" -> "Maggiori e minori"
                "Pentatonic & Blues" -> "Pentatoniche e blues"
                "Modes" -> "Modi"
                "Other Scales" -> "Altre scale"
                else -> category
            }
        } else category

    fun triadTypeName(type: TriadType) = if (language == UiLanguage.ITALIAN) type.title else type.englishTitle

    fun triadInversionName(inversion: TriadInversion) =
        if (language == UiLanguage.ITALIAN) inversion.title else inversion.englishTitle

    fun chordCount(count: Int) =
        if (language == UiLanguage.ITALIAN) " · $count accordi" else " · $count chords"

    fun chordCategory(category: ChordCategory) = when (category) {
        ChordCategory.TRIADS -> if (language == UiLanguage.ITALIAN) "Triadi" else "Triads"
        ChordCategory.SEVENTHS -> if (language == UiLanguage.ITALIAN) "7ª" else "7ths"
        ChordCategory.EXTENDED -> if (language == UiLanguage.ITALIAN) "Estesi" else "Extended"
    }

    /** Compact, language-neutral chord symbol; the verbose name remains in the content description. */
    fun chordSymbol(type: ChordTypeInfo) = type.symbol.ifEmpty { type.id }

    fun chordTypeName(type: ChordTypeInfo) = if (language == UiLanguage.ITALIAN) type.name else chordSymbol(type)

    fun chordFormula(type: ChordTypeInfo): String {
        if (language == UiLanguage.ITALIAN) return type.description
        return type.description
            .replace("Tonica", "Root")
            .replace("3M", "3")
            .replace("3m", "♭3")
            .replace("5G", "5")
            .replace("5♭", "♭5")
            .replace("5♯", "♯5")
            .replace("7M", "7")
            .replace("7m", "♭7")
            .replace("7dim", "♭♭7")
            .replace("6M", "6")
            .replace("9M", "9")
            .replace("2M", "2")
            .replace("4G", "4")
    }

    fun themeDescription(theme: VisualTheme) =
        if (language == UiLanguage.ITALIAN) theme.description else when (theme) {
            VisualTheme.DEFAULT -> "A dark studio with cyan, warm gold, and magenta"
            VisualTheme.NEON -> "Cyberpunk contrast with violet, lime, and electric pink"
            VisualTheme.PLAYFUL -> "Cream, berry, mango, and mint with a cherrywood fretboard"
            VisualTheme.VIBRANT -> "Coral, turquoise, and yellow over midnight blue"
        }
}
