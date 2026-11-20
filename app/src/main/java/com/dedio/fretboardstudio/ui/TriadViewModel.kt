package com.dedio.fretboardstudio.ui

import androidx.lifecycle.ViewModel
import com.dedio.fretboardstudio.model.ChordDictionaryRepository
import com.dedio.fretboardstudio.model.ChordTypeInfo
import com.dedio.fretboardstudio.model.DotLabelMode
import com.dedio.fretboardstudio.model.FretNote
import com.dedio.fretboardstudio.model.GuitarConstants
import com.dedio.fretboardstudio.model.GuitarTuning
import com.dedio.fretboardstudio.model.NoteNotation
import com.dedio.fretboardstudio.model.UiLanguage
import com.dedio.fretboardstudio.model.Pitch
import com.dedio.fretboardstudio.model.ScaleType
import com.dedio.fretboardstudio.model.TriadCalculator
import com.dedio.fretboardstudio.model.TriadInversion
import com.dedio.fretboardstudio.model.TriadType
import com.dedio.fretboardstudio.model.VisualTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class VisualizerMode { SCALES, TRIADS, DICTIONARY }

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
    val lastTappedNote: FretNote? = null
)

class TriadViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TriadUiState())
    val uiState: StateFlow<TriadUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    private fun recalculate() {
        _uiState.update { current ->
            val notes = TriadCalculator.calculateAllFretNotes(
                rootPitch = current.selectedRoot,
                triadType = current.selectedTriadType,
                scaleType = current.selectedScale,
                tuning = current.selectedTuning,
                selectedBaseString = current.selectedBaseString,
                maxFret = GuitarConstants.TOTAL_FRETS
            )
            current.copy(allFretNotes = notes)
        }
    }

    fun setVisualizerMode(mode: VisualizerMode) {
        _uiState.update { it.copy(visualizerMode = mode) }
    }

    fun selectTriadType(type: TriadType) {
        _uiState.update { it.copy(visualizerMode = VisualizerMode.TRIADS, selectedTriadType = type) }
        recalculate()
    }

    fun selectInversion(inversion: TriadInversion) {
        _uiState.update { it.copy(visualizerMode = VisualizerMode.TRIADS, selectedInversion = inversion) }
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
        _uiState.update { it.copy(selectedRoot = pitch) }
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

    fun toggleAllTargetDegrees() {
        _uiState.update { current ->
            val allDegrees = (1..current.selectedScale.degreeLabels.size).toSet()
            current.copy(
                selectedTargetDegrees = if (current.selectedTargetDegrees == allDegrees) emptySet() else allDegrees
            )
        }
    }

    fun selectTuning(tuning: GuitarTuning) {
        _uiState.update { it.copy(selectedTuning = tuning) }
        recalculate()
    }

    fun selectBaseString(stringNumber: Int) {
        if (stringNumber !in 1..6) return
        _uiState.update { it.copy(visualizerMode = VisualizerMode.TRIADS, selectedBaseString = stringNumber) }
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
    }

    fun setLanguage(language: UiLanguage) {
        _uiState.update { it.copy(language = language) }
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
}
