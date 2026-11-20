package com.dedio.fretboardstudio.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dedio.fretboardstudio.model.BadgeDisplayMode
import com.dedio.fretboardstudio.model.CagedShape
import com.dedio.fretboardstudio.model.DotLabelMode
import com.dedio.fretboardstudio.model.FretNote
import com.dedio.fretboardstudio.model.FretboardDisplayScope
import com.dedio.fretboardstudio.model.FretboardOrientation
import com.dedio.fretboardstudio.model.GuitarConstants
import com.dedio.fretboardstudio.model.GuitarTuning
import com.dedio.fretboardstudio.model.NoteNotation
import com.dedio.fretboardstudio.model.TriadCalculator
import com.dedio.fretboardstudio.model.TriadRole
import com.dedio.fretboardstudio.model.TriadType
import kotlin.math.sin
import kotlin.math.sqrt

val NeonPink = Color(0xFFFF2A6D)
val NeonGreen = Color(0xFF05FFA1)
val NeonCyan = Color(0xFF00F0FF)
val NeonGold = Color(0xFFFFD600)

@Composable
fun FretboardView(
    allNotes: List<FretNote>,
    notation: NoteNotation,
    triadType: TriadType,
    badgeDisplayMode: BadgeDisplayMode,
    dotLabelMode: DotLabelMode = DotLabelMode.NOTES,
    showChordTargets: Boolean = true,
    displayScope: FretboardDisplayScope,
    selectedBaseString: Int,
    showFingerHints: Boolean,
    lastTappedNote: FretNote?,
    onNoteClick: (FretNote) -> Unit,
    modifier: Modifier = Modifier,
    cagedShape: CagedShape = CagedShape.C_SHAPE,
    tuning: GuitarTuning = GuitarTuning.STANDARD,
    orientation: FretboardOrientation = FretboardOrientation.HORIZONTAL,
    isLeftHanded: Boolean = false,
    highlightSelectedString: Boolean = true
) {
    if (orientation == FretboardOrientation.VERTICAL) {
        FretboardVerticalView(
            allNotes = allNotes,
            notation = notation,
            triadType = triadType,
            badgeDisplayMode = badgeDisplayMode,
            dotLabelMode = dotLabelMode,
            showChordTargets = showChordTargets,
            displayScope = displayScope,
            selectedBaseString = selectedBaseString,
            showFingerHints = showFingerHints,
            lastTappedNote = lastTappedNote,
            onNoteClick = onNoteClick,
            modifier = modifier,
            cagedShape = cagedShape,
            tuning = tuning,
            isLeftHanded = isLeftHanded,
            highlightSelectedString = highlightSelectedString
        )
    } else {
        FretboardHorizontalView(
            allNotes = allNotes,
            notation = notation,
            triadType = triadType,
            badgeDisplayMode = badgeDisplayMode,
            dotLabelMode = dotLabelMode,
            showChordTargets = showChordTargets,
            displayScope = displayScope,
            selectedBaseString = selectedBaseString,
            showFingerHints = showFingerHints,
            lastTappedNote = lastTappedNote,
            onNoteClick = onNoteClick,
            modifier = modifier,
            cagedShape = cagedShape,
            tuning = tuning,
            isLeftHanded = isLeftHanded,
            highlightSelectedString = highlightSelectedString
        )
    }
}

@Composable
private fun FretboardHorizontalView(
    allNotes: List<FretNote>,
    notation: NoteNotation,
    triadType: TriadType,
    badgeDisplayMode: BadgeDisplayMode,
    dotLabelMode: DotLabelMode = DotLabelMode.NOTES,
    showChordTargets: Boolean = true,
    displayScope: FretboardDisplayScope,
    selectedBaseString: Int,
    showFingerHints: Boolean,
    lastTappedNote: FretNote?,
    onNoteClick: (FretNote) -> Unit,
    modifier: Modifier = Modifier,
    cagedShape: CagedShape = CagedShape.C_SHAPE,
    tuning: GuitarTuning = GuitarTuning.STANDARD,
    isLeftHanded: Boolean = false,
    highlightSelectedString: Boolean = true
) {
    val scrollState = rememberScrollState()
    val zoom = rememberSaveable { mutableFloatStateOf(1f) }

    val fretWidth: Dp = 62.dp * zoom.floatValue
    val nutWidth: Dp = 26.dp * zoom.floatValue
    val stringSpacing: Dp = 32.dp * zoom.floatValue
    val headerTopPadding: Dp = 24.dp
    val totalHeight: Dp = headerTopPadding + (stringSpacing * 5) + 32.dp

    val fingerHintsMap = remember(allNotes) {
        calculateFingerHints(allNotes.filter { it.isInSelectedVoicing && it.fret > 0 })
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .pinchToZoom(zoom)
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.cardBorder(),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.cardBorder()
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            ),
        color = MaterialTheme.colorScheme.fretboardBg(),
        shadowElevation = 14.dp
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .padding(vertical = 8.dp, horizontal = 10.dp)
        ) {
            NeonStringHeaders(
                stringSpacing = stringSpacing,
                topPadding = headerTopPadding,
                notation = notation,
                selectedBaseString = selectedBaseString,
                cagedShape = cagedShape,
                tuning = tuning,
                isLeftHanded = isLeftHanded,
                highlightSelectedString = highlightSelectedString
            )

            Box(
                modifier = Modifier
                    .height(totalHeight)
                    .width(nutWidth + (fretWidth * GuitarConstants.TOTAL_FRETS))
            ) {
                NeonNeckCanvas(
                    totalFrets = GuitarConstants.TOTAL_FRETS,
                    fretWidth = fretWidth,
                    nutWidth = nutWidth,
                    stringSpacing = stringSpacing,
                    startY = headerTopPadding,
                    selectedBaseString = selectedBaseString,
                    lastTappedNote = lastTappedNote,
                    highlightSelectedString = highlightSelectedString
                )

                NeonFretNumberRow(
                    totalFrets = GuitarConstants.TOTAL_FRETS,
                    fretWidth = fretWidth,
                    nutWidth = nutWidth
                )

                val notesByPosition = remember(allNotes) {
                    allNotes.associateBy { it.stringNumber to it.fret }
                }

                val currentStringSet = remember(selectedBaseString) {
                    TriadCalculator.getStringSetForBaseString(selectedBaseString)
                }

                for (stringNum in 1..6) {
                    val isSetString = currentStringSet.contains(stringNum)
                    for (fret in 0..GuitarConstants.TOTAL_FRETS) {
                        val note = notesByPosition[stringNum to fret]
                        if (note != null) {
                            val role = note.triadRole
                            val isTriadNote = role != null
                            val isScaleNote = note.isInScale

                            val shouldShow = when (displayScope) {
                                FretboardDisplayScope.FULL_NECK -> isScaleNote
                                FretboardDisplayScope.SELECTED_VOICING ->
                                    isSetString && isTriadNote &&
                                        (note.isInSelectedVoicing || note.isHighlightedRoot)
                            }

                            val xOffset = if (fret == 0) {
                                nutWidth / 2 - 16.dp
                            } else {
                                nutWidth + fretWidth * (fret - 1) + (fretWidth / 2) - 17.dp
                            }

                            val displayStringIndex = if (isLeftHanded) 6 - stringNum else stringNum - 1
                            val yOffset = headerTopPadding + stringSpacing * displayStringIndex - 17.dp

                            if (shouldShow) {
                                val isHighlighted = role == TriadRole.ROOT || note.isInSelectedVoicing || note.isHighlightedRoot || note.isTargetNote
                                val isTapped = lastTappedNote?.stringNumber == note.stringNumber &&
                                        lastTappedNote.fret == note.fret
                                val fingerNumber = if (showFingerHints && isHighlighted) fingerHintsMap[note.stringNumber to note.fret] else null
                                val noteAlpha = if (displayScope == FretboardDisplayScope.FULL_NECK || isSetString || note.isInSelectedVoicing) 1.0f else 0.35f

                                if (displayScope == FretboardDisplayScope.FULL_NECK) {
                                    ScaleNoteBadge(
                                        note = note,
                                        notation = notation,
                                        displayMode = badgeDisplayMode,
                                        dotLabelMode = dotLabelMode,
                                        isChordTarget = note.isTargetNote || (showChordTargets && isTriadNote),
                                        onClick = { onNoteClick(note) },
                                        modifier = Modifier.offset(x = xOffset, y = yOffset)
                                    )
                                } else if (role != null) {
                                    NeonGlowNoteBadge(
                                        note = note,
                                        role = role,
                                        triadType = triadType,
                                        notation = notation,
                                        displayMode = badgeDisplayMode,
                                        dotLabelMode = dotLabelMode,
                                        showChordTargets = showChordTargets,
                                        isTargetNote = note.isTargetNote,
                                        isHighlighted = isHighlighted,
                                        isTapped = isTapped,
                                        fingerHint = fingerNumber,
                                        alpha = noteAlpha,
                                        onClick = { onNoteClick(note) },
                                        modifier = Modifier.offset(x = xOffset, y = yOffset)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .offset(x = xOffset, y = yOffset)
                                        .size(34.dp)
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            onNoteClick(note)
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FretboardVerticalView(
    allNotes: List<FretNote>,
    notation: NoteNotation,
    triadType: TriadType,
    badgeDisplayMode: BadgeDisplayMode,
    dotLabelMode: DotLabelMode = DotLabelMode.NOTES,
    showChordTargets: Boolean = true,
    displayScope: FretboardDisplayScope,
    selectedBaseString: Int,
    showFingerHints: Boolean,
    lastTappedNote: FretNote?,
    onNoteClick: (FretNote) -> Unit,
    modifier: Modifier = Modifier,
    cagedShape: CagedShape = CagedShape.C_SHAPE,
    tuning: GuitarTuning = GuitarTuning.STANDARD,
    isLeftHanded: Boolean = false,
    highlightSelectedString: Boolean = true
) {
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    val zoom = rememberSaveable { mutableFloatStateOf(1f) }

    val fretHeight: Dp = 56.dp * zoom.floatValue
    val nutHeight: Dp = 24.dp * zoom.floatValue
    val stringWidth: Dp = 25.dp * zoom.floatValue
    val leftHeaderWidth: Dp = 22.dp * zoom.floatValue
    val boardColor = MaterialTheme.colorScheme.surfaceVariant
    val grainColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.09f)

    val fingerHintsMap = remember(allNotes) {
        calculateFingerHints(allNotes.filter { it.isInSelectedVoicing && it.fret > 0 })
    }

    val orderedStringNumbers = if (isLeftHanded) (1..6).toList() else (6 downTo 1).toList()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .pinchToZoom(zoom)
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.75f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            ),
        color = boardColor,
        shadowElevation = 14.dp
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val contentWidth = maxWidth.coerceAtLeast(leftHeaderWidth + stringWidth * 6 + 24.dp)
            Row(Modifier.horizontalScroll(horizontalScrollState)) {
                Column(
            modifier = Modifier
                .width(contentWidth)
                .verticalScroll(verticalScrollState)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            boardColor,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            boardColor,
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f),
                            boardColor
                        )
                    )
                )
                .drawBehind {
                    for (grain in 0..24) {
                        val progress = grain / 24f
                        val x = size.width * progress
                        val wave = sin(grain * 1.37f) * 2.5.dp.toPx()
                        drawLine(
                            color = grainColor,
                            start = Offset(x, 0f),
                            end = Offset(x + wave, size.height),
                            strokeWidth = 0.7.dp.toPx()
                        )
                    }
                }
                .padding(vertical = 10.dp, horizontal = 12.dp)
        ) {
            // String Headers Top Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = leftHeaderWidth),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val stringInfoList = tuning.getStringInfoList()
                orderedStringNumbers.forEach { strNum ->
                    val strInfo = stringInfoList.first { it.stringNumber == strNum }
                    val isSelected = highlightSelectedString && strNum == selectedBaseString
                    Box(
                        modifier = Modifier
                            .width(stringWidth)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Color(0xFF694329) else Color(0xFF4A2D1D))
                            .border(1.dp, if (isSelected) Color(0xFFFFC832) else Color(0xFF9A7652), RoundedCornerShape(6.dp))
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strInfo.openPitch.getDisplayName(notation),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) Color(0xFFFFD567) else Color(0xFFE2C5A0),
                                fontSize = 8.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val notesByPosition = remember(allNotes) {
                allNotes.associateBy { it.stringNumber to it.fret }
            }

            val currentStringSet = remember(selectedBaseString) {
                TriadCalculator.getStringSetForBaseString(selectedBaseString)
            }

            // Vertical fretboard, open strings through fret 22
            for (fret in 0..GuitarConstants.TOTAL_FRETS) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (fret == 0) nutHeight else fretHeight)
                        .background(if (fret == 0) Color(0xFF372317) else Color.Transparent)
                        .drawBehind {
                            val nutLabelWidth = leftHeaderWidth.toPx()
                            val neckCenterX = nutLabelWidth + (size.width - nutLabelWidth) / 2f
                            if (fret in GuitarConstants.SINGLE_DOT_FRETS) {
                                drawCircle(
                                    color = Color(0xFFD6B27A).copy(alpha = 0.45f),
                                    radius = 4.dp.toPx(),
                                    center = Offset(neckCenterX, size.height / 2f)
                                )
                            } else if (fret == 12) {
                                val markerGap = 18.dp.toPx()
                                listOf(-markerGap, markerGap).forEach { offset ->
                                    drawCircle(
                                        color = Color(0xFFD6B27A).copy(alpha = 0.45f),
                                        radius = 4.dp.toPx(),
                                        center = Offset(neckCenterX + offset, size.height / 2f)
                                    )
                                }
                            }
                            drawLine(
                                color = Color(0xFFD0B28D).copy(alpha = 0.72f),
                                start = Offset(0f, size.height - 1.dp.toPx()),
                                end = Offset(size.width, size.height - 1.dp.toPx()),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        .border(
                            width = if (fret == 0) 2.dp else 1.dp,
                            color = Color(0xFFB9926B).copy(alpha = if (fret == 0) 0.95f else 0.68f)
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Fret Number Label Column
                    Box(
                        modifier = Modifier
                            .width(leftHeaderWidth)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        val isMarkerFret = fret in listOf(3, 5, 7, 9, 12, 15)
                        Text(
                            text = "$fret",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isMarkerFret) Color(0xFFFFD567) else Color(0xFFD0B28D),
                                fontWeight = if (isMarkerFret) FontWeight.Black else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        )
                    }

                    // Strings Row
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        orderedStringNumbers.forEach { stringNum ->
                            val isSetString = currentStringSet.contains(stringNum)
                            val note = notesByPosition[stringNum to fret]

                            Box(
                                modifier = Modifier
                                    .width(stringWidth)
                                    .fillMaxHeight()
                                    .drawBehind {
                                        val centerX = size.width / 2f
                                        val stringIndex = orderedStringNumbers.indexOf(stringNum)
                                        val thickness = (0.7f + stringIndex * 0.18f).dp.toPx()
                                        drawLine(
                                            color = Color(0xFF2C1A12).copy(alpha = 0.95f),
                                            start = Offset(centerX + 1.dp.toPx(), 0f),
                                            end = Offset(centerX + 1.dp.toPx(), size.height),
                                            strokeWidth = thickness + 1.dp.toPx()
                                        )
                                        drawLine(
                                            color = Color(0xFFD6C4A6).copy(alpha = 0.78f),
                                            start = Offset(centerX, 0f),
                                            end = Offset(centerX, size.height),
                                            strokeWidth = thickness
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (note != null) {
                                    val role = note.triadRole
                                    val isTriadNote = role != null
                                    val isScaleNote = note.isInScale

                                    val shouldShow = when (displayScope) {
                                        FretboardDisplayScope.FULL_NECK -> isScaleNote
                                        FretboardDisplayScope.SELECTED_VOICING ->
                                            isSetString && isTriadNote &&
                                                (note.isInSelectedVoicing || note.isHighlightedRoot)
                                    }

                                    if (shouldShow) {
                                        val isHighlighted = role == TriadRole.ROOT || note.isInSelectedVoicing || note.isHighlightedRoot || note.isTargetNote
                                        val isTapped = lastTappedNote?.stringNumber == note.stringNumber &&
                                                lastTappedNote.fret == note.fret
                                        val fingerNumber = if (showFingerHints && isHighlighted) fingerHintsMap[note.stringNumber to note.fret] else null
                                        val noteAlpha = if (displayScope == FretboardDisplayScope.FULL_NECK || isSetString || note.isInSelectedVoicing) 1.0f else 0.35f

                                        if (displayScope == FretboardDisplayScope.FULL_NECK) {
                                            ScaleNoteBadge(
                                                note = note,
                                                notation = notation,
                                                displayMode = badgeDisplayMode,
                                                dotLabelMode = dotLabelMode,
                                                isChordTarget = note.isTargetNote || (showChordTargets && isTriadNote),
                                                onClick = { onNoteClick(note) }
                                            )
                                        } else if (role != null) {
                                            NeonGlowNoteBadge(
                                                note = note,
                                                role = role,
                                                triadType = triadType,
                                                notation = notation,
                                                displayMode = badgeDisplayMode,
                                                dotLabelMode = dotLabelMode,
                                                showChordTargets = showChordTargets,
                                                isTargetNote = note.isTargetNote,
                                                isHighlighted = isHighlighted,
                                                isTapped = isTapped,
                                                fingerHint = fingerNumber,
                                                alpha = noteAlpha,
                                                onClick = { onNoteClick(note) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
                }
            }
        }
    }
}

private fun calculateFingerHints(voicingNotes: List<FretNote>): Map<Pair<Int, Int>, Int> {
    if (voicingNotes.isEmpty()) return emptyMap()

    val sortedByFret = voicingNotes.sortedBy { it.fret }
    val minFret = sortedByFret.first().fret
    val map = mutableMapOf<Pair<Int, Int>, Int>()

    for (note in sortedByFret) {
        val relDiff = note.fret - minFret
        val finger = when (relDiff) {
            0 -> 1
            1 -> 2
            2 -> 3
            else -> 4
        }
        map[note.stringNumber to note.fret] = finger
    }
    return map
}

@Composable
private fun ScaleNoteBadge(
    note: FretNote,
    notation: NoteNotation,
    displayMode: BadgeDisplayMode,
    dotLabelMode: DotLabelMode = DotLabelMode.NOTES,
    isChordTarget: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val noteName = note.pitch.getDisplayName(notation)
    val degreeLabel = note.scaleDegreeLabel ?: ""

    val isLightTheme = MaterialTheme.colorScheme.background.luminance() > 0.5f
    val ringColor = when {
        isChordTarget -> NeonGold
        isLightTheme -> Color(0xFF67E8C5)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
    }
    val bgColor = when {
        isChordTarget -> Color(0xFF2A2200)
        isLightTheme -> Color(0xFF30213D)
        else -> Color(0xFF08252B)
    }
    val textColor = when {
        isChordTarget -> NeonGold
        isLightTheme -> Color(0xFFFFF3D6)
        else -> MaterialTheme.colorScheme.primary
    }
    val degColor = if (isChordTarget) NeonGold.copy(alpha = 0.8f) else ringColor.copy(alpha = 0.9f)

    val primaryLabel: String? = when (dotLabelMode) {
        DotLabelMode.NOTES     -> noteName
        DotLabelMode.INTERVALS -> degreeLabel
        DotLabelMode.CLEAN     -> null
    }

    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(if (isChordTarget) 1.5.dp else 1.dp, ringColor, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (primaryLabel != null) {
            Text(
                text = primaryLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (dotLabelMode == DotLabelMode.INTERVALS) degColor else textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (primaryLabel.length > 2) 7.sp else 9.sp,
                    lineHeight = 9.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NeonStringHeaders(
    stringSpacing: Dp,
    topPadding: Dp,
    notation: NoteNotation,
    selectedBaseString: Int,
    cagedShape: CagedShape,
    tuning: GuitarTuning,
    isLeftHanded: Boolean = false,
    highlightSelectedString: Boolean = true
) {
    Column(
        modifier = Modifier
            .padding(top = topPadding - 18.dp, end = 6.dp)
            .width(62.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 4.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(cagedShape.colorHex).copy(alpha = 0.25f))
                .border(1.dp, Color(cagedShape.colorHex), RoundedCornerShape(6.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${cagedShape.letter}-SHAPE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = Color(cagedShape.colorHex),
                    fontSize = 9.sp
                )
            )
        }

        val strings = tuning.getStringInfoList()
        val orderedStrings = if (isLeftHanded) strings.reversed() else strings
        for (strInfo in orderedStrings) {
            val isBaseString = highlightSelectedString && strInfo.stringNumber == selectedBaseString
            val noteName = strInfo.openPitch.getDisplayName(notation)

            Box(
                modifier = Modifier
                    .height(stringSpacing)
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${strInfo.stringNumber}ª",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isBaseString) FontWeight.Black else FontWeight.Normal,
                            color = if (isBaseString) MaterialTheme.colorScheme.primary else Color(0xFF6B7280),
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isBaseString) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else Color(0xFF141724)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isBaseString) MaterialTheme.colorScheme.primary else Color(0xFF262C44),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = noteName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = if (isBaseString) MaterialTheme.colorScheme.primary else Color(0xFFB0B8C8),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NeonFretNumberRow(
    totalFrets: Int,
    fretWidth: Dp,
    nutWidth: Dp
) {
    Row(
        modifier = Modifier
            .height(22.dp)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.width(nutWidth),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "0",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            )
        }

        for (f in 1..totalFrets) {
            val isMarkerFret = f in listOf(3, 5, 7, 9, 12, 15)
            Box(
                modifier = Modifier.width(fretWidth),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$f",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isMarkerFret) Color(0xFFFFD567) else Color(0xFFD0B28D),
                        fontWeight = if (isMarkerFret) FontWeight.Black else FontWeight.Normal,
                        fontSize = if (isMarkerFret) 11.sp else 10.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun NeonNeckCanvas(
    totalFrets: Int,
    fretWidth: Dp,
    nutWidth: Dp,
    stringSpacing: Dp,
    startY: Dp,
    selectedBaseString: Int,
    lastTappedNote: FretNote?,
    highlightSelectedString: Boolean
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        val nutPx = nutWidth.toPx()
        val fretPx = fretWidth.toPx()
        val spacingPx = stringSpacing.toPx()
        val startYPx = startY.toPx()

        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF1E2235), primaryColor.copy(alpha = 0.8f), Color(0xFF1E2235)),
                startX = 0f,
                endX = nutPx
            ),
            topLeft = Offset(0f, startYPx - 8f),
            size = Size(nutPx, spacingPx * 5 + 16f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )

        val middleY = startYPx + spacingPx * 2.5f
        for (f in 1..totalFrets) {
            val fretCenterX = nutPx + (f - 1) * fretPx + fretPx / 2f

            if (f in GuitarConstants.SINGLE_DOT_FRETS) {
                drawCircle(
                    color = primaryColor.copy(alpha = 0.15f),
                    radius = 8.dp.toPx(),
                    center = Offset(fretCenterX, middleY)
                )
                drawCircle(
                    color = primaryColor.copy(alpha = 0.6f),
                    radius = 3.dp.toPx(),
                    center = Offset(fretCenterX, middleY)
                )
            } else if (f in GuitarConstants.DOUBLE_DOT_FRETS) {
                val dotOffsetY = spacingPx * 0.85f
                for (offsetSign in listOf(-1f, 1f)) {
                    val dotY = middleY + (offsetSign * dotOffsetY)
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.18f),
                        radius = 7.dp.toPx(),
                        center = Offset(fretCenterX, dotY)
                    )
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.7f),
                        radius = 3.dp.toPx(),
                        center = Offset(fretCenterX, dotY)
                    )
                }
            }
        }

        for (f in 1..totalFrets) {
            val fretX = nutPx + f * fretPx
            drawLine(
                color = Color(0xFF1F2436),
                start = Offset(fretX, startYPx - 6f),
                end = Offset(fretX, startYPx + spacingPx * 5 + 6f),
                strokeWidth = 2f
            )
        }

        for (stringIdx in 0..5) {
            val stringNum = stringIdx + 1
            val y = startYPx + stringIdx * spacingPx
            val isSelectedString = highlightSelectedString && stringNum == selectedBaseString
            val isTappedString = lastTappedNote?.stringNumber == stringNum

            if (isTappedString) {
                drawLine(
                    color = primaryColor.copy(alpha = 0.6f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 8f
                )
                drawLine(
                    color = Color.White,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 3f
                )
            } else if (isSelectedString) {
                drawLine(
                    color = primaryColor.copy(alpha = 0.35f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 6f
                )
                drawLine(
                    color = primaryColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 2.2f
                )
            } else {
                val stringColor = Color(0xFF4A526E)
                val thickness = 1.2f + (stringIdx * 0.4f)
                drawLine(
                    color = stringColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = thickness
                )
            }
        }
    }
}

@Composable
private fun NeonGlowNoteBadge(
    note: FretNote,
    role: TriadRole,
    triadType: TriadType,
    notation: NoteNotation,
    displayMode: BadgeDisplayMode,
    dotLabelMode: DotLabelMode = DotLabelMode.NOTES,
    showChordTargets: Boolean = true,
    isTargetNote: Boolean = false,
    isHighlighted: Boolean,
    isTapped: Boolean,
    fingerHint: Int?,
    alpha: Float = 1.0f,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scaleAnim by animateFloatAsState(
        targetValue = if (isTapped) 1.3f else 1.0f,
        animationSpec = spring(dampingRatio = 0.35f, stiffness = 700f),
        label = "neon_bounce"
    )

    val neonColor = MaterialTheme.colorScheme.primary

    // "Target chord tones" feature: highlighted chord tones glow gold
    val isGoldTarget = isTargetNote || showChordTargets
    val effectiveColor = if (isGoldTarget) NeonGold else neonColor
    val borderColor = if (isGoldTarget) NeonGold else neonColor

    val noteName = note.pitch.getDisplayName(notation)
    val degreeLabel = role.getDegreeLabel(triadType)
    val badgeSize = if (isHighlighted) 36.dp else 30.dp

    // Label respects DotLabelMode
    val primaryLabel: String? = when (dotLabelMode) {
        DotLabelMode.NOTES     -> noteName
        DotLabelMode.INTERVALS -> degreeLabel
        DotLabelMode.CLEAN     -> null
    }

    Box(
        modifier = modifier
            .scale(scaleAnim)
            .graphicsLayer(alpha = alpha)
            .size(badgeSize)
            .shadow(
                elevation = if (isHighlighted) 12.dp else 4.dp,
                shape = CircleShape,
                spotColor = effectiveColor,
                ambientColor = effectiveColor
            )
            .clip(CircleShape)
            .background(Color(0xFF0F121C))
            .border(
                width = if (note.isHighlightedRoot) 2.5.dp else if (isHighlighted) 2.dp else 1.2.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(effectiveColor.copy(alpha = 0.4f), Color.Transparent),
                    radius = size.width / 2f
                )
            )
        }

        if (primaryLabel != null) {
            Text(
                text = primaryLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (dotLabelMode == DotLabelMode.INTERVALS) effectiveColor else Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = if (primaryLabel.length > 2) 9.sp else 11.sp,
                    lineHeight = 11.sp
                ),
                textAlign = TextAlign.Center
            )
        }

        if (fingerHint != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(13.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$fingerHint",
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 8.sp
                )
            }
        }
    }
}

private fun Modifier.pinchToZoom(zoom: MutableFloatState): Modifier = pointerInput(zoom) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        var previousSpan = 0f
        do {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val pressedChanges = event.changes.filter { it.pressed }
            if (pressedChanges.size >= 2) {
                val centroidX = pressedChanges.sumOf { it.position.x.toDouble() } / pressedChanges.size
                val centroidY = pressedChanges.sumOf { it.position.y.toDouble() } / pressedChanges.size
                val centroid = Offset(centroidX.toFloat(), centroidY.toFloat())
                val span = pressedChanges.sumOf { (it.position - centroid).getDistance().toDouble() }
                    .div(pressedChanges.size)
                    .toFloat()
                if (previousSpan > 0f && span > 0f) {
                    zoom.floatValue = (zoom.floatValue * (span / previousSpan)).coerceIn(1f, 2.5f)
                    pressedChanges.forEach { it.consume() }
                }
                previousSpan = span
            } else {
                previousSpan = 0f
            }
        } while (event.changes.any { it.pressed })
    }
}

@Composable
private fun androidx.compose.material3.ColorScheme.cardBorder(): Color {
    return this.primary.copy(alpha = 0.3f)
}

@Composable
private fun androidx.compose.material3.ColorScheme.fretboardBg(): Color {
    return this.background
}
