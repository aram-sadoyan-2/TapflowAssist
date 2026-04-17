package com.algorithm.tapflow.assist.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
            .background(Color.Black.copy(alpha = 0.18f))
    ) {
        points.forEachIndexed { index, point ->
            DraggablePoint(
                label = "${index + 1}",
                startX = point.x,
                startY = point.y,
                onMoved = { x, y -> onMovePoint(point.id, x, y) }
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.70f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onSave) { Text("Save") }
            Button(onClick = onClose) { Text("Close") }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FloatingActionButton(onClick = onAddPoint) {
                Text("+")
            }

            FloatingActionButton(onClick = onDeleteLast) {
                Text("−")
            }
        }
    }
}