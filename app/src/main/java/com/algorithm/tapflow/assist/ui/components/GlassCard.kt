package com.algorithm.tapflow.assist.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.algorithm.tapflow.assist.ui.theme.AppCard
import com.algorithm.tapflow.assist.ui.theme.DividerColor

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(AppCard)
            .border(
                border = BorderStroke(1.dp, DividerColor),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(18.dp),
        content = content
    )
}