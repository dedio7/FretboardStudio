package com.dedio.fretboardstudio.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dedio.fretboardstudio.model.NoteNotation
import com.dedio.fretboardstudio.model.Pitch
import com.dedio.fretboardstudio.model.ScaleType
import com.dedio.fretboardstudio.model.UiLanguage
import com.dedio.fretboardstudio.model.UiStrings

@Composable
fun RootNoteGrid(
    selectedRoot: Pitch,
    notation: NoteNotation,
    onRootSelected: (Pitch) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Pitch.ALL.chunked(3).forEach { pitchRow ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                pitchRow.forEach { pitch ->
                    val selected = pitch == selectedRoot
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface)
                            .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(9.dp))
                            .clickable { onRootSelected(pitch) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            pitch.getDisplayName(notation),
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScaleCategorySelector(
    selectedScale: ScaleType,
    language: UiLanguage = UiLanguage.ITALIAN,
    onScaleSelected: (ScaleType) -> Unit
) {
    var expandedCategory by remember(selectedScale.category) { mutableStateOf(selectedScale.category) }
    val groups = ScaleType.values().groupBy { it.category }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        groups.forEach { (category, scales) ->
            val expanded = expandedCategory == category
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { expandedCategory = if (expanded) "" else category }
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(UiStrings(language).scaleCategory(category), color = if (expanded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Text(if (expanded) "⌃" else "⌄", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            if (expanded) {
                scales.forEach { scale ->
                    val selected = scale == selectedScale
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface)
                            .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface, RoundedCornerShape(9.dp))
                            .clickable { onScaleSelected(scale); expandedCategory = category }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            UiStrings(language).scaleTitle(scale),
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
