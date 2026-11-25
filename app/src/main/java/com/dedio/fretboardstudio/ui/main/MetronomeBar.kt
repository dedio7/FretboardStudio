package com.dedio.fretboardstudio.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dedio.fretboardstudio.audio.MetronomeMeter
import com.dedio.fretboardstudio.audio.MetronomeUiState
import com.dedio.fretboardstudio.audio.MetronomeViewModel
import com.dedio.fretboardstudio.model.UiLanguage
import com.dedio.fretboardstudio.model.UiStrings
import kotlin.math.roundToInt

/** Compact floating pill: play/stop, tempo and beat dots are always visible; controls expand on demand. */
@Composable
fun MetronomeBar(
    viewModel: MetronomeViewModel,
    language: UiLanguage,
    modifier: Modifier = Modifier,
) {
    val state: MetronomeUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = UiStrings(language)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 3.dp,
        modifier = modifier.widthIn(max = 320.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            if (state.isPlaying) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        )
                        .clickable { viewModel.togglePlay() }
                        .semantics(mergeDescendants = true) {
                            contentDescription = if (state.isPlaying) {
                                strings.stopMetronome
                            } else {
                                strings.playMetronome
                            }
                            role = Role.Button
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (state.isPlaying) "■" else "▶",
                        color = if (state.isPlaying) MaterialTheme.colorScheme.onTertiary
                        else MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Text(
                    text = state.bpm.toString(),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "BPM",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                )

                BeatDots(state)

                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.toggleExpanded() }
                        .semantics(mergeDescendants = true) {
                            contentDescription = if (state.isExpanded) {
                                strings.collapseMetronome
                            } else {
                                strings.expandMetronome
                            }
                            role = Role.Button
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (state.isExpanded) "⌃" else "⌄",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        fontSize = 16.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.setVisible(false) }
                        .semantics(mergeDescendants = true) {
                            contentDescription = strings.hideMetronome
                            role = Role.Button
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "✕",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        fontSize = 12.sp
                    )
                }
            }

            AnimatedVisibility(visible = state.isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = strings.metronome,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SmallChip(
                            text = "−",
                            selected = false,
                            description = strings.decreaseBpm,
                            modifier = Modifier.size(28.dp)
                        ) { viewModel.adjustBpm(-1) }
                        Slider(
                            value = state.bpm.toFloat(),
                            onValueChange = { viewModel.setBpm(it.roundToInt()) },
                            valueRange = MetronomeUiState.MIN_BPM.toFloat()..MetronomeUiState.MAX_BPM.toFloat(),
                            modifier = Modifier.weight(1f)
                        )
                        SmallChip(
                            text = "+",
                            selected = false,
                            description = strings.increaseBpm,
                            modifier = Modifier.size(28.dp)
                        ) { viewModel.adjustBpm(1) }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SmallChip(
                            text = strings.tapTempo,
                            selected = false,
                            description = strings.tapTempoDescription,
                            modifier = Modifier.weight(1f),
                        ) { viewModel.tapTempo() }
                        MetronomeMeter.entries.forEach { meter ->
                            SmallChip(
                                text = meter.label,
                                selected = state.meter == meter
                            ) { viewModel.setMeter(meter) }
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = strings.accent,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        SmallChip(
                            text = strings.accentOff,
                            selected = state.accentBeat == null,
                            description = strings.accentOffDescription,
                        ) { viewModel.setAccentBeat(null) }
                        (0 until state.meter.beatsPerBar).forEach { beat ->
                            SmallChip(
                                text = (beat + 1).toString(),
                                selected = state.accentBeat == beat,
                                description = strings.accentBeatDescription(beat + 1),
                            ) { viewModel.setAccentBeat(beat) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BeatDots(state: MetronomeUiState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.semantics {
            val beatText = if (state.isPlaying) {
                "${state.currentBeat + 1}/${state.meter.beatsPerBar}"
            } else {
                "–/${state.meter.beatsPerBar}"
            }
            contentDescription = "${state.meter.label} · $beatText"
        }
    ) {
        (0 until state.meter.beatsPerBar).forEach { beat ->
            val isLit = state.isPlaying && (beat == state.currentBeat)
            val color = when {
                isLit && (state.accentBeat == beat) -> MaterialTheme.colorScheme.tertiary
                isLit -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun SmallChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    description: String? = null,
    onClick: () -> Unit
) {
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                description?.let { contentDescription = it }
            },
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        fontSize = 10.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        maxLines = 1,
        softWrap = false
    )
}
