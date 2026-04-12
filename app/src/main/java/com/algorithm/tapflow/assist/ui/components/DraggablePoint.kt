package com.algorithm.tapflow.assist.ui.components

import android.view.MotionEvent
import android.view.ViewParent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.algorithm.tapflow.assist.ui.theme.PrimaryBlue
import com.algorithm.tapflow.assist.ui.theme.SecondaryPurple
import com.algorithm.tapflow.assist.ui.theme.TextPrimary
import kotlin.math.roundToInt

@Composable
fun DraggablePoint(
    number: Int,
    x: Float,
    y: Float,
    isSelected: Boolean,
    canvasWidth: Float,
    canvasHeight: Float,
    onTap: () -> Unit,
    onDrag: (Float, Float) -> Unit
) {
    val density = LocalDensity.current
    val view = LocalView.current
    val radiusPx = with(density) { 22.dp.toPx() }

    var lastRawX by remember { mutableFloatStateOf(0f) }
    var lastRawY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableFloatStateOf(0f) } // 0f=false, 1f=true

    Box(
        modifier = Modifier
            .zIndex(100f)
            .offset {
                IntOffset(
                    (x - radiusPx).roundToInt(),
                    (y - radiusPx).roundToInt()
                )
            }
            .size(44.dp)
            .background(
                color = if (isSelected) SecondaryPurple else PrimaryBlue,
                shape = CircleShape
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = Color.White,
                shape = CircleShape
            )
            .pointerInteropFilter { event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        onTap()
                        isDragging = 1f
                        lastRawX = event.rawX
                        lastRawY = event.rawY
                        requestDisallowIntercept(view.parent, true)
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (isDragging == 1f) {
                            val dx = event.rawX - lastRawX
                            val dy = event.rawY - lastRawY

                            lastRawX = event.rawX
                            lastRawY = event.rawY

                            val newX = (x + dx).coerceIn(radiusPx, canvasWidth - radiusPx)
                            val newY = (y + dy).coerceIn(radiusPx, canvasHeight - radiusPx)

                            onDrag(newX, newY)
                        }
                        true
                    }

                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        isDragging = 0f
                        requestDisallowIntercept(view.parent, false)
                        true
                    }

                    else -> false
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            color = TextPrimary
        )
    }
}

private fun requestDisallowIntercept(parent: ViewParent?, disallow: Boolean) {
    parent?.requestDisallowInterceptTouchEvent(disallow)
}