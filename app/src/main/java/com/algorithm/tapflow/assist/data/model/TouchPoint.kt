package com.algorithm.tapflow.assist.data.model

data class TouchPoint(
    val id: Int,
    val x: Float,
    val y: Float,
    val delayBeforeMs: Long = 0L
)