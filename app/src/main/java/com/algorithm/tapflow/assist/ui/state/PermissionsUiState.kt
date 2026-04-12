package com.algorithm.tapflow.assist.ui.state

data class PermissionsUiState(
    val overlayGranted: Boolean = false,
    val accessibilityGranted: Boolean = false,
    val batteryOptimizationIgnored: Boolean = false
)