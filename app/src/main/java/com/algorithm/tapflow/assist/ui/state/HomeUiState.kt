package com.algorithm.tapflow.assist.ui.state

import com.algorithm.tapflow.assist.data.model.TouchPreset

data class HomeUiState(
    val presets: List<TouchPreset> = emptyList(),
    val isLoading: Boolean = true
)