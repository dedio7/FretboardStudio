package com.dedio.fretboardstudio.ui.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dedio.fretboardstudio.model.BadgeDisplayMode
import com.dedio.fretboardstudio.model.DotLabelMode
import com.dedio.fretboardstudio.model.FretboardDisplayScope
import com.dedio.fretboardstudio.model.FretboardOrientation
import com.dedio.fretboardstudio.model.TriadCalculator
import com.dedio.fretboardstudio.model.TriadType
import com.dedio.fretboardstudio.model.UiStrings
import com.dedio.fretboardstudio.ui.TriadUiState
import com.dedio.fretboardstudio.ui.TriadViewModel
import com.dedio.fretboardstudio.ui.components.FretboardView
import com.dedio.fretboardstudio.ui.components.TuningSelector

private val VisualizerMuted = Color(0xFF777786)

@Composable
fun ReferenceVisualizerContent(
    viewModel: TriadViewModel,
    modifier: Modifier = Modifier,
) {
    val state: TriadUiState = viewModel.uiState.collectAsStateWithLifecycle().value
    var controlsVisible by rememberSaveable { mutableStateOf(value = true) }
    val controlsWeight by animateFloatAsState(
        targetValue = if (controlsVisible) 0.48f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "controls_panel_width",
    )

    Row(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(if (controlsVisible) 6.dp else 0.dp)
    ) {
            Column(
                modifier = Modifier
                    .weight(controlsWeight.coerceAtLeast(0.001f))
                    .graphicsLayer {
                        val visibleFraction = (controlsWeight / 0.48f).coerceIn(0f, 1f)
                        translationX = -size.width * (1f - visibleFraction)
                    }
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 6.dp)
            ) {
                val strings = UiStrings(state.language)
                Text(
                    text = "${state.selectedRoot.getDisplayName(state.notation)} ${strings.scaleTitle(state.selectedScale)}",
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
                            fontSize = 9.sp
                        )
                    }
                    Switch(
                        checked = state.showTuningNotes,
                        onCheckedChange = viewModel::setShowTuningNotes
                    )
                }

                ControlLabel(strings.rootNote)
                RootNoteGrid(
                    selectedRoot = state.selectedRoot,
                    notation = state.notation,
                    onRootSelected = viewModel::selectRoot
                )

                val usesPentatonicBoxes = state.selectedScale in setOf(
                    com.dedio.fretboardstudio.model.ScaleType.MAJOR_PENTATONIC,
                    com.dedio.fretboardstudio.model.ScaleType.MINOR_PENTATONIC,
                    com.dedio.fretboardstudio.model.ScaleType.BLUES
                )
                ControlLabel(strings.scale)
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
                val allDegrees = (1..state.selectedScale.degreeLabels.size).toSet()
                val targetRows = state.selectedScale.degreeLabels.withIndex().toList().chunked(4)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SegmentedChip(
                        text = strings.allTargets,
                        selected = state.selectedTargetDegrees == allDegrees,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = viewModel::toggleAllTargetDegrees
                    )
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

            Box(
                modifier = Modifier
                    .weight((1f - controlsWeight).coerceAtLeast(0.001f))
                    .fillMaxHeight()
            ) {
            FretboardView(
                allNotes = state.allFretNotes
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
                    },
                notation = state.notation,
                triadType = TriadType.MAJOR,
                badgeDisplayMode = BadgeDisplayMode.NOTE_ONLY,
                dotLabelMode = if (state.showTuningNotes) DotLabelMode.NOTES else state.dotLabelMode,
                showChordTargets = false,
                displayScope = FretboardDisplayScope.FULL_NECK,
                selectedBaseString = state.selectedBaseString,
                showFingerHints = false,
                lastTappedNote = state.lastTappedNote,
                onNoteClick = viewModel::onFretNoteTapped,
                tuning = state.selectedTuning,
                orientation = FretboardOrientation.VERTICAL,
                isLeftHanded = state.isLeftHanded,
                highlightSelectedString = false,
                modifier = Modifier
                    .fillMaxWidth(if (controlsVisible) 1f else 0.88f)
                    .fillMaxHeight()
                    .align(Alignment.Center)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
                    .size(width = 30.dp, height = 52.dp)
                    .clip(RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
                    .background(Color(0xCC12121A))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
                    .clickable { controlsVisible = !controlsVisible },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (controlsVisible) "‹" else "›",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            }
        }
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
