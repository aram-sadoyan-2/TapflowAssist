package com.algorithm.tapflow.assist.ui.screens.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.algorithm.tapflow.assist.data.model.GestureType
import com.algorithm.tapflow.assist.ui.components.AppTopBar
import com.algorithm.tapflow.assist.ui.components.GlassCard
import com.algorithm.tapflow.assist.ui.components.PrimaryButton
import com.algorithm.tapflow.assist.ui.components.SectionTitle
import com.algorithm.tapflow.assist.ui.components.TouchPointItem
import com.algorithm.tapflow.assist.ui.theme.AppBackground
import com.algorithm.tapflow.assist.ui.theme.AppCardSecondary
import com.algorithm.tapflow.assist.ui.theme.PrimaryBlue
import com.algorithm.tapflow.assist.ui.theme.TextPrimary
import com.algorithm.tapflow.assist.ui.theme.TextSecondary
import com.algorithm.tapflow.assist.ui.viewmodel.EditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    presetId: Long?,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(presetId) {
        if (presetId != null && presetId > 0L) {
            viewModel.loadPreset(presetId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppTopBar(
            title = if ((presetId ?: 0L) > 0L) "Edit Preset" else "New Preset",
            showBack = true,
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Loading preset...",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            return
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Preview Area",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                            .background(AppCardSecondary)
                    ) {
                        uiState.points.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .padding(
                                        start = (40 + index * 60).dp,
                                        top = (50 + index * 50).dp
                                    )
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                GlassCard {
                    SectionTitle("Preset Details")
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::updateName,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Preset name") },
                        colors = fieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = uiState.type.name.replace("_", " "),
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            label = { Text("Gesture type") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            colors = fieldColors()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            GestureType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name.replace("_", " ")) },
                                    onClick = {
                                        viewModel.updateType(type)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.intervalMs,
                        onValueChange = viewModel::updateInterval,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Interval (ms)") },
                        colors = fieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.repeatCount,
                        onValueChange = viewModel::updateRepeat,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Repeat count") },
                        colors = fieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.holdDurationMs,
                        onValueChange = viewModel::updateHold,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Hold duration (ms)") },
                        colors = fieldColors()
                    )
                }
            }

            item {
                GlassCard {
                    SectionTitle("Touch Points")
                    Spacer(modifier = Modifier.height(10.dp))

                    uiState.points.forEachIndexed { index, point ->
                        TouchPointItem(index + 1, point)
                    }
                }
            }

            item {
                PrimaryButton(
                    text = if (uiState.isSaving) "Saving..." else "Save Preset",
                    onClick = {
                        viewModel.savePreset {
                            onBack()
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun fieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = AppCardSecondary,
    unfocusedContainerColor = AppCardSecondary,
    disabledContainerColor = AppCardSecondary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedLabelColor = TextSecondary,
    unfocusedLabelColor = TextSecondary,
    focusedIndicatorColor = PrimaryBlue,
    unfocusedIndicatorColor = TextSecondary,
    cursorColor = PrimaryBlue
)