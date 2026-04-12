package com.algorithm.tapflow.assist.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.algorithm.tapflow.assist.data.model.TouchPoint
import com.algorithm.tapflow.assist.ui.theme.AppCardSecondary
import com.algorithm.tapflow.assist.ui.theme.DividerColor
import kotlin.math.hypot

@Composable
fun TouchPointsEditor(
    points: List<TouchPoint>,
    selectedPointId: Int?,
    onAddPoint: (Float, Float) -> Unit,
    onMovePoint: (Int, Float, Float) -> Unit,
    onSelectPoint: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val pointRadiusPx = with(density) { 22.dp.toPx() }
    val hitRadiusPx = with(density) { 28.dp.toPx() }

    var canvasWidth by remember { mutableIntStateOf(0) }
    var canvasHeight by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp)
            .background(AppCardSecondary, RoundedCornerShape(24.dp))
            .onSizeChanged {
                canvasWidth = it.width
                canvasHeight = it.height
            }
            .pointerInput(points, canvasWidth, canvasHeight) {
                detectTapGestures { offset ->
                    val hitExisting = points.any { point ->
                        hypot(
                            (offset.x - point.x).toDouble(),
                            (offset.y - point.y).toDouble()
                        ) <= hitRadiusPx
                    }

                    if (!hitExisting) {
                        val x = offset.x.coerceIn(pointRadiusPx, canvasWidth.toFloat() - pointRadiusPx)
                        val y = offset.y.coerceIn(pointRadiusPx, canvasHeight.toFloat() - pointRadiusPx)
                        onAddPoint(x, y)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            if (points.size >= 2) {
                for (i in 0 until points.lastIndex) {
                    drawLine(
                        color = DividerColor,
                        start = Offset(points[i].x, points[i].y),
                        end = Offset(points[i + 1].x, points[i + 1].y),
                        strokeWidth = 3.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f))
                    )
                }
            }
        }

        points.forEachIndexed { index, point ->
            DraggablePoint(
                number = index + 1,
                x = point.x,
                y = point.y,
                isSelected = point.id == selectedPointId,
                canvasWidth = canvasWidth.toFloat(),
                canvasHeight = canvasHeight.toFloat(),
                onTap = { onSelectPoint(point.id) },
                onDrag = { newX, newY ->
                    onMovePoint(point.id, newX, newY)
                }
            )
        }
    }
}