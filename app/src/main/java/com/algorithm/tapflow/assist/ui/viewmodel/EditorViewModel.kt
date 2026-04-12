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
import kotlinx.coroutines.launch

class EditorViewModel(
    private val repository: PresetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        EditorUiState(
            points = listOf(
                TouchPoint(id = 1, x = 220f, y = 420f),
                TouchPoint(id = 2, x = 400f, y = 680f, delayBeforeMs = 300)
            )
        )
    )
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
                    isLoading = false,
                    isSaving = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun updateName(value: String) {
        _uiState.value = _uiState.value.copy(name = value)
    }

    fun updateInterval(value: String) {
        _uiState.value = _uiState.value.copy(intervalMs = value.filter(Char::isDigit))
    }

    fun updateRepeat(value: String) {
        _uiState.value = _uiState.value.copy(repeatCount = value.filter(Char::isDigit))
    }

    fun updateHold(value: String) {
        _uiState.value = _uiState.value.copy(holdDurationMs = value.filter(Char::isDigit))
    }

    fun updateType(type: GestureType) {
        _uiState.value = _uiState.value.copy(type = type)
    }

    fun addPoint(point: TouchPoint) {
        _uiState.value = _uiState.value.copy(
            points = _uiState.value.points + point
        )
    }

    fun savePreset(onSaved: (Long) -> Unit = {}) {
        val state = _uiState.value

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
            _uiState.value = _uiState.value.copy(isSaving = true)
            val savedId = repository.savePreset(preset)
            _uiState.value = _uiState.value.copy(
                presetId = savedId,
                isSaving = false
            )
            onSaved(savedId)
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