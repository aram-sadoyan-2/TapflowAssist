package com.algorithm.tapflow.assist.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.algorithm.tapflow.assist.data.model.GestureType
import com.algorithm.tapflow.assist.data.model.TouchPoint
import com.algorithm.tapflow.assist.data.model.TouchPreset
import com.algorithm.tapflow.assist.data.repository.PresetRepository
import com.algorithm.tapflow.assist.ui.state.EditorUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditorViewModel(
    private val repository: PresetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    fun loadPreset(presetId: Long) {
        if (presetId <= 0L) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val preset = repository.getPresetById(presetId)

            if (preset != null) {
                _uiState.value = EditorUiState(
                    presetId = preset.id,
                    name = preset.name,
                    type = preset.type,
                    intervalMs = preset.intervalMs.toString(),
                    repeatCount = preset.repeatCount.toString(),
                    holdDurationMs = preset.holdDurationMs.toString(),
                    points = preset.points,
                    selectedPointId = preset.points.firstOrNull()?.id,
                    isLoading = false,
                    isSaving = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun updateName(value: String) {
        _uiState.update { it.copy(name = value, errorMessage = null) }
    }

    fun updateInterval(value: String) {
        _uiState.update {
            it.copy(
                intervalMs = value.filter(Char::isDigit),
                errorMessage = null
            )
        }
    }

    fun updateRepeat(value: String) {
        _uiState.update {
            it.copy(
                repeatCount = value.filter(Char::isDigit),
                errorMessage = null
            )
        }
    }

    fun updateHold(value: String) {
        _uiState.update {
            it.copy(
                holdDurationMs = value.filter(Char::isDigit),
                errorMessage = null
            )
        }
    }

    fun updateType(type: GestureType) {
        _uiState.update { current ->
            val adjustedPoints = enforcePointLimit(type, current.points)
            current.copy(
                type = type,
                points = adjustedPoints,
                selectedPointId = adjustedPoints.firstOrNull()?.id,
                errorMessage = null
            )
        }
    }

    fun selectPoint(pointId: Int?) {
        _uiState.update { it.copy(selectedPointId = pointId) }
    }

    fun clearAllPoints() {
        _uiState.update {
            it.copy(
                points = emptyList(),
                selectedPointId = null,
                errorMessage = null
            )
        }
    }

    fun replaceAllPoints(newPoints: List<TouchPoint>) {
        _uiState.update { current ->
            val adjustedPoints = enforcePointLimit(current.type, newPoints)
            current.copy(
                points = adjustedPoints,
                selectedPointId = adjustedPoints.firstOrNull()?.id,
                errorMessage = null
            )
        }
    }

    fun addPointAt(x: Float, y: Float) {
        val state = _uiState.value
        val nextId = (state.points.maxOfOrNull { it.id } ?: 0) + 1

        val newPoint = TouchPoint(
            id = nextId,
            x = x,
            y = y,
            delayBeforeMs = 0L
        )

        val updatedPoints = enforcePointLimit(
            state.type,
            state.points + newPoint
        )

        _uiState.value = state.copy(
            points = updatedPoints,
            selectedPointId = updatedPoints.lastOrNull()?.id,
            errorMessage = null
        )
    }

    fun savePreset(onSaved: (Long) -> Unit = {}) {
        val state = _uiState.value
        val validationError = validatePoints(state.type, state.points)

        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        val preset = TouchPreset(
            id = state.presetId,
            name = state.name.ifBlank { "New Preset" },
            type = state.type,
            intervalMs = state.intervalMs.toLongOrNull() ?: 1000L,
            repeatCount = state.repeatCount.toIntOrNull() ?: 1,
            holdDurationMs = state.holdDurationMs.toLongOrNull() ?: 0L,
            points = state.points
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                errorMessage = null
            )

            val savedId = repository.savePreset(preset)

            _uiState.value = _uiState.value.copy(
                presetId = savedId,
                isSaving = false
            )

            onSaved(savedId)
        }
    }

    private fun enforcePointLimit(
        type: GestureType,
        points: List<TouchPoint>
    ): List<TouchPoint> {
        return when (type) {
            GestureType.SINGLE_TAP,
            GestureType.LONG_PRESS -> points.take(1)

            GestureType.SWIPE -> points.take(2)

            GestureType.MULTI_TAP -> points
        }
    }

    private fun validatePoints(
        type: GestureType,
        points: List<TouchPoint>
    ): String? {
        return when (type) {
            GestureType.SINGLE_TAP ->
                if (points.size != 1) "Single tap requires exactly 1 point." else null

            GestureType.LONG_PRESS ->
                if (points.size != 1) "Long press requires exactly 1 point." else null

            GestureType.SWIPE ->
                if (points.size != 2) "Swipe requires exactly 2 points." else null

            GestureType.MULTI_TAP ->
                if (points.isEmpty()) "Multi tap requires at least 1 point." else null
        }
    }

    companion object {
        fun factory(repository: PresetRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EditorViewModel(repository) as T
                }
            }
        }
    }
}