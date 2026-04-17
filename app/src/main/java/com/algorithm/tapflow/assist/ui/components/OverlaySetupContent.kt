package com.algorithm.tapflow.assist.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun OverlaySetupContent(
    points: List<OverlayPoint>,
    onAddPoint: () -> Unit,
    onDeleteLast: () -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
    onMovePoint: (Long, Int, Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        points.forEachIndexed { index, point ->
            DraggablePoint(
                label = "${index + 1}",
                startX = point.x,
                startY = point.y,
                onMoved = { x, y ->
                    onMovePoint(point.id, x, y)
                }
            )
        }

        DraggableControlPanel(
            onAddPoint = onAddPoint,
            onDeleteLast = onDeleteLast,
            onSave = onSave,
            onClose = onClose
        )
    }
}