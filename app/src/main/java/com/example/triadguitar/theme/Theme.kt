package com.example.triadguitar.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.triadguitar.model.VisualTheme

@Composable
fun TriadGuitarTheme(
    visualTheme: VisualTheme = VisualTheme.DEFAULT,
    content: @Composable () -> Unit
) {
    val colorScheme = if (visualTheme == VisualTheme.PLAYFUL) {
        lightColorScheme(
            primary = visualTheme.primaryColor,
            secondary = visualTheme.secondaryColor,
            tertiary = visualTheme.tertiaryColor,
            background = visualTheme.backgroundColor,
            surface = visualTheme.surfaceColor,
            surfaceVariant = visualTheme.fretboardBgColor,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = Color(0xFF27232C),
            onSurface = Color(0xFF27232C)
        )
    } else {
        darkColorScheme(
            primary = visualTheme.primaryColor,
            secondary = visualTheme.secondaryColor,
            tertiary = visualTheme.tertiaryColor,
            background = visualTheme.backgroundColor,
            surface = visualTheme.surfaceColor,
            surfaceVariant = visualTheme.fretboardBgColor,
            onPrimary = Color.White,
            onSecondary = Color(0xFF17131B),
            onTertiary = Color(0xFF17131B),
            onBackground = Color.White,
            onSurface = Color.White
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
