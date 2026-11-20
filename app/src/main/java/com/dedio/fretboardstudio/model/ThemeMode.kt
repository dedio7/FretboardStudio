package com.dedio.fretboardstudio.model

import androidx.compose.ui.graphics.Color

enum class VisualTheme(
    val title: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val tertiaryColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val cardBorderColor: Color,
    val fretboardBgColor: Color
) {
    DEFAULT(
        title = "Guitar Visualizer",
        description = "Studio scuro con ciano, oro caldo e magenta",
        primaryColor = Color(0xFF00D7E8),
        secondaryColor = Color(0xFFFFC44D),
        tertiaryColor = Color(0xFFE85C9E),
        backgroundColor = Color(0xFF100F16),
        surfaceColor = Color(0xFF1D1A22),
        cardBorderColor = Color(0xFF3A303A),
        fretboardBgColor = Color(0xFF56331F)
    ),
    NEON(
        title = "Cyber Neon",
        description = "Contrasto cyberpunk con violetto, lime e rosa elettrico",
        primaryColor = Color(0xFFB967FF),
        secondaryColor = Color(0xFFB8FF2C),
        tertiaryColor = Color(0xFFFF2E88),
        backgroundColor = Color(0xFF05050B),
        surfaceColor = Color(0xFF10101D),
        cardBorderColor = Color(0xFF39245A),
        fretboardBgColor = Color(0xFF171326)
    ),
    PLAYFUL(
        title = "Playful",
        description = "Crema, mirtillo, mango e menta con manico color legno di ciliegio",
        primaryColor = Color(0xFF5636B5),
        secondaryColor = Color(0xFFE58A35),
        tertiaryColor = Color(0xFF16877D),
        backgroundColor = Color(0xFFFFF6E8),
        surfaceColor = Color(0xFFFFFCF5),
        cardBorderColor = Color(0xFFE8D8BE),
        fretboardBgColor = Color(0xFF704552)
    )
}
