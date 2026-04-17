package com.algorithm.tapflow.assist.ui.screens.presets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.algorithm.tapflow.assist.ui.components.AppTopBar
import com.algorithm.tapflow.assist.ui.components.GlassCard
import com.algorithm.tapflow.assist.ui.components.PrimaryButton
import com.algorithm.tapflow.assist.ui.theme.AppBackground
import com.algorithm.tapflow.assist.ui.theme.TextPrimary
import com.algorithm.tapflow.assist.ui.theme.TextSecondary
import com.algorithm.tapflow.assist.ui.viewmodel.PresetsViewModel

@Composable
fun PresetsScreen(
    viewModel: PresetsViewModel,
    onBack: () -> Unit,
    onPresetClick: (Long) -> Unit,
    onStartPreset: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppTopBar(
            title = "Presets",
            showBack = true,
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Loading...",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            uiState.presets.isEmpty() -> {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No presets yet",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Create your first preset from the Home screen.",
                        color = TextSecondary
                    )
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(
                        items = uiState.presets,
                        key = { it.id }
                    ) { preset ->
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPresetClick(preset.id) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = preset.name,
                                        color = TextPrimary,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "Type: ${preset.type.name.replace("_", " ")}",
                                        color = TextSecondary
                                    )

                                    Text(
                                        text = "Interval: ${preset.intervalMs} ms",
                                        color = TextSecondary
                                    )

                                    Text(
                                        text = "Repeats: ${preset.repeatCount}",
                                        color = TextSecondary
                                    )

                                    Text(
                                        text = "Points: ${preset.points.size}",
                                        color = TextSecondary
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    PrimaryButton(
                                        text = "Start",
                                        onClick = { onStartPreset(preset.id) }
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = { viewModel.deletePreset(preset.id) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Delete preset",
                                        tint = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}