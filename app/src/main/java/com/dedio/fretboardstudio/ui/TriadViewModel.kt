package com.dedio.fretboardstudio.ui

import androidx.lifecycle.ViewModel
import com.dedio.fretboardstudio.model.ChordDictionaryRepository
import com.dedio.fretboardstudio.model.ChordPreset
import com.dedio.fretboardstudio.model.ChordSlot
import com.dedio.fretboardstudio.model.PresetRepository
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
        ),
    ),
    /** The chord currently being edited; also the one shown in the header and on the neck. */
    val selectedSlotId: String = DEFAULT_SLOT_ID,
    /** Which slot card is expanded, or null when the triad editor is collapsed. */
    val expandedSlotId: String? = DEFAULT_SLOT_ID,
    /** Show only the selected chord so a voicing can be studied in isolation. */
    val isolateSelectedSlot: Boolean = false,
    /** Draw a dedicated indicator on root/tonic notes. */
    val highlightTonic: Boolean = false,
    val isPresetsExpanded: Boolean = false,
    val editingPresetId: String? = null,
    val renameInputText: String = "",
    val savedPresets: List<ChordPreset> = PresetRepository.getPresets(),
)

/** Chords that actually contribute markers to the fretboard. */
val TriadUiState.visibleChordSlots: List<ChordSlot>
    get() = triadSequenceSlots.filter { it.isVisible }

/** The chord the header should describe: the selected visible one, or the first visible one. */
val TriadUiState.selectedChordSlot: ChordSlot?
    get() = triadSequenceSlots.firstOrNull { slot -> (slot.id == selectedSlotId) && slot.isVisible }
        ?: visibleChordSlots.firstOrNull()
        ?: triadSequenceSlots.firstOrNull { it.id == selectedSlotId }

class TriadViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TriadUiState())
    val uiState: StateFlow<TriadUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    /** Keeps the root grid, type/inversion selectors and header in step with a slot. */
    private fun TriadUiState.syncedTo(slot: ChordSlot?) = copy(
        selectedRoot = slot?.root ?: selectedRoot,
        selectedTriadType = slot?.quality ?: selectedTriadType,
        selectedInversion = slot?.inversion ?: selectedInversion,
        selectedBaseString = slot?.baseString ?: selectedBaseString
    )

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
                current.syncedTo(current.selectedChordSlot).copy(
                    visualizerMode = mode,
                    lastTappedNote = null
                )
            } else {
                current.copy(visualizerMode = mode, lastTappedNote = null)
            }
        }
        recalculate()
    }

    /** Applies a change to the chord the user is currently editing, not always slot 0. */
    private fun TriadUiState.updateSelectedSlot(transform: (ChordSlot) -> ChordSlot): List<ChordSlot> =
        triadSequenceSlots.map { if (it.id == selectedSlotId) transform(it) else it }

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
                lastTappedNote = null
            ).syncedTo(slot)
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
        _uiState.update { current ->
            if (!isolated) {
                current.copy(isolateSelectedSlot = false, lastTappedNote = null)
            } else {
                // Solo on a hidden chord would draw an empty neck. Move to a visible chord first.
                val selected = current.triadSequenceSlots.firstOrNull { it.id == current.selectedSlotId }
                if (selected != null && !selected.isVisible) {
                    val replacement = current.visibleChordSlots.firstOrNull()
                    if (replacement == null) {
                        current.copy(isolateSelectedSlot = false, lastTappedNote = null)
                    } else {
                        current.copy(
                            isolateSelectedSlot = true,
                            selectedSlotId = replacement.id,
                            expandedSlotId = replacement.id,
                            lastTappedNote = null
                        ).syncedTo(replacement)
                    }
                } else {
                    current.copy(isolateSelectedSlot = true, lastTappedNote = null)
                }
            }
        }
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
                expandedSlotId = newSlot.id,
                lastTappedNote = null
            ).syncedTo(newSlot)
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
            val neighbour = updated.getOrNull(removedIndex.coerceIn(0, updated.lastIndex)) ?: updated.first()
            val nextSelected = when {
                current.selectedSlotId != id -> updated.firstOrNull { it.id == current.selectedSlotId }
                current.isolateSelectedSlot && !neighbour.isVisible -> current.visibleChordSlots.firstOrNull()
                    ?: updated.firstOrNull { it.isVisible }
                else -> neighbour
            } ?: updated.first()
            val nextExpandedId = if (current.expandedSlotId == id) nextSelected.id else current.expandedSlotId
            current.copy(
                visualizerMode = VisualizerMode.TRIADS,
                triadSequenceSlots = updated,
                selectedSlotId = nextSelected.id,
                expandedSlotId = nextExpandedId,
                lastTappedNote = null
            ).syncedTo(nextSelected)
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
                lastTappedNote = null
            ).syncedTo(selected)
        }
        recalculate()
    }

    /** Visibility change: keeps the current mode and selection whenever that selection is still drawable. */
    fun toggleSlotVisibility(id: String) {
        _uiState.update { current ->
            val updated = current.triadSequenceSlots.map { slot ->
                if (slot.id == id) slot.copy(isVisible = !slot.isVisible) else slot
            }
            val selectedIsHidden = updated.firstOrNull { it.id == current.selectedSlotId }?.isVisible == false
            // Only a selected chord in Solo mode needs a visible replacement to avoid an empty neck.
            val replacement = if (selectedIsHidden && current.isolateSelectedSlot) {
                updated.firstOrNull { it.isVisible }
            } else {
                null
            }
            current.copy(
                triadSequenceSlots = updated,
                selectedSlotId = replacement?.id ?: current.selectedSlotId,
                expandedSlotId = if (current.expandedSlotId == current.selectedSlotId) {
                    replacement?.id ?: current.expandedSlotId
                } else {
                    current.expandedSlotId
                },
                isolateSelectedSlot = if (selectedIsHidden) replacement != null else current.isolateSelectedSlot,
                lastTappedNote = null
            ).syncedTo(replacement)
        }
        recalculate()
    }

    fun moveSlot(index: Int, direction: Int) {
        _uiState.update { current ->
            val list = current.triadSequenceSlots.toMutableList()
            val targetIndex = index + direction
            if (index !in list.indices || targetIndex !in list.indices) {
                return@update current
            }
            val item = list.removeAt(index)
            list.add(targetIndex, item)
            val reindexed = list.mapIndexed { idx, slot -> slot.copy(slotIndex = idx) }
            current.copy(
                visualizerMode = VisualizerMode.TRIADS,
                triadSequenceSlots = reindexed,
                lastTappedNote = null
            )
        }
        recalculate()
    }

    fun saveCurrentPreset(name: String = "Progressione ${_uiState.value.savedPresets.size + 1}") {
        val newPreset = ChordPreset(
            name = name,
            slots = _uiState.value.triadSequenceSlots.mapIndexed { idx, slot ->
                slot.copy(id = java.util.UUID.randomUUID().toString(), slotIndex = idx)
            }
        )
        PresetRepository.savePreset(newPreset)
        _uiState.update { current ->
            current.copy(savedPresets = PresetRepository.getPresets())
        }
        recalculate()
    }

    fun loadPreset(preset: ChordPreset) {
        _uiState.update { current ->
            val loadedSlots = preset.slots.mapIndexed { idx, slot ->
                slot.copy(id = java.util.UUID.randomUUID().toString(), slotIndex = idx)
            }
            current.copy(
                visualizerMode = VisualizerMode.TRIADS,
                triadSequenceSlots = loadedSlots,
                selectedSlotId = loadedSlots.firstOrNull()?.id ?: DEFAULT_SLOT_ID,
                expandedSlotId = loadedSlots.firstOrNull()?.id,
                lastTappedNote = null
            ).syncedTo(loadedSlots.firstOrNull())
        }
        recalculate()
    }

    fun startRenamingPreset(presetId: String, currentName: String) {
        _uiState.update { it.copy(editingPresetId = presetId, renameInputText = currentName) }
    }

    fun updateRenameInput(text: String) {
        _uiState.update { it.copy(renameInputText = text) }
    }

    fun finishRenamingPreset() {
        val editingId = _uiState.value.editingPresetId ?: return
        val newName = _uiState.value.renameInputText.trim()
        if (newName.isNotEmpty()) {
            PresetRepository.renamePreset(editingId, newName)
            _uiState.update { current ->
                current.copy(
                    savedPresets = PresetRepository.getPresets(),
                    editingPresetId = null,
                    renameInputText = ""
                )
            }
        } else {
            cancelRenamingPreset()
        }
    }

    fun cancelRenamingPreset() {
        _uiState.update { it.copy(editingPresetId = null, renameInputText = "") }
    }

    fun togglePresetsExpanded() {
        _uiState.update { it.copy(isPresetsExpanded = !it.isPresetsExpanded) }
    }

    fun updatePresetWithCurrentSequence(presetId: String) {
        val currentSlots = _uiState.value.triadSequenceSlots.mapIndexed { idx, slot ->
            slot.copy(id = java.util.UUID.randomUUID().toString(), slotIndex = idx)
        }
        PresetRepository.updatePreset(presetId, currentSlots)
        _uiState.update { current ->
            current.copy(savedPresets = PresetRepository.getPresets())
        }
    }

    fun deletePreset(presetId: String) {
        PresetRepository.deletePreset(presetId)
        _uiState.update { current ->
            current.copy(savedPresets = PresetRepository.getPresets())
        }
    }
}
