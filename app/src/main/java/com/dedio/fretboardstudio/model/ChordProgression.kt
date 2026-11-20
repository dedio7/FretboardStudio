package com.dedio.fretboardstudio.model

data class ProgressionChord(
    val rootPitch: Pitch,
    val triadType: TriadType,
    val durationBeats: Int = 4,
    val romanNumeral: String = ""
) {
    fun getDisplayName(notation: NoteNotation): String {
        val rootName = rootPitch.getDisplayName(notation)
        val symbol = triadType.symbol
        return "$rootName ${triadType.title} $symbol ($romanNumeral)"
    }
}

data class ChordProgression(
    val id: String,
    val title: String,
    val description: String,
    val keyRoot: Pitch,
    val chords: List<ProgressionChord>,
    val defaultBpm: Int = 100
) {
    companion object {
        fun getPresets(keyRoot: Pitch = Pitch(0)): List<ChordProgression> {
            val root = keyRoot.semitone

            // 1. Pop Standard (I - V - vi - IV)
            val pop1564 = ChordProgression(
                id = "pop_1564",
                title = "Pop Magic (I - V - vi - IV)",
                description = "La progressione più famosa del pop moderno",
                keyRoot = keyRoot,
                defaultBpm = 110,
                chords = listOf(
                    ProgressionChord(Pitch.fromSemitone(root + 0), TriadType.MAJOR, 4, "I"),
                    ProgressionChord(Pitch.fromSemitone(root + 7), TriadType.MAJOR, 4, "V"),
                    ProgressionChord(Pitch.fromSemitone(root + 9), TriadType.MINOR, 4, "vi"),
                    ProgressionChord(Pitch.fromSemitone(root + 5), TriadType.MAJOR, 4, "IV")
                )
            )

            // 2. Jazz Standard (ii - V - I)
            val jazz251 = ChordProgression(
                id = "jazz_251",
                title = "Jazz Classic (ii - V - I)",
                description = "La cadenza fondamentale della musica Jazz e Bossanova",
                keyRoot = keyRoot,
                defaultBpm = 95,
                chords = listOf(
                    ProgressionChord(Pitch.fromSemitone(root + 2), TriadType.MINOR, 4, "ii"),
                    ProgressionChord(Pitch.fromSemitone(root + 7), TriadType.MAJOR, 4, "V"),
                    ProgressionChord(Pitch.fromSemitone(root + 0), TriadType.MAJOR, 8, "I")
                )
            )

            // 3. Cadenza Andalusa (i - bVII - bVI - V)
            val andalusian = ChordProgression(
                id = "andalusian",
                title = "Cadenza Andalusa (i - ♭VII - ♭VI - V)",
                description = "Suono flamenco e spagnolo passionale",
                keyRoot = keyRoot,
                defaultBpm = 120,
                chords = listOf(
                    ProgressionChord(Pitch.fromSemitone(root + 0), TriadType.MINOR, 4, "i"),
                    ProgressionChord(Pitch.fromSemitone(root + 10), TriadType.MAJOR, 4, "♭VII"),
                    ProgressionChord(Pitch.fromSemitone(root + 8), TriadType.MAJOR, 4, "♭VI"),
                    ProgressionChord(Pitch.fromSemitone(root + 7), TriadType.MAJOR, 4, "V")
                )
            )

            // 4. Blues 12-Bar (I - IV - I - V - IV - I)
            val blues12Bar = ChordProgression(
                id = "blues_12",
                title = "Blues 12 Battute",
                description = "Giro Blues classico a 12 battute",
                keyRoot = keyRoot,
                defaultBpm = 100,
                chords = listOf(
                    ProgressionChord(Pitch.fromSemitone(root + 0), TriadType.MAJOR, 4, "I"),
                    ProgressionChord(Pitch.fromSemitone(root + 5), TriadType.MAJOR, 4, "IV"),
                    ProgressionChord(Pitch.fromSemitone(root + 0), TriadType.MAJOR, 4, "I"),
                    ProgressionChord(Pitch.fromSemitone(root + 7), TriadType.MAJOR, 4, "V"),
                    ProgressionChord(Pitch.fromSemitone(root + 5), TriadType.MAJOR, 4, "IV"),
                    ProgressionChord(Pitch.fromSemitone(root + 0), TriadType.MAJOR, 4, "I")
                )
            )

            return listOf(pop1564, jazz251, andalusian, blues12Bar)
        }
    }
}
