package com.algorithm.tapflow.assist.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.algorithm.tapflow.assist.data.model.TouchPreset
import com.algorithm.tapflow.assist.data.repository.PresetRepository
import com.algorithm.tapflow.assist.service.FloatingOverlayStarter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context

data class PresetsUiState(
    val presets: List<TouchPreset> = emptyList(),
    val isLoading: Boolean = true
)

class PresetsViewModel(
    private val repository: PresetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PresetsUiState())
    val uiState: StateFlow<PresetsUiState> = _uiState.asStateFlow()

    init {
        observePresets()
    }

    private fun observePresets() {
        viewModelScope.launch {
            repository.observePresets().collect { presets ->
                _uiState.value = PresetsUiState(
                    presets = presets,
                    isLoading = false
                )
            }
        }
    }

    fun deletePreset(id: Long) {
        viewModelScope.launch {
            repository.deletePreset(id)
        }
    }

    companion object {
        fun factory(repository: PresetRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PresetsViewModel(repository) as T
                }
            }
        }
    }
}