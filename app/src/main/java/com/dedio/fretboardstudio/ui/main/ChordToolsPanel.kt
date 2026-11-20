package com.dedio.fretboardstudio.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dedio.fretboardstudio.model.ChordCategory
import com.dedio.fretboardstudio.model.ChordDictionaryRepository
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
    modifier: Modifier = Modifier
) {
    var selectedTab by remember(state.visualizerMode) {
        mutableStateOf(if (state.visualizerMode == com.dedio.fretboardstudio.ui.VisualizerMode.DICTIONARY) ChordToolsTab.DICTIONARY else ChordToolsTab.TRIADS)
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
                selectedTab = ChordToolsTab.TRIADS
                viewModel.setVisualizerMode(com.dedio.fretboardstudio.ui.VisualizerMode.TRIADS)
            }
            PanelChip(if (italian(state)) "PRONTUARIO" else "CHORDS", selectedTab == ChordToolsTab.DICTIONARY, Modifier.weight(1f)) {
                selectedTab = ChordToolsTab.DICTIONARY
                viewModel.setVisualizerMode(com.dedio.fretboardstudio.ui.VisualizerMode.DICTIONARY)
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
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PanelSection(if (isIt) "TONICA" else "ROOT NOTE")
        Pitch.ALL.chunked(6).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { pitch ->
                    PanelChip(
                        text = pitch.getDisplayName(state.notation),
                        selected = state.selectedRoot == pitch,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.selectRoot(pitch) }
                }
            }
        }

        PanelSection(if (isIt) "ACCORDATURA" else "TUNING")
        TuningSelector(
            selectedTuning = state.selectedTuning,
            onTuningSelected = viewModel::selectTuning,
            modifier = Modifier.fillMaxWidth()
        )

        PanelSection(if (isIt) "QUALITÀ" else "TRIAD QUALITY")
        TriadType.entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { type ->
                    PanelChip(
                        text = if (isIt) type.title else type.englishTitle,
                        selected = state.selectedTriadType == type,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.selectTriadType(type) }
                }
                if (row.size == 1) androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
            }
        }

        PanelSection(if (isIt) "RIVOLTO" else "INVERSION")
        TriadInversion.entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { inversion ->
                    PanelChip(
                        text = if (isIt) inversion.title else when (inversion) {
                            TriadInversion.ALL -> "All"
                            TriadInversion.ROOT_POSITION -> "Root"
                            TriadInversion.FIRST_INVERSION -> "1st"
                            TriadInversion.SECOND_INVERSION -> "2nd"
                        },
                        selected = state.selectedInversion == inversion,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.selectInversion(inversion) }
                }
                if (row.size == 1) androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
            }
        }

        PanelSection(if (isIt) "GRUPPO DI CORDE" else "STRING SET")
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
            listOf(6, 5, 4, 3).forEachIndexed { index, baseString ->
                PanelChip(
                    text = listOf("6–4", "5–3", "4–2", "3–1")[index],
                    selected = state.selectedBaseString == baseString,
                    modifier = Modifier.weight(1f)
                ) { viewModel.selectBaseString(baseString) }
            }
        }

        PanelSection(if (isIt) "ETICHETTE SUL MANICO" else "FRETBOARD LABELS")
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

        val formula = TriadCalculator.buildFormulaExplanation(
            rootPitch = state.selectedRoot,
            triadType = state.selectedTriadType,
            notation = state.notation
        )
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.background).padding(10.dp)
        ) {
            Text(formula.triadTitle, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(formula.summary, color = MaterialTheme.colorScheme.onBackground, fontSize = 12.sp)
            Text(state.selectedTriadType.description, color = MaterialTheme.colorScheme.onBackground.copy(alpha = .65f), fontSize = 11.sp)
        }
        val stringSet = TriadCalculator.getStringSetForBaseString(state.selectedBaseString)
        val voicings = remember(state.selectedRoot, state.selectedTriadType, state.selectedTuning, state.selectedBaseString, state.selectedInversion) {
            TriadCalculator.findVoicings(state.selectedRoot, state.selectedTriadType, stringSet, state.selectedTuning)
                .filter { state.selectedInversion == TriadInversion.ALL || it.inversion == state.selectedInversion }
        }
        PanelSection(if (isIt) "POSIZIONI SUL MANICO" else "FRETBOARD VOICINGS")
        Text(
            text = if (isIt) "${voicings.size} forme trovate · corde ${stringSet.joinToString("–")}" else "${voicings.size} voicings · strings ${stringSet.joinToString("–")}",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .75f),
            fontSize = 11.sp
        )
        voicings.take(12).chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { voicing ->
                    val frets = voicing.notes.joinToString(" · ") { "${it.stringNumber}:${it.fret}" }
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.background).padding(7.dp)
                    ) {
                        Text(if (isIt) voicing.inversion.title else voicing.inversion.name.replace('_', ' '), fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text(frets, fontSize = 9.sp, color = MaterialTheme.colorScheme.onBackground)
                    }
                }
                repeat(3 - row.size) { androidx.compose.foundation.layout.Spacer(Modifier.weight(1f)) }
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
        Pitch.ALL.chunked(6).forEach { row ->
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
                if (row.size == 1) androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
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
                if (row.size == 1) androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
            }
        }
        diagrams.getOrNull(state.selectedDictionaryPosition)?.let { selectedDiagram ->
            ChordDiagramView(diagram = selectedDiagram, modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally))
        }
    }
}

@Composable
private fun PanelSection(text: String) {
    Text(text, color = MaterialTheme.colorScheme.tertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
}

@Composable
private fun PanelChip(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = modifier.clip(RoundedCornerShape(8.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = .2f) else MaterialTheme.colorScheme.background)
            .clickable(onClick = onClick).padding(horizontal = 7.dp, vertical = 8.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = .72f),
        fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, lineHeight = 12.sp
    )
}

private fun italian(state: TriadUiState) = state.language == UiLanguage.ITALIAN
