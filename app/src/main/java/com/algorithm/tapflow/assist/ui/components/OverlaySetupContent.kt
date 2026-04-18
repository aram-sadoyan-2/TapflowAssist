package com.algorithm.tapflow.assist.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.algorithm.tapflow.assist.data.model.GestureType

@Composable
fun OverlaySetupContent(
    type: GestureType,
    points: List<OverlayPoint>,
    onAddPoint: () -> Unit,
    onDeleteLast: () -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
    onMovePoint: (Long, Int, Int) -> Unit,
    onReplaceSinglePoint: (Int, Int) -> Unit
) {
    val singlePointMode = type == GestureType.SINGLE_TAP || type == GestureType.LONG_PRESS

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        points.forEachIndexed { index, point ->
            DraggablePoint(
                label = if (singlePointMode) null else "${index + 1}",
                startX = point.x,
                startY = point.y,
                onMoved = { x, y ->
                    onMovePoint(point.id, x, y)
                }
            )
        }

        DraggableControlPanel(
            showAddRemove = !singlePointMode,
            onAddPoint = onAddPoint,
            onDeleteLast = onDeleteLast,
            onSave = onSave,
            onClose = onClose
        )
    }
}