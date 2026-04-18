package com.algorithm.tapflow.assist.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.algorithm.tapflow.assist.data.model.GestureType
import com.algorithm.tapflow.assist.data.model.TouchPoint
import com.algorithm.tapflow.assist.data.model.TouchPreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class TouchAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var instance: TouchAccessibilityService? = null
            private set
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var runJob: Job? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d("TapFlowRun", "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        runJob?.cancel()
        serviceScope.cancel()
        if (instance === this) instance = null
        super.onDestroy()
    }

    fun startPreset(preset: TouchPreset) {
        Log.d(
            "TapFlowRun",
            "startPreset called type=${preset.type} points=${preset.points.size} repeat=${preset.repeatCount}"
        )
        runJob?.cancel()

        runJob = serviceScope.launch {
            val repeatCount = preset.repeatCount.coerceAtLeast(1)

            repeat(repeatCount) { index ->
                if (!isActive) return@launch

                runGestureOnce(
                    type = preset.type,
                    points = preset.points,
                    holdDurationMs = preset.holdDurationMs
                )

                if (index < repeatCount - 1) {
                    delay(preset.intervalMs.coerceAtLeast(1L))
                }
            }

            PresetRuntimeSession.stop()
        }
    }

    fun pausePreset() {
        runJob?.cancel()
        runJob = null
    }

    fun stopPreset() {
        runJob?.cancel()
        runJob = null
        PresetRuntimeSession.stop()
    }

    private suspend fun runGestureOnce(
        type: GestureType,
        points: List<TouchPoint>,
        holdDurationMs: Long = 0L
    ) {
        Log.d(
            "TapFlowRun",
            "runGestureOnce type=$type points=${points.size} hold=$holdDurationMs"
        )
        when (type) {
            GestureType.SINGLE_TAP -> {
                val point = points.firstOrNull() ?: return
                dispatchTap(point.x, point.y, 1L)
            }

            GestureType.MULTI_TAP -> {
                points.forEach { point ->
                    dispatchTap(point.x, point.y, 1L)
                    delay(50L)
                }
            }

            GestureType.LONG_PRESS -> {
                val point = points.firstOrNull() ?: return
                dispatchTap(point.x, point.y, holdDurationMs.coerceAtLeast(1L))
            }

            GestureType.SWIPE -> {
                if (points.size >= 2) {
                    dispatchSwipe(points)
                }
            }
        }
    }

    private suspend fun dispatchTap(x: Float, y: Float, durationMs: Long) {
        Log.d("TapFlowRun", "dispatchTap x=$x y=$y duration=$durationMs")
        val path = Path().apply { moveTo(x, y) }

        val gesture = GestureDescription.Builder()
            .addStroke(
                GestureDescription.StrokeDescription(path, 0L, durationMs)
            )
            .build()

        dispatchGestureAwait(gesture)
    }

    private suspend fun dispatchSwipe(points: List<TouchPoint>) {
        Log.d("TapFlowRun", "dispatchSwipe points=${points.size}")
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }

        val gesture = GestureDescription.Builder()
            .addStroke(
                GestureDescription.StrokeDescription(path, 0L, 500L)
            )
            .build()

        dispatchGestureAwait(gesture)
    }

    private suspend fun dispatchGestureAwait(gesture: GestureDescription) {
        suspendCancellableCoroutine { continuation ->
            val dispatched = dispatchGesture(
                gesture,
                object : GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        if (continuation.isActive) continuation.resume(Unit)
                    }

                    override fun onCancelled(gestureDescription: GestureDescription?) {
                        if (continuation.isActive) continuation.resume(Unit)
                    }
                },
                null
            )

            if (!dispatched && continuation.isActive) {
                continuation.resume(Unit)
            }
        }
    }
}