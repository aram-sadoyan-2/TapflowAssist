package com.algorithm.tapflow.assist.overlay

import com.algorithm.tapflow.assist.data.model.GestureType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object OverlaySetupSession {

    private val _points = MutableStateFlow<List<OverlayPoint>>(emptyList())
    val points: StateFlow<List<OverlayPoint>> = _points.asStateFlow()

    private var nextId = 1L

    private val _gestureType = MutableStateFlow(GestureType.SINGLE_TAP)
    val gestureType: StateFlow<GestureType> = _gestureType.asStateFlow()

    fun setGestureType(type: GestureType) {
        _gestureType.value = type
    }

    fun reset() {
        _points.value = emptyList()
        nextId = 1L
    }

    fun setPoints(points: List<OverlayPoint>) {
        _points.value = points
        nextId = (points.maxOfOrNull { it.id } ?: 0L) + 1L
    }

    fun addPoint(defaultX: Int = 300, defaultY: Int = 500) {
        _points.value += OverlayPoint(
                    id = nextId++,
                    x = defaultX,
                    y = defaultY
                )
    }

    fun deleteLastPoint() {
        _points.value = _points.value.dropLast(1)
    }

    fun movePoint(id: Long, x: Int, y: Int) {
        _points.value = _points.value.map { point ->
            if (point.id == id) point.copy(x = x, y = y) else point
        }
    }

    fun replaceWithSinglePoint(x: Int, y: Int) {
        _points.value = listOf(
            OverlayPoint(
                id = 1L,
                x = x,
                y = y
            )
        )
        nextId = 2L
    }

    fun getPoints(): List<OverlayPoint> = _points.value
}