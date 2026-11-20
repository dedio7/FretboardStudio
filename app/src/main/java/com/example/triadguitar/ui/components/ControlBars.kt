package com.example.triadguitar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.example.triadguitar.model.GuitarTuning

@Composable
fun TuningSelector(
    selectedTuning: GuitarTuning,
    onTuningSelected: (GuitarTuning) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(9.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(9.dp))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 11.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "${selectedTuning.title}  ▾",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            GuitarTuning.entries.forEach { tuning ->
                DropdownMenuItem(
                    text = { Text(tuning.title) },
                    onClick = {
                        onTuningSelected(tuning)
                        expanded = false
                    }
                )
            }
        }
    }
}
