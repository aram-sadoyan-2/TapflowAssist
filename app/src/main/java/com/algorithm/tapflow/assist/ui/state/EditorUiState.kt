package com.algorithm.tapflow.assist.ui.state

import com.algorithm.tapflow.assist.data.model.GestureType
import com.algorithm.tapflow.assist.data.model.TouchPoint

data class EditorUiState(
    val presetId: Long = 0,
    val name: String = "New Preset",
    val type: GestureType = GestureType.SINGLE_TAP,
    val intervalMs: String = "1000",
    val repeatCount: String = "10",
    val holdDurationMs: String = "0",
    val points: List<TouchPoint> = emptyList(),
    val isSaving: Boolean = false
)