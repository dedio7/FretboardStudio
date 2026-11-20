package com.example.triadguitar.ui

import androidx.lifecycle.ViewModel
import com.example.triadguitar.model.DotLabelMode
import com.example.triadguitar.model.FretNote
import com.example.triadguitar.model.GuitarConstants
import com.example.triadguitar.model.GuitarTuning
import com.example.triadguitar.model.NoteNotation
import com.example.triadguitar.model.UiLanguage
import com.example.triadguitar.model.Pitch
import com.example.triadguitar.model.ScaleType
import com.example.triadguitar.model.TriadCalculator
import com.example.triadguitar.model.VisualTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TriadUiState(
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
                triadType = null,
                scaleType = current.selectedScale,
                tuning = current.selectedTuning,
                selectedBaseString = current.selectedBaseString,
                maxFret = GuitarConstants.TOTAL_FRETS
            )
            current.copy(allFretNotes = notes)
        }
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
        _uiState.update { it.copy(selectedBaseString = stringNumber) }
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
