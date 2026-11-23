package com.dedio.fretboardstudio.ui

import androidx.lifecycle.ViewModel
import com.dedio.fretboardstudio.model.ChordDictionaryRepository
import com.dedio.fretboardstudio.model.ChordSlot
import com.dedio.fretboardstudio.model.ChordTypeInfo
import com.dedio.fretboardstudio.model.DotLabelMode
import com.dedio.fretboardstudio.model.FretNote
import com.dedio.fretboardstudio.model.GuitarConstants
import com.dedio.fretboardstudio.model.GuitarTuning
import com.dedio.fretboardstudio.model.NoteNotation
import com.dedio.fretboardstudio.model.Pitch
import com.dedio.fretboardstudio.model.ScaleType
import com.dedio.fretboardstudio.model.TriadCalculator
import com.dedio.fretboardstudio.model.TriadInversion
import com.dedio.fretboardstudio.model.TriadSequenceAggregator
import com.dedio.fretboardstudio.model.TriadType
import com.dedio.fretboardstudio.model.UiLanguage
import com.dedio.fretboardstudio.model.VisualTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class VisualizerMode { SCALES, TRIADS, DICTIONARY }

private const val DEFAULT_SLOT_ID = "triad-slot-1"

data class TriadUiState(
    val visualizerMode: VisualizerMode = VisualizerMode.SCALES,
    val selectedTriadType: TriadType = TriadType.MAJOR,
    val selectedInversion: TriadInversion = TriadInversion.ALL,
    val selectedDictionaryRoot: Pitch = Pitch(0),
    val selectedDictionaryType: ChordTypeInfo = ChordDictionaryRepository.ALL_TYPES.first(),
    val selectedDictionaryPosition: Int = 0,
    val selectedRoot: Pitch = Pitch(0),
    val selectedScale: ScaleType = ScaleType.MAJOR,
    val selectedPositions: Set<Int> = emptySet(),
    val selectedTargetDegrees: Set<Int> = emptySet(),
    val selectedTuning: GuitarTuning = GuitarTuning.STANDARD,
    val selectedBaseString: Int = 5,
    val notation: NoteNotation = NoteNotation.ENGLISH,
    val language: UiLanguage = UiLanguage.ITALIAN,
    val dotLabelMode: DotLabelMode = DotLabelMode.NOTES,
    val showTuningNotes: Boolean = false,
    val isLeftHanded: Boolean = false,
    val visualTheme: VisualTheme = VisualTheme.DEFAULT,
    val allFretNotes: List<FretNote> = emptyList(),
    val isSettingsDialogOpen: Boolean = false,
    val lastTappedNote: FretNote? = null,
    val triadSequenceSlots: List<ChordSlot> = listOf(
        ChordSlot(
            id = DEFAULT_SLOT_ID,
            slotIndex = 0,
            root = Pitch(0),
            quality = TriadType.MAJOR,
            inversion = TriadInversion.ALL,
            baseString = 5,
        )
    ),
    /** The chord currently being edited; also the one shown in the header and on the neck. */
    val selectedSlotId: String = DEFAULT_SLOT_ID,
    /** Which slot card is expanded, or null when the triad editor is collapsed. */
    val expandedSlotId: String? = DEFAULT_SLOT_ID,
    /** Show only the selected chord so a voicing can be studied in isolation. */
    val isolateSelectedSlot: Boolean = false,
    /** Draw a dedicated indicator on root/tonic notes. */
    val highlightTonic: Boolean = false
)

val TriadUiState.selectedChordSlot: ChordSlot?
    get() = triadSequenceSlots.firstOrNull { it.id == selectedSlotId } ?: triadSequenceSlots.firstOrNull()

class TriadViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TriadUiState())
    val uiState: StateFlow<TriadUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    private fun recalculate() {
        _uiState.update { current ->
            val notes = if (current.visualizerMode == VisualizerMode.TRIADS) {
                TriadSequenceAggregator.aggregate(
                    slots = current.triadSequenceSlots,
                    tuning = current.selectedTuning,
                    maxFret = GuitarConstants.TOTAL_FRETS,
                    notation = current.notation,
                    language = current.language,
                    isolatedSlotId = if (current.isolateSelectedSlot) current.selectedSlotId else null,
                    selectedSlotId = current.selectedSlotId
                )
            } else {
                TriadCalculator.calculateAllFretNotes(
                    rootPitch = current.selectedRoot,
                    triadType = current.selectedTriadType,
                    scaleType = current.selectedScale,
                    tuning = current.selectedTuning,
                    selectedBaseString = current.selectedBaseString,
                    maxFret = GuitarConstants.TOTAL_FRETS
                )
            }
            current.copy(allFretNotes = notes)
        }
    }

    fun setVisualizerMode(mode: VisualizerMode) {
        _uiState.update { current ->
            if (mode == VisualizerMode.TRIADS) {
                val slot = current.selectedChordSlot
                current.copy(
                    visualizerMode = mode,
                    selectedRoot = slot?.root ?: current.selectedRoot,
                    selectedTriadType = slot?.quality ?: current.selectedTriadType,
                    selectedInversion = slot?.inversion ?: current.selectedInversion,
                    selectedBaseString = slot?.baseString ?: current.selectedBaseString
                )
            } else {
                current.copy(visualizerMode = mode)
            }
        }
        recalculate()
    }

    /** Applies a change to the chord the user is currently editing, not always slot 0. */
    private fun TriadUiState.updateSelectedSlot(transform: (ChordSlot) -> ChordSlot): List<ChordSlot> =
        triadSequenceSlots.map { if (it.id == selectedSlotId) transform(it) else it }

    fun selectTriadType(type: TriadType) {
        _uiState.update { current ->
            current.copy(
                visualizerMode = VisualizerMode.TRIADS,
                selectedTriadType = type,
                triadSequenceSlots = current.updateSelectedSlot { it.copy(quality = type) }
            )
        }
        recalculate()
    }

    fun selectInversion(inversion: TriadInversion) {
        _uiState.update { current ->
            current.copy(
                visualizerMode = VisualizerMode.TRIADS,
                selectedInversion = inversion,
                triadSequenceSlots = current.updateSelectedSlot { it.copy(inversion = inversion) }
            )
        }
        recalculate()
    }

    fun selectDictionaryRoot(pitch: Pitch) {
        _uiState.update {
            it.copy(
                visualizerMode = VisualizerMode.DICTIONARY,
                selectedDictionaryRoot = pitch,
                selectedDictionaryPosition = 0
            )
        }
    }

    fun selectDictionaryType(type: ChordTypeInfo) {
        _uiState.update {
            it.copy(
                visualizerMode = VisualizerMode.DICTIONARY,
                selectedDictionaryType = type,
                selectedDictionaryPosition = 0
            )
        }
    }

    fun selectDictionaryPosition(position: Int) {
        if (position < 0) return
        _uiState.update { it.copy(visualizerMode = VisualizerMode.DICTIONARY, selectedDictionaryPosition = position) }
    }

    fun selectRoot(pitch: Pitch) {
        _uiState.update { current ->
            val slots = if (current.visualizerMode == VisualizerMode.TRIADS) {
                current.updateSelectedSlot { it.copy(root = pitch) }
            } else {
                current.triadSequenceSlots
            }
            current.copy(selectedRoot = pitch, triadSequenceSlots = slots)
        }
        recalculate()
    }

    fun selectScale(scale: ScaleType) {
        _uiState.update { current ->
            current.copy(
                selectedScale = scale,
                selectedTargetDegrees = current.selectedTargetDegrees.intersect(
                    (1..scale.degreeLabels.size).toSet()
                )
            )
        }
        recalculate()
    }

    fun togglePosition(position: Int) {
        if (position !in 1..5) return
        _uiState.update { current ->
            val nextPositions = if (position in current.selectedPositions) {
                current.selectedPositions - position
            } else {
                current.selectedPositions + position
            }
            current.copy(selectedPositions = nextPositions)
        }
    }

    fun clearPositionSelection() {
        _uiState.update { it.copy(selectedPositions = emptySet()) }
    }

    fun toggleTargetDegree(degree: Int) {
        if (degree !in 1.._uiState.value.selectedScale.degreeLabels.size) return
        _uiState.update { current ->
            val next = if (degree in current.selectedTargetDegrees) {
                current.selectedTargetDegrees - degree
            } else {
                current.selectedTargetDegrees + degree
            }
            current.copy(selectedTargetDegrees = next)
        }
    }

    fun selectTuning(tuning: GuitarTuning) {
        _uiState.update { it.copy(selectedTuning = tuning) }
        recalculate()
    }

    fun selectBaseString(stringNumber: Int) {
        if (stringNumber !in 1..6) return
        _uiState.update { current ->
            val slots = if (current.visualizerMode == VisualizerMode.TRIADS) {
                current.updateSelectedSlot { it.copy(baseString = stringNumber) }
            } else {
                current.triadSequenceSlots
            }
            current.copy(
                visualizerMode = VisualizerMode.TRIADS,
                selectedBaseString = stringNumber,
                triadSequenceSlots = slots
            )
        }
        recalculate()
    }

    fun setDotLabelMode(mode: DotLabelMode) {
        _uiState.update { it.copy(dotLabelMode = mode) }
    }

    fun setShowTuningNotes(show: Boolean) {
        _uiState.update { it.copy(showTuningNotes = show) }
    }

    fun setNotation(notation: NoteNotation) {
        _uiState.update { it.copy(notation = notation) }
        recalculate()
    }

    fun setLanguage(language: UiLanguage) {
        _uiState.update { it.copy(language = language) }
        recalculate()
    }

    fun setVisualTheme(theme: VisualTheme) {
        _uiState.update { it.copy(visualTheme = theme) }
    }

    fun onFretNoteTapped(note: FretNote) {
        _uiState.update { it.copy(lastTappedNote = note) }
    }

    fun openSettingsDialog(open: Boolean) {
        _uiState.update { it.copy(isSettingsDialogOpen = open) }
    }

    // Triad Sequence Management Methods (max 5 slots)
    /** Makes a slot the active chord and instantly syncs the header, root grid and fretboard to it. */
    fun selectSlot(id: String) {
        _uiState.update { current ->
            val slot = current.triadSequenceSlots.firstOrNull { it.id == id } ?: return@update current
            current.copy(
                visualizerMode = VisualizerMode.TRIADS,
                selectedSlotId = id,
                expandedSlotId = id,
                selectedRoot = slot.root,
                selectedTriadType = slot.quality,
                selectedInversion = slot.inversion,
                selectedBaseString = slot.baseString
            )
        }
        recalculate()
    }

    /** Collapses the open triad card without losing the current chord selection. */
    fun toggleSlotExpanded(id: String) {
        if (_uiState.value.expandedSlotId == id) {
            _uiState.update { it.copy(expandedSlotId = null) }
        } else {
            selectSlot(id)
        }
    }

    fun setSlotIsolation(isolated: Boolean) {
        _uiState.update { it.copy(isolateSelectedSlot = isolated) }
        recalculate()
    }

    fun setHighlightTonic(highlight: Boolean) {
        _uiState.update { it.copy(highlightTonic = highlight) }
    }

    fun addChordSlot() {
        _uiState.update { current ->
            if (current.triadSequenceSlots.size >= 5) return@update current
            val source = current.selectedChordSlot
            val newSlot = ChordSlot(
                slotIndex = current.triadSequenceSlots.size,
                root = source?.root ?: current.selectedRoot,
                quality = source?.quality ?: current.selectedTriadType,
                inversion = source?.inversion ?: current.selectedInversion,
                baseString = source?.baseString ?: current.selectedBaseString,
                isVisible = true
            )
            current.copy(
                visualizerMode = VisualizerMode.TRIADS,
                triadSequenceSlots = current.triadSequenceSlots + newSlot,
                selectedSlotId = newSlot.id,
                expandedSlotId = newSlot.id
            )
        }
        recalculate()
    }

    fun removeChordSlot(id: String) {
        _uiState.update { current ->
            if (current.triadSequenceSlots.size <= 1) return@update current
            val removedIndex = current.triadSequenceSlots.indexOfFirst { it.id == id }
            val updated = current.triadSequenceSlots.filter { it.id != id }.mapIndexed { idx, slot ->
                slot.copy(slotIndex = idx)
            }
            val nextId = if (current.selectedSlotId == id) {
                updated.getOrNull(removedIndex.coerceIn(0, updated.lastIndex))?.id ?: updated.first().id
            } else {
                current.selectedSlotId
            }
            val selected = updated.firstOrNull { it.id == nextId }
            val nextExpandedId = if (current.expandedSlotId == id) nextId else current.expandedSlotId
            current.copy(
                visualizerMode = VisualizerMode.TRIADS,
                triadSequenceSlots = updated,
                selectedSlotId = nextId,
                expandedSlotId = nextExpandedId,
                selectedRoot = selected?.root ?: current.selectedRoot,
                selectedTriadType = selected?.quality ?: current.selectedTriadType,
                selectedInversion = selected?.inversion ?: current.selectedInversion,
                selectedBaseString = selected?.baseString ?: current.selectedBaseString
            )
        }
        recalculate()
    }

    fun updateChordSlot(id: String, updateFn: (ChordSlot) -> ChordSlot) {
        _uiState.update { current ->
            val updated = current.triadSequenceSlots.map { slot ->
                if (slot.id == id) updateFn(slot) else slot
            }
            val selected = updated.firstOrNull { it.id == id }
            current.copy(
                visualizerMode = VisualizerMode.TRIADS,
                triadSequenceSlots = updated,
                selectedSlotId = id,
                expandedSlotId = id,
                selectedRoot = selected?.root ?: current.selectedRoot,
                selectedTriadType = selected?.quality ?: current.selectedTriadType,
                selectedInversion = selected?.inversion ?: current.selectedInversion,
                selectedBaseString = selected?.baseString ?: current.selectedBaseString
            )
        }
        recalculate()
    }

    /** Pure visibility change: must not disturb the selected chord or the current mode. */
    fun toggleSlotVisibility(id: String) {
        _uiState.update { current ->
            current.copy(
                triadSequenceSlots = current.triadSequenceSlots.map { slot ->
                    if (slot.id == id) slot.copy(isVisible = !slot.isVisible) else slot
                }
            )
        }
        recalculate()
    }

    fun moveSlot(index: Int, direction: Int) {
        _uiState.update { current ->
            val list = current.triadSequenceSlots.toMutableList()
            val targetIndex = index + direction
            if (targetIndex in list.indices) {
                val item = list.removeAt(index)
                list.add(targetIndex, item)
                val reindexed = list.mapIndexed { idx, slot -> slot.copy(slotIndex = idx) }
                current.copy(visualizerMode = VisualizerMode.TRIADS, triadSequenceSlots = reindexed)
            } else {
                current
            }
        }
        recalculate()
    }
}
