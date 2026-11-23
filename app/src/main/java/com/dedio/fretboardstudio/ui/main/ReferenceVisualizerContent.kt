package com.dedio.fretboardstudio.ui.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dedio.fretboardstudio.model.BadgeDisplayMode
import com.dedio.fretboardstudio.model.ChordDictionaryRepository
import com.dedio.fretboardstudio.model.DotLabelMode
import com.dedio.fretboardstudio.model.FretboardDisplayScope
import com.dedio.fretboardstudio.model.FretboardOrientation
import com.dedio.fretboardstudio.model.TriadCalculator
import com.dedio.fretboardstudio.model.UiStrings
import com.dedio.fretboardstudio.ui.TriadUiState
import com.dedio.fretboardstudio.ui.TriadViewModel
import com.dedio.fretboardstudio.ui.VisualizerMode
import com.dedio.fretboardstudio.ui.selectedChordSlot
import com.dedio.fretboardstudio.ui.visibleChordSlots
import com.dedio.fretboardstudio.ui.components.FretboardView
import com.dedio.fretboardstudio.ui.components.TuningSelector

private val VisualizerMuted = Color(0xFF777786)

/** A single source of truth for the side panel: opening one always closes the other. */
private enum class SidePanel { NONE, CONTROLS, CHORDS }

@Composable
fun ReferenceVisualizerContent(
    viewModel: TriadViewModel,
    modifier: Modifier = Modifier,
) {
    val state: TriadUiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val strings = UiStrings(state.language)
    var activePanel by rememberSaveable { mutableStateOf(SidePanel.CONTROLS) }
    val focusManager = LocalFocusManager.current

    val closeChordPanel: () -> Unit = {
        activePanel = SidePanel.NONE
        focusManager.clearFocus()
    }

    val controlsWeight by animateFloatAsState(
        targetValue = if (activePanel == SidePanel.CONTROLS) 0.48f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "controls_panel_width",
    )
    val chordPanelWeight by animateFloatAsState(
        targetValue = if (activePanel == SidePanel.CHORDS) 0.48f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "chord_panel_width",
    )

    Row(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(
            6.dp * (controlsWeight + chordPanelWeight).coerceIn(0f, 1f)
        )
    ) {
        if (activePanel == SidePanel.CONTROLS || controlsWeight > 0f) {
            Column(
                modifier = Modifier
                    .weight(controlsWeight.coerceAtLeast(0.001f))
                    .graphicsLayer {
                        val visibleFraction = (controlsWeight / 0.48f).coerceIn(0f, 1f)
                        alpha = visibleFraction
                        translationX = -size.width * (1f - visibleFraction)
                    }
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 6.dp)
            ) {
                Text(
                    text = when (state.visualizerMode) {
                        VisualizerMode.TRIADS -> {
                            val title = state.selectedChordSlot?.displayTitle(state.notation, state.language).orEmpty()
                            val visibleCount = state.visibleChordSlots.size
                            if (visibleCount <= 1) title else title + strings.chordCount(visibleCount)
                        }
                        VisualizerMode.DICTIONARY -> {
                            val root = state.selectedDictionaryRoot.getDisplayName(state.notation)
                            "$root ${strings.chordSymbol(state.selectedDictionaryType)}"
                        }
                        VisualizerMode.SCALES -> "${state.selectedRoot.getDisplayName(state.notation)} ${strings.scaleTitle(state.selectedScale)}"
                    },
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 8.dp)
                )

                ControlLabel(strings.guitar)
                TuningSelector(
                    selectedTuning = state.selectedTuning,
                    onTuningSelected = viewModel::selectTuning,
                    modifier = Modifier.fillMaxWidth()
                )

                ControlLabel(strings.rootNote)
                RootNoteGrid(
                    selectedRoot = state.selectedRoot,
                    notation = state.notation,
                    onRootSelected = viewModel::selectRoot
                )

                if (state.visualizerMode == VisualizerMode.SCALES) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.fretboardNotes,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = strings.showTuningNotes,
                            color = VisualizerMuted,
                            fontSize = 9.sp,
                            modifier = Modifier.semantics { contentDescription = strings.showTuningNotesDescription }
                        )
                    }
                    Switch(
                        checked = state.showTuningNotes,
                        onCheckedChange = viewModel::setShowTuningNotes
                    )
                }

                }
                if (state.visualizerMode == VisualizerMode.SCALES) {
                val usesPentatonicBoxes = state.selectedScale in setOf(
                    com.dedio.fretboardstudio.model.ScaleType.MAJOR_PENTATONIC,
                    com.dedio.fretboardstudio.model.ScaleType.MINOR_PENTATONIC,
                    com.dedio.fretboardstudio.model.ScaleType.BLUES
                )
                ControlLabel(if (usesPentatonicBoxes) strings.boxes else strings.scalePosition)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    (0..5).forEach { position ->
                        SegmentedChip(
                            text = if (position == 0) strings.all else position.toString(),
                            selected = if (position == 0) state.selectedPositions.isEmpty() else position in state.selectedPositions,
                            modifier = Modifier.weight(if (position == 0) 1.3f else 1f),
                            onClick = {
                                if (position == 0) viewModel.clearPositionSelection()
                                else viewModel.togglePosition(position)
                            }
                        )
                    }
                }

                ControlLabel(strings.display)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(
                        DotLabelMode.CLEAN to strings.clean,
                        DotLabelMode.NOTES to strings.notes,
                        DotLabelMode.INTERVALS to strings.intervals
                    ).forEach { (mode, label) ->
                        SegmentedChip(
                            text = label,
                            selected = state.dotLabelMode == mode,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setDotLabelMode(mode) }
                        )
                    }
                }

                ScaleCategorySelector(
                    selectedScale = state.selectedScale,
                    language = state.language,
                    onScaleSelected = viewModel::selectScale
                )

                ControlLabel(strings.targetNotes)
                val targetRows = state.selectedScale.degreeLabels.withIndex().toList().chunked(4)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    targetRows.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                            row.forEach { entry ->
                                SegmentedChip(
                                    text = entry.value,
                                    selected = (entry.index + 1) in state.selectedTargetDegrees,
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.toggleTargetDegree(entry.index + 1) }
                                )
                            }
                            repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
                }

            }
        }

            BoxWithConstraints(
                modifier = Modifier
                    .weight((1f - controlsWeight - chordPanelWeight).coerceAtLeast(0.001f))
                    .fillMaxHeight()
            ) {
            val boardWidth = maxWidth.coerceAtMost((maxHeight * 0.42f).coerceAtMost(320.dp))
            val dictionaryDiagrams = remember(state.selectedDictionaryRoot, state.selectedDictionaryType, state.notation) {
                ChordDictionaryRepository.getDiagramsForChord(
                    state.selectedDictionaryRoot,
                    state.selectedDictionaryType,
                    state.notation
                )
            }
            val fretboardNotes = when (state.visualizerMode) {
                VisualizerMode.TRIADS -> state.allFretNotes
                VisualizerMode.DICTIONARY -> {
                    val selectedDiagram = dictionaryDiagrams.getOrNull(state.selectedDictionaryPosition)
                    if (selectedDiagram != null) {
                        val positionNotes = selectedDiagram.stringFrets.mapIndexedNotNull { index, fret ->
                            if (fret < 0) null else (6 - index) to fret
                        }.toSet()
                        state.allFretNotes.filter { (it.stringNumber to it.fret) in positionNotes }
                            .map { note ->
                                val offset = (note.pitch.semitone - state.selectedDictionaryRoot.semitone + 12) % 12
                                val interval = state.selectedDictionaryType.intervals.firstOrNull {
                                    ((it % 12) + 12) % 12 == offset
                                } ?: offset
                                note.copy(isInScale = true, isTargetNote = true, scaleDegreeLabel = chordDegreeLabel(interval))
                            }
                    } else {
                        val chordIntervals = state.selectedDictionaryType.intervals
                        state.allFretNotes.mapNotNull { note ->
                            val offset = (note.pitch.semitone - state.selectedDictionaryRoot.semitone + 12) % 12
                            val interval = chordIntervals.firstOrNull { ((it % 12) + 12) % 12 == offset }
                                ?: return@mapNotNull null
                            note.copy(
                                isInScale = true,
                                isTargetNote = true,
                                scaleDegreeLabel = chordDegreeLabel(interval)
                            )
                        }
                    }
                }
                VisualizerMode.SCALES -> state.allFretNotes
                    .filter { note ->
                        state.showTuningNotes || TriadCalculator.isNoteInAnyScalePosition(
                            note = note,
                            rootPitch = state.selectedRoot,
                            scaleType = state.selectedScale,
                            tuning = state.selectedTuning,
                            baseString = state.selectedBaseString,
                            positions = state.selectedPositions
                        )
                    }
                    .map { note ->
                        if (state.showTuningNotes) {
                            note.copy(isInScale = true, isTargetNote = false)
                        } else {
                            val degree = state.selectedScale.intervals.indexOf(
                                (note.pitch.semitone - state.selectedRoot.semitone + 12) % 12
                            ) + 1
                            note.copy(isTargetNote = degree in state.selectedTargetDegrees)
                        }
                    }
            }
            FretboardView(
                allNotes = fretboardNotes,
                notation = state.notation,
                triadType = state.selectedTriadType,
                badgeDisplayMode = if (state.visualizerMode == VisualizerMode.TRIADS) BadgeDisplayMode.BOTH else BadgeDisplayMode.NOTE_ONLY,
                dotLabelMode = when (state.visualizerMode) {
                    VisualizerMode.DICTIONARY -> DotLabelMode.INTERVALS
                    VisualizerMode.SCALES -> if (state.showTuningNotes) DotLabelMode.NOTES else state.dotLabelMode
                    VisualizerMode.TRIADS -> state.dotLabelMode
                },
                showChordTargets = state.visualizerMode == VisualizerMode.DICTIONARY,
                displayScope = FretboardDisplayScope.FULL_NECK,
                selectedBaseString = state.selectedBaseString,
                showFingerHints = false,
                lastTappedNote = state.lastTappedNote,
                onNoteClick = viewModel::onFretNoteTapped,
                tuning = state.selectedTuning,
                orientation = FretboardOrientation.VERTICAL,
                isLeftHanded = state.isLeftHanded,
                highlightSelectedString = false,
                highlightTonic = state.highlightTonic && state.visualizerMode == VisualizerMode.TRIADS,
                modifier = Modifier
                    .width(boardWidth)
                    .fillMaxHeight()
                    .align(Alignment.Center)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
                    .size(width = 34.dp, height = 56.dp)
                    .clip(RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp))
                    .background(Color(0xCC12121A))
                    .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f), RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp))
                    .clickable {
                        if (activePanel == SidePanel.CHORDS) {
                            closeChordPanel()
                        } else {
                            focusManager.clearFocus()
                            activePanel = SidePanel.CHORDS
                            // Keep the dictionary tab intact; only leave the Scale view.
                            if (state.visualizerMode == VisualizerMode.SCALES) {
                                viewModel.setVisualizerMode(VisualizerMode.TRIADS)
                            }
                        }
                    }
                    .semantics(mergeDescendants = true) {
                        contentDescription = if (activePanel == SidePanel.CHORDS) {
                            strings.closeChordPanel
                        } else {
                            strings.openChordPanel
                        }
                        role = Role.Button
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (activePanel == SidePanel.CHORDS) "›" else "‹",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
                    .size(width = 34.dp, height = 56.dp)
                    .clip(RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
                    .background(Color(0xCC12121A))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
                    .clickable {
                        if (activePanel == SidePanel.CONTROLS) {
                            activePanel = SidePanel.NONE
                        } else {
                            focusManager.clearFocus()
                            activePanel = SidePanel.CONTROLS
                            // The left panel is exclusively the Scale panel.
                            viewModel.setVisualizerMode(VisualizerMode.SCALES)
                        }
                    }
                    .semantics(mergeDescendants = true) {
                        contentDescription = if (activePanel == SidePanel.CONTROLS) {
                            strings.closeScalesPanel
                        } else {
                            strings.openScalesPanel
                        }
                        role = Role.Button
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (activePanel == SidePanel.CONTROLS) "‹" else "›",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            }
            if (activePanel == SidePanel.CHORDS || chordPanelWeight > 0f) {
                ChordToolsPanel(
                    state = state,
                    viewModel = viewModel,
                    onClose = closeChordPanel,
                    modifier = Modifier
                        .weight(chordPanelWeight.coerceAtLeast(0.001f))
                        .graphicsLayer {
                            val visibleFraction = (chordPanelWeight / 0.48f).coerceIn(0f, 1f)
                            alpha = visibleFraction
                            translationX = size.width * (1f - visibleFraction)
                        }
                        .fillMaxHeight()
                )
            }
        }
}

private fun chordDegreeLabel(interval: Int): String = when (interval) {
    0 -> "1"
    1 -> "♭9"
    2 -> "9"
    3 -> "♭3"
    4 -> "3"
    5 -> "4"
    6 -> "♭5"
    7 -> "5"
    8 -> "♯5"
    9 -> "6"
    10 -> "♭7"
    11 -> "7"
    14 -> "9"
    17 -> "11"
    21 -> "13"
    else -> interval.toString()
}

@Composable
private fun ControlLabel(text: String) {
    Text(
        text = text,
        color = VisualizerMuted,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun SegmentedChip(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) MaterialTheme.colorScheme.primary else VisualizerMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}
