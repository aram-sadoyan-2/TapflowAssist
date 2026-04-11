package com.algorithm.tapflow.assist.data.model

data class TouchPreset(
    val id: Long,
    val name: String,
    val type: GestureType,
    val intervalMs: Long,
    val repeatCount: Int,
    val holdDurationMs: Long,
    val points: List<TouchPoint>
)