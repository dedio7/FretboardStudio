package com.dedio.fretboardstudio.model

enum class CagedShape(
    val letter: String,
    val title: String,
    val colorHex: Long
) {
    C_SHAPE("C", "Forma DO (C-Shape)", 0xFFFF2A6D),
    A_SHAPE("A", "Forma LA (A-Shape)", 0xFF05FFA1),
    G_SHAPE("G", "Forma SOL (G-Shape)", 0xFFFFD600),
    E_SHAPE("E", "Forma MI (E-Shape)", 0xFF00F0FF),
    D_SHAPE("D", "Forma RE (D-Shape)", 0xFFB537F2);

    companion object {
        fun identifyShape(baseStringNumber: Int, inversion: TriadInversion): CagedShape {
            return when (baseStringNumber) {
                // Set 1-2-3 (3ª corda base - SOL)
                3 -> when (inversion) {
                    TriadInversion.ROOT_POSITION -> C_SHAPE
                    TriadInversion.FIRST_INVERSION -> A_SHAPE
                    TriadInversion.SECOND_INVERSION -> E_SHAPE
                    TriadInversion.ALL -> C_SHAPE
                }
                // Set 2-3-4 (4ª corda base - RE)
                4 -> when (inversion) {
                    TriadInversion.ROOT_POSITION -> A_SHAPE
                    TriadInversion.FIRST_INVERSION -> E_SHAPE
                    TriadInversion.SECOND_INVERSION -> C_SHAPE
                    TriadInversion.ALL -> A_SHAPE
                }
                // Set 3-4-5 (5ª corda base - LA)
                5 -> when (inversion) {
                    TriadInversion.ROOT_POSITION -> E_SHAPE
                    TriadInversion.FIRST_INVERSION -> C_SHAPE
                    TriadInversion.SECOND_INVERSION -> A_SHAPE
                    TriadInversion.ALL -> E_SHAPE
                }
                // Set 4-5-6 (6ª corda base - MI)
                6 -> when (inversion) {
                    TriadInversion.ROOT_POSITION -> E_SHAPE
                    TriadInversion.FIRST_INVERSION -> D_SHAPE
                    TriadInversion.SECOND_INVERSION -> C_SHAPE
                    TriadInversion.ALL -> E_SHAPE
                }
                else -> C_SHAPE
            }
        }
    }
}
