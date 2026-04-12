package com.algorithm.tapflow.assist.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import com.algorithm.tapflow.assist.data.model.GestureType
import com.algorithm.tapflow.assist.data.model.TouchPoint

class TouchAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var instance: TouchAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        super.onDestroy()
        if (instance === this) instance = null
    }

    fun runUserStartedGesture(
        type: GestureType,
        points: List<TouchPoint>,
        holdDurationMs: Long = 0L
    ) {
        when (type) {
            GestureType.SINGLE_TAP,
            GestureType.MULTI_TAP,
            GestureType.LONG_PRESS -> {
                points.forEach { point ->
                    dispatchTap(point.x, point.y, maxOf(1L, holdDurationMs))
                }
            }

            GestureType.SWIPE -> {
                if (points.size >= 2) {
                    dispatchSwipe(points)
                }
            }
        }
    }

    private fun dispatchTap(x: Float, y: Float, durationMs: Long) {
        val path = Path().apply {
            moveTo(x, y)
        }

        val gesture = GestureDescription.Builder()
            .addStroke(
                GestureDescription.StrokeDescription(path, 0L, durationMs)
            )
            .build()

        dispatchGesture(gesture, null, null)
    }

    private fun dispatchSwipe(points: List<TouchPoint>) {
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }

        val gesture = GestureDescription.Builder()
            .addStroke(
                GestureDescription.StrokeDescription(path, 0L, 500L)
            )
            .build()

        dispatchGesture(gesture, null, null)
    }
}