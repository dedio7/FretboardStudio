package com.example.triadguitar.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.triadguitar.model.NoteNotation
import com.example.triadguitar.model.UiLanguage
import com.example.triadguitar.model.UiStrings
import com.example.triadguitar.model.VisualTheme

@Composable
fun SettingsDialog(
    currentNotation: NoteNotation,
    currentLanguage: UiLanguage,
    currentTheme: VisualTheme,
    onNotationChange: (NoteNotation) -> Unit,
    onLanguageChange: (UiLanguage) -> Unit,
    onThemeChange: (VisualTheme) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = UiStrings(currentLanguage)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "⚙️ ${strings.settingsTitle}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                SettingsSectionTitle(strings.visualTheme)
                VisualTheme.entries.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeChange(theme) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentTheme == theme,
                            onClick = { onThemeChange(theme) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(theme.title, fontWeight = FontWeight.Bold)
                            Text(strings.themeDescription(theme), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                SettingsSectionTitle(strings.appLanguage)
                UiLanguage.entries.forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageChange(language) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentLanguage == language,
                            onClick = { onLanguageChange(language) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(language.displayName, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                SettingsSectionTitle(strings.noteNotation)
                NoteNotation.entries.forEach { notation ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNotationChange(notation) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentNotation == notation,
                            onClick = { onNotationChange(notation) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (notation == NoteNotation.ITALIAN) strings.italianNotation else strings.englishNotation,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.close, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    )
    Spacer(Modifier.padding(bottom = 4.dp))
}
