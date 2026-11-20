package com.example.triadguitar.model

enum class UiLanguage(val displayName: String) {
    ITALIAN("Italiano"),
    ENGLISH("English")
}

class UiStrings(private val language: UiLanguage) {
    val settingsTitle get() = if (language == UiLanguage.ITALIAN) "Impostazioni" else "Settings"
    val settingsButton get() = if (language == UiLanguage.ITALIAN) "Impostazioni" else "Settings"
    val guitar get() = if (language == UiLanguage.ITALIAN) "CHITARRA" else "GUITAR"
    val boxes get() = if (language == UiLanguage.ITALIAN) "BOX PENTATONICI" else "PENTATONIC BOXES"
    val scalePosition get() = if (language == UiLanguage.ITALIAN) "POSIZIONE SCALA" else "SCALE POSITION"
    val all get() = if (language == UiLanguage.ITALIAN) "Tutte" else "All"
    val display get() = if (language == UiLanguage.ITALIAN) "VISUALIZZAZIONE" else "DISPLAY"
    val clean get() = if (language == UiLanguage.ITALIAN) "Pulito" else "Clean"
    val notes get() = if (language == UiLanguage.ITALIAN) "Note" else "Notes"
    val intervals get() = if (language == UiLanguage.ITALIAN) "Intervalli" else "Intervals"
    val fretboardNotes get() = if (language == UiLanguage.ITALIAN) "NOTE DEL MANICO" else "FRETBOARD NOTES"
    val showTuningNotes get() = if (language == UiLanguage.ITALIAN) "Mostra tutte le note dell’accordatura" else "Show every note for this tuning"
    val targetNotes get() = if (language == UiLanguage.ITALIAN) "NOTE TARGET" else "TARGET NOTES"
    val allTargets get() = if (language == UiLanguage.ITALIAN) "TUTTE LE NOTE TARGET" else "ALL TARGET NOTES"
    val rootNote get() = if (language == UiLanguage.ITALIAN) "TONICA" else "ROOT NOTE"
    val scale get() = if (language == UiLanguage.ITALIAN) "SCALA" else "SCALE"
    val visualTheme get() = if (language == UiLanguage.ITALIAN) "TEMA VISIVO" else "VISUAL THEME"
    val appLanguage get() = if (language == UiLanguage.ITALIAN) "LINGUA DELL’APP" else "APP LANGUAGE"
    val noteNotation get() = if (language == UiLanguage.ITALIAN) "NOMENCLATURA DELLE NOTE" else "NOTE NAMING"
    val italianNotation get() = if (language == UiLanguage.ITALIAN) "Italiana / Latina (DO, RE, MI...)" else "Italian / Latin (DO, RE, MI...)"
    val englishNotation get() = if (language == UiLanguage.ITALIAN) "Anglosassone (C, D, E...)" else "English (C, D, E...)"
    val close get() = if (language == UiLanguage.ITALIAN) "Chiudi" else "Close"

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

    fun themeDescription(theme: VisualTheme) =
        if (language == UiLanguage.ITALIAN) theme.description else when (theme) {
            VisualTheme.DEFAULT -> "A dark studio with cyan, warm gold, and magenta"
            VisualTheme.NEON -> "Cyberpunk contrast with violet, lime, and electric pink"
            VisualTheme.PLAYFUL -> "Cream, berry, mango, and mint with a cherrywood fretboard"
        }
}
