package com.algorithm.tapflow.assist.ui.screens.editor

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.algorithm.tapflow.assist.data.model.GestureType
import com.algorithm.tapflow.assist.overlay.OverlaySavedPointsReader
import com.algorithm.tapflow.assist.overlay.OverlaySetupService
import com.algorithm.tapflow.assist.overlay.OverlaySetupSession
import com.algorithm.tapflow.assist.overlay.OverlayStarter
import com.algorithm.tapflow.assist.ui.components.AppTopBar
import com.algorithm.tapflow.assist.ui.components.GlassCard
import com.algorithm.tapflow.assist.ui.components.PrimaryButton
import com.algorithm.tapflow.assist.ui.components.SectionTitle
import com.algorithm.tapflow.assist.ui.components.TouchPointItem
import com.algorithm.tapflow.assist.ui.components.TouchPointsEditor
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
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var expanded by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var lastImportedAt by remember { mutableLongStateOf(0L) }

    LaunchedEffect(presetId) {
        if (presetId != null && presetId > 0L) {
            viewModel.loadPreset(presetId)
        }
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val prefs = context.getSharedPreferences("overlay_setup_prefs", android.content.Context.MODE_PRIVATE)
                val savedAt = prefs.getLong(OverlaySetupService.KEY_LAST_SAVED_AT, 0L)

                if (savedAt > 0L && savedAt != lastImportedAt) {
                    val savedOverlayPoints = OverlaySavedPointsReader.read(context)

                    if (savedOverlayPoints.isNotEmpty()) {
                        viewModel.clearAllPoints()

                        savedOverlayPoints.forEach { point ->
                            viewModel.addPointAt(
                                point.x.toFloat(),
                                point.y.toFloat()
                            )
                        }

                        lastImportedAt = savedAt
                    }
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            userScrollEnabled = !isDragging
        ) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Touch Canvas",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tap empty space to add a point. Drag point to move it.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    TouchPointsEditor(
                        points = uiState.points,
                        selectedPointId = uiState.selectedPointId,
                        onAddPoint = viewModel::addPointAt,
                        onMovePoint = viewModel::movePoint,
                        onSelectPoint = viewModel::selectPoint,
                        onDragStateChanged = { dragging ->
                            isDragging = dragging
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PrimaryButton(
                        text = "Delete Selected Point",
                        onClick = viewModel::removeSelectedPoint
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Log.d("dwd", "dsdsd")

//                    PrimaryButton(
//                        text = "Setup Points Over Apps",
//                        onClick = {
//                            Log.d("dwd", "Button clicked")
//
//                            if (!OverlayStarter.canDrawOverlays(context)) {
//                                Log.d("dwd", "Overlay permission missing, opening settings")
//                                OverlayStarter.openOverlayPermission(context)
//                            } else {
//                                Log.d("dwd", "Overlay permission granted, starting service")
//
//                                OverlaySetupSession.reset()
//
//                                uiState.points.forEach { point ->
//                                    OverlaySetupSession.addPoint(
//                                        defaultX = point.x.toInt(),
//                                        defaultY = point.y.toInt()
//                                    )
//                                }
//
//                                if (uiState.points.isEmpty()) {
//                                    OverlaySetupSession.addPoint(300, 500)
//                                }
//
//                                OverlayStarter.startOverlay(context)
//                            }
//                        }
//                    )
                }
            }

            item {
                GlassCard {
                    SectionTitle(text = "Preset Details")

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
                    SectionTitle(text = "Touch Points")

                    Spacer(modifier = Modifier.height(10.dp))

                    if (uiState.points.isEmpty()) {
                        Text(
                            text = "No points added yet.",
                            color = TextSecondary
                        )
                    } else {
                        uiState.points.forEachIndexed { index, point ->
                            TouchPointItem(
                                index = index + 1,
                                point = point
                            )
                        }
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
                Spacer(modifier = Modifier.height(16.dp))
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