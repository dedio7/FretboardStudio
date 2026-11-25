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
import androidx.compose.foundation.layout.widthIn
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
import com.dedio.fretboardstudio.model.DotLabelMode
import com.dedio.fretboardstudio.model.Pitch
import com.dedio.fretboardstudio.model.TriadInversion
import com.dedio.fretboardstudio.model.TriadType
import com.dedio.fretboardstudio.model.UiStrings
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
    val strings = UiStrings(state.language)
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
            PanelChip(strings.triadsTab, selectedTab == ChordToolsTab.TRIADS, Modifier.weight(1f)) {
                viewModel.setVisualizerMode(com.dedio.fretboardstudio.ui.VisualizerMode.TRIADS)
            }
            PanelChip(strings.dictionaryTab, selectedTab == ChordToolsTab.DICTIONARY, Modifier.weight(1f)) {
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
                        contentDescription = strings.closeChordPanel
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
    val strings = UiStrings(state.language)
    val expandedSlotId = state.expandedSlotId

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PanelSection(strings.tuning)
        TuningSelector(
            selectedTuning = state.selectedTuning,
            onTuningSelected = viewModel::selectTuning,
            modifier = Modifier.fillMaxWidth()
        )

        PanelSection(strings.display)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            listOf(
                DotLabelMode.CLEAN to strings.clean,
                DotLabelMode.NOTES to strings.notes,
                DotLabelMode.INTERVALS to strings.intervals
            ).forEach { (mode, label) ->
                PanelChip(
                    text = label,
                    selected = state.dotLabelMode == mode,
                    modifier = Modifier.weight(1f)
                ) { viewModel.setDotLabelMode(mode) }
            }
            PanelChip(
                text = strings.highlightTonic,
                selected = state.highlightTonic,
                modifier = Modifier.weight(1f)
            ) { viewModel.setHighlightTonic(!state.highlightTonic) }
        }

        PanelSection(strings.sequence)

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
                            .background(slot.color.copy(alpha = if (slot.isVisible) 1f else 0.3f))
                    )
                }
            }
            val canAdd = state.triadSequenceSlots.size < 5
            PanelChip(
                text = "+ (${state.triadSequenceSlots.size}/5)",
                selected = false,
                modifier = Modifier.semantics { contentDescription = strings.addChord }
            ) {
                if (canAdd) viewModel.addChordSlot()
            }
        }

        // Tonic now lives under "VISUALIZZAZIONE"; this row keeps only isolation.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PanelChip(
                text = strings.allChords,
                selected = !state.isolateSelectedSlot,
                modifier = Modifier.weight(1f)
            ) { viewModel.setSlotIsolation(false) }
            PanelChip(
                text = strings.soloChord,
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
                // Title and ON/OFF sit together; the collapse caret moves to the far right.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(slot.color.copy(alpha = if (slot.isVisible) 1f else 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text((index + 1).toString(), color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = chordTitle,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.toggleSlotExpanded(slot.id) },
                        color = if (slot.isVisible) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                    PanelChip(
                        text = if (slot.isVisible) "ON" else "OFF",
                        selected = slot.isVisible,
                        modifier = Modifier
                            .height(28.dp)
                            .widthIn(min = 36.dp)
                            .semantics { contentDescription = if (slot.isVisible) strings.hideChord else strings.showChord }
                    ) { viewModel.toggleSlotVisibility(slot.id) }
                    Text(
                        text = if (isExpanded) "▴" else "▾",
                        color = slot.color.copy(alpha = if (slot.isVisible) 1f else 0.4f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { viewModel.toggleSlotExpanded(slot.id) }
                            .semantics {
                                contentDescription = if (isExpanded) strings.collapseChord else strings.expandChord
                            }
                    )
                }

                // Reorder/remove stay in their own row; visibility is in the header.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (index > 0) {
                        PanelChip(
                            text = strings.moveUp,
                            selected = false,
                            modifier = Modifier.size(44.dp)
                        ) { viewModel.moveSlot(index, -1) }
                    }
                    if (index < state.triadSequenceSlots.size - 1) {
                        PanelChip(
                            text = strings.moveDown,
                            selected = false,
                            modifier = Modifier.size(44.dp)
                        ) { viewModel.moveSlot(index, 1) }
                    }
                    if (state.triadSequenceSlots.size > 1) {
                        PanelChip(
                            text = "−",
                            selected = false,
                            modifier = Modifier
                                .size(28.dp)
                                .semantics { contentDescription = strings.removeChord }
                        ) {
                            viewModel.removeChordSlot(slot.id)
                        }
                    }
                }

                if (isExpanded) {
                    Spacer(Modifier.height(2.dp))
                    Text(strings.rootNote, color = MaterialTheme.colorScheme.tertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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

                    Text(strings.chordType, color = MaterialTheme.colorScheme.tertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    TriadType.entries.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxWidth()) {
                            row.forEach { type ->
                                PanelChip(
                                    text = strings.triadTypeName(type),
                                    selected = slot.quality == type,
                                    modifier = Modifier.weight(1f)
                                ) { viewModel.updateChordSlot(slot.id) { it.copy(quality = type) } }
                            }
                            if (row.size < 3) repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }

                    Text(strings.inversion, color = MaterialTheme.colorScheme.tertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    TriadInversion.entries.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxWidth()) {
                            row.forEach { inv ->
                                PanelChip(
                                    text = strings.triadInversionName(inv),
                                    selected = slot.inversion == inv,
                                    modifier = Modifier.weight(1f)
                                ) { viewModel.updateChordSlot(slot.id) { it.copy(inversion = inv) } }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }

                    Text(strings.fretboardStrings, color = MaterialTheme.colorScheme.tertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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
    val strings = UiStrings(state.language)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        PanelSection(strings.rootNote)
        Pitch.ALL.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { pitch ->
                    PanelChip(pitch.getDisplayName(state.notation), root == pitch, Modifier.weight(1f)) { viewModel.selectDictionaryRoot(pitch) }
                }
            }
        }
        PanelSection(strings.tuning)
        TuningSelector(
            selectedTuning = state.selectedTuning,
            onTuningSelected = viewModel::selectTuning,
            modifier = Modifier.fillMaxWidth()
        )
        PanelSection(strings.display)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            listOf(
                DotLabelMode.CLEAN to strings.clean,
                DotLabelMode.NOTES to strings.notes,
                DotLabelMode.INTERVALS to strings.intervals
            ).forEach { (mode, label) ->
                PanelChip(
                    text = label,
                    selected = state.dotLabelMode == mode,
                    modifier = Modifier.weight(1f)
                ) { viewModel.setDotLabelMode(mode) }
            }
        }
        PanelSection(strings.category)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            ChordCategory.entries.forEach { item ->
                PanelChip(
                    text = strings.chordCategory(item),
                    selected = category == item,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.selectDictionaryType(ChordDictionaryRepository.ALL_TYPES.first { it.category == item })
                }
            }
        }
        PanelSection(strings.chordType)
        types.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { type ->
                    PanelChip(
                        text = strings.chordSymbol(type),
                        selected = selectedType.id == type.id,
                        modifier = Modifier.weight(1f).semantics { contentDescription = strings.chordTypeName(type) }
                    ) {
                        viewModel.selectDictionaryType(type)
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        val formula = strings.chordFormula(selectedType)
        Text(
            text = "${root.getDisplayName(state.notation)} ${selectedType.symbol} · $formula".trim(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp))
                .background(MaterialTheme.colorScheme.background).padding(9.dp)
        )
        PanelSection(strings.positions)
        diagrams.chunked(2).forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEachIndexed { columnIndex, diagram ->
                    val positionIndex = rowIndex * 2 + columnIndex
                    PanelChip(
                        text = "${positionIndex + 1}",
                        selected = state.selectedDictionaryPosition == positionIndex,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = diagram.positionLabel }
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
