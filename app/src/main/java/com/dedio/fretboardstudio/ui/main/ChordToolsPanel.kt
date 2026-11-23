package com.dedio.fretboardstudio.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dedio.fretboardstudio.model.ChordCategory
import com.dedio.fretboardstudio.model.ChordDictionaryRepository
import com.dedio.fretboardstudio.model.ChordTypeInfo
import com.dedio.fretboardstudio.model.DotLabelMode
import com.dedio.fretboardstudio.model.Pitch
import com.dedio.fretboardstudio.model.TriadCalculator
import com.dedio.fretboardstudio.model.TriadInversion
import com.dedio.fretboardstudio.model.TriadType
import com.dedio.fretboardstudio.model.UiLanguage
import com.dedio.fretboardstudio.ui.TriadUiState
import com.dedio.fretboardstudio.ui.TriadViewModel
import com.dedio.fretboardstudio.ui.components.ChordDiagramView
import com.dedio.fretboardstudio.ui.components.TuningSelector

private enum class ChordToolsTab { TRIADS, DICTIONARY }

@Composable
fun ChordToolsPanel(
    state: TriadUiState,
    viewModel: TriadViewModel,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
) {
    // Derived from the ViewModel so the tab cannot drift out of sync when the panel
    // is closed/reopened or the mode changes from elsewhere.
    val selectedTab = if (state.visualizerMode == com.dedio.fretboardstudio.ui.VisualizerMode.DICTIONARY) {
        ChordToolsTab.DICTIONARY
    } else {
        ChordToolsTab.TRIADS
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            PanelChip(if (italian(state)) "TRIADI" else "TRIADS", selectedTab == ChordToolsTab.TRIADS, Modifier.weight(1f)) {
                viewModel.setVisualizerMode(com.dedio.fretboardstudio.ui.VisualizerMode.TRIADS)
            }
            PanelChip(if (italian(state)) "PRONTUARIO" else "CHORDS", selectedTab == ChordToolsTab.DICTIONARY, Modifier.weight(1f)) {
                viewModel.setVisualizerMode(com.dedio.fretboardstudio.ui.VisualizerMode.DICTIONARY)
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                    .clickable(onClick = onClose)
                    .semantics(mergeDescendants = true) {
                        contentDescription = if (italian(state)) "Chiudi pannello accordi" else "Close chord panel"
                        role = Role.Button
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✕",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        when (selectedTab) {
            ChordToolsTab.TRIADS -> TriadControls(state, viewModel)
            ChordToolsTab.DICTIONARY -> ChordDictionaryControls(state, viewModel)
        }
    }
}

@Composable
private fun TriadControls(state: TriadUiState, viewModel: TriadViewModel) {
    val isIt = italian(state)
    val expandedSlotId = state.expandedSlotId

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PanelSection(if (isIt) "ACCORDATURA" else "TUNING")
        TuningSelector(
            selectedTuning = state.selectedTuning,
            onTuningSelected = viewModel::selectTuning,
            modifier = Modifier.fillMaxWidth()
        )

        PanelSection(if (isIt) "ETICHETTE" else "LABELS")
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            listOf(
                DotLabelMode.CLEAN to if (isIt) "Vuote" else "Empty",
                DotLabelMode.NOTES to if (isIt) "Note" else "Notes",
                DotLabelMode.INTERVALS to if (isIt) "Intervalli" else "Intervals"
            ).forEach { (mode, label) ->
                PanelChip(
                    text = label,
                    selected = state.dotLabelMode == mode,
                    modifier = Modifier.weight(1f)
                ) { viewModel.setDotLabelMode(mode) }
            }
        }

        PanelSection(if (isIt) "SEQUENZA (1-5)" else "SEQUENCE (1-5)")

        // Color legend & Add button row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                state.triadSequenceSlots.forEach { slot ->
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(slot.color)
                    )
                }
            }
            val canAdd = state.triadSequenceSlots.size < 5
            PanelChip(
                text = "+ (${state.triadSequenceSlots.size}/5)",
                selected = false,
                modifier = Modifier
            ) {
                if (canAdd) viewModel.addChordSlot()
            }
        }

        // View + tonic controls on a single row to save vertical space.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PanelChip(
                text = if (isIt) "TONICA" else "TONIC",
                selected = state.highlightTonic,
                modifier = Modifier.weight(1f)
            ) { viewModel.setHighlightTonic(!state.highlightTonic) }
            PanelChip(
                text = if (isIt) "Tutti" else "All",
                selected = !state.isolateSelectedSlot,
                modifier = Modifier.weight(1f)
            ) { viewModel.setSlotIsolation(false) }
            PanelChip(
                text = if (isIt) "Solo" else "Solo",
                selected = state.isolateSelectedSlot,
                modifier = Modifier.weight(1f)
            ) { viewModel.setSlotIsolation(true) }
        }

        // Slots list
        state.triadSequenceSlots.forEachIndexed { index, slot ->
            val isExpanded = expandedSlotId == slot.id
            val chordTitle = slot.displayTitle(state.notation, state.language)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .border(
                        width = if (isExpanded) 1.5.dp else 1.dp,
                        color = slot.color.copy(alpha = if (slot.isVisible) 0.8f else 0.3f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Chord name gets the full width: never stack letters vertically when
                // the panel is narrow.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleSlotExpanded(slot.id) },
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(slot.color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${index + 1}", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = chordTitle,
                        modifier = Modifier.weight(1f),
                        color = if (slot.isVisible) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isExpanded) "▴" else "▾",
                        color = slot.color,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // One compact action row: on/off, reorder, remove. The old ambiguous
                // "solo" icon is gone (Solo/All above already controls isolation).
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PanelChip(
                        text = if (slot.isVisible) "ON" else "OFF",
                        selected = slot.isVisible,
                        modifier = Modifier.size(38.dp)
                    ) { viewModel.toggleSlotVisibility(slot.id) }

                    if (index > 0) {
                        PanelChip(
                            text = if (isIt) "SU" else "UP",
                            selected = false,
                            modifier = Modifier.size(44.dp)
                        ) { viewModel.moveSlot(index, -1) }
                    }
                    if (index < state.triadSequenceSlots.size - 1) {
                        PanelChip(
                            text = if (isIt) "GIÙ" else "DOWN",
                            selected = false,
                            modifier = Modifier.size(44.dp)
                        ) { viewModel.moveSlot(index, 1) }
                    }
                    if (state.triadSequenceSlots.size > 1) {
                        PanelChip(text = "✕", selected = false, modifier = Modifier.size(28.dp)) {
                            viewModel.removeChordSlot(slot.id)
                        }
                    }
                }

                if (isExpanded) {
                    Spacer(Modifier.height(2.dp))
                    Text(if (isIt) "TONICA" else "ROOT", color = MaterialTheme.colorScheme.tertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Pitch.ALL.chunked(4).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxWidth()) {
                            row.forEach { pitch ->
                                PanelChip(
                                    text = pitch.getDisplayName(state.notation),
                                    selected = slot.root == pitch,
                                    modifier = Modifier.weight(1f)
                                ) { viewModel.updateChordSlot(slot.id) { it.copy(root = pitch) } }
                            }
                        }
                    }

                    Text(if (isIt) "QUALITÀ" else "QUALITY", color = MaterialTheme.colorScheme.tertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    TriadType.entries.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxWidth()) {
                            row.forEach { type ->
                                PanelChip(
                                    text = if (isIt) type.title else type.englishTitle,
                                    selected = slot.quality == type,
                                    modifier = Modifier.weight(1f)
                                ) { viewModel.updateChordSlot(slot.id) { it.copy(quality = type) } }
                            }
                            if (row.size < 3) repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }

                    Text(if (isIt) "RIVOLTO" else "INVERSION", color = MaterialTheme.colorScheme.tertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    TriadInversion.entries.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxWidth()) {
                            row.forEach { inv ->
                                PanelChip(
                                    text = if (isIt) inv.title else inv.englishTitle,
                                    selected = slot.inversion == inv,
                                    modifier = Modifier.weight(1f)
                                ) { viewModel.updateChordSlot(slot.id) { it.copy(inversion = inv) } }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }

                    Text(if (isIt) "CORDE" else "STRINGS", color = MaterialTheme.colorScheme.tertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf(6, 5, 4, 3).forEachIndexed { idx, baseString ->
                            PanelChip(
                                text = listOf("6–4", "5–3", "4–2", "3–1")[idx],
                                selected = slot.baseString == baseString,
                                modifier = Modifier.weight(1f)
                            ) { viewModel.updateChordSlot(slot.id) { it.copy(baseString = baseString) } }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChordDictionaryControls(state: TriadUiState, viewModel: TriadViewModel) {
    val root = state.selectedDictionaryRoot
    val selectedType = state.selectedDictionaryType
    val category = selectedType.category
    val types = ChordDictionaryRepository.ALL_TYPES.filter { it.category == category }
    val diagrams = remember(root, selectedType, state.notation) {
        ChordDictionaryRepository.getDiagramsForChord(root, selectedType, state.notation)
    }
    val isIt = italian(state)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        PanelSection(if (isIt) "TONICA" else "ROOT")
        Pitch.ALL.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { pitch ->
                    PanelChip(pitch.getDisplayName(state.notation), root == pitch, Modifier.weight(1f)) { viewModel.selectDictionaryRoot(pitch) }
                }
            }
        }
        PanelSection(if (isIt) "ACCORDATURA" else "TUNING")
        TuningSelector(
            selectedTuning = state.selectedTuning,
            onTuningSelected = viewModel::selectTuning,
            modifier = Modifier.fillMaxWidth()
        )
        PanelSection(if (isIt) "CATEGORIA" else "CATEGORY")
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            ChordCategory.entries.forEach { item ->
                PanelChip(
                    text = when (item) {
                        ChordCategory.TRIADS -> if (isIt) "Triadi" else "Triads"
                        ChordCategory.SEVENTHS -> if (isIt) "7e" else "7ths"
                        ChordCategory.EXTENDED -> if (isIt) "Estesi" else "Extended"
                    },
                    selected = category == item,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.selectDictionaryType(ChordDictionaryRepository.ALL_TYPES.first { it.category == item })
                }
            }
        }
        PanelSection(if (isIt) "TIPO DI ACCORDO" else "CHORD TYPE")
        types.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { type ->
                    PanelChip("${type.name} ${type.symbol}", selectedType.id == type.id, Modifier.weight(1f)) {
                        viewModel.selectDictionaryType(type)
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        Text(
            text = "${root.getDisplayName(state.notation)} ${selectedType.symbol} · ${selectedType.description}",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp))
                .background(MaterialTheme.colorScheme.background).padding(9.dp)
        )
        PanelSection(if (isIt) "POSIZIONI DISPONIBILI" else "AVAILABLE POSITIONS")
        diagrams.chunked(2).forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEachIndexed { columnIndex, diagram ->
                    val positionIndex = rowIndex * 2 + columnIndex
                    PanelChip(
                        text = "${positionIndex + 1}. ${diagram.positionLabel}",
                        selected = state.selectedDictionaryPosition == positionIndex,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.selectDictionaryPosition(positionIndex) }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        diagrams.getOrNull(state.selectedDictionaryPosition)?.let { selectedDiagram ->
            ChordDiagramView(diagram = selectedDiagram, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
private fun PanelSection(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.tertiary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun PanelChip(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = modifier.clip(RoundedCornerShape(8.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .2f) else MaterialTheme.colorScheme.background)
            .clickable(onClick = onClick).padding(horizontal = 4.dp, vertical = 8.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = .72f),
        fontSize = 10.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        lineHeight = 12.sp,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis
    )
}

private fun italian(state: TriadUiState) = state.language == UiLanguage.ITALIAN
