package com.algorithm.tapflow.assist.preview

import com.algorithm.tapflow.assist.data.model.GestureType
import com.algorithm.tapflow.assist.data.model.TouchPoint
import com.algorithm.tapflow.assist.data.model.TouchPreset

object PreviewData {
    val samplePoints = listOf(
        TouchPoint(id = 1, x = 220f, y = 420f, delayBeforeMs = 0),
        TouchPoint(id = 2, x = 410f, y = 700f, delayBeforeMs = 400),
        TouchPoint(id = 3, x = 620f, y = 920f, delayBeforeMs = 700)
    )

    val samplePresets = listOf(
        TouchPreset(
            id = 1,
            name = "Reading Assist",
            type = GestureType.MULTI_TAP,
            intervalMs = 1000,
            repeatCount = 20,
            holdDurationMs = 0,
            points = samplePoints
        ),
        TouchPreset(
            id = 2,
            name = "Long Press Action",
            type = GestureType.LONG_PRESS,
            intervalMs = 1500,
            repeatCount = 10,
            holdDurationMs = 800,
            points = listOf(TouchPoint(1, 300f, 500f))
        )
    )
}