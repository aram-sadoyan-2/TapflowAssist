package com.algorithm.tapflow.assist.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun DraggablePoint(
    label: String,
    startX: Int,
    startY: Int,
    onMoved: (Int, Int) -> Unit
) {
    val pointSize = 58.dp
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    val maxX = with(density) { configuration.screenWidthDp.dp.toPx() - pointSize.toPx() }
    val maxY = with(density) { configuration.screenHeightDp.dp.toPx() - pointSize.toPx() }

    var offsetX by remember(startX) { mutableFloatStateOf(startX.toFloat()) }
    var offsetY by remember(startY) { mutableFloatStateOf(startY.toFloat()) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .size(pointSize)
            .background(Color(0xFFE53935), CircleShape)
            .border(2.dp, Color.White.copy(alpha = 0.9f), CircleShape)
            .pointerInput(maxX, maxY) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()

                        offsetX = (offsetX + dragAmount.x).coerceIn(0f, maxX)
                        offsetY = (offsetY + dragAmount.y).coerceIn(0f, maxY)

                        onMoved(offsetX.roundToInt(), offsetY.roundToInt())
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}