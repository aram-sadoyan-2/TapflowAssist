package com.algorithm.tapflow.assist.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.algorithm.tapflow.assist.ui.state.PermissionsUiState

class PermissionsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionsUiState())
    val uiState: StateFlow<PermissionsUiState> = _uiState.asStateFlow()

    fun setOverlayGranted(value: Boolean) {
        _uiState.value = _uiState.value.copy(overlayGranted = value)
    }

    fun setAccessibilityGranted(value: Boolean) {
        _uiState.value = _uiState.value.copy(accessibilityGranted = value)
    }

    fun setBatteryIgnored(value: Boolean) {
        _uiState.value = _uiState.value.copy(batteryOptimizationIgnored = value)
    }
}