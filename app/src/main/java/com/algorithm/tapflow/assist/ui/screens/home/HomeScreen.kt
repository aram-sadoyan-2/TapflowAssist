package com.algorithm.tapflow.assist.ui.screens.home

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.algorithm.tapflow.assist.overlay.OverlaySetupService
import com.algorithm.tapflow.assist.overlay.OverlaySetupSession
import com.algorithm.tapflow.assist.overlay.OverlayStarter
import com.algorithm.tapflow.assist.ui.components.AppTopBar
import com.algorithm.tapflow.assist.ui.components.GlassCard
import com.algorithm.tapflow.assist.ui.components.PrimaryButton
import com.algorithm.tapflow.assist.ui.components.SectionTitle
import com.algorithm.tapflow.assist.ui.theme.AppBackground
import com.algorithm.tapflow.assist.ui.theme.SuccessGreen
import com.algorithm.tapflow.assist.ui.theme.TextPrimary
import com.algorithm.tapflow.assist.ui.theme.TextSecondary
import com.algorithm.tapflow.assist.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCreateNew: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenPresets: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        AppTopBar(
            title = "TapFlow Assist",
            showSettings = true,
            onSettings = onOpenSettings
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Welcome back",
            color = TextPrimary,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "User-controlled touch assistance",
            color = TextSecondary,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "● Ready",
            color = SuccessGreen,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(20.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Create New Setup",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Build a tap, long press, or swipe preset.",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            PrimaryButton(
                text = "Start",
                onClick = onCreateNew
            )

            val context = LocalContext.current
//            PrimaryButton(
//                text = "Setup Points Over Apps",
//                onClick = {
//                    Log.d("dwd", "Button clicked")
//
//                    if (!OverlayStarter.canDrawOverlays(context)) {
//                        Log.d("dwd", "Overlay permission missing, opening settings")
//                        OverlayStarter.openOverlayPermission(context)
//                    } else {
//                        Log.d("dwd", "Overlay permission granted, starting service")
//
//                        OverlaySetupSession.reset()
//
//                        uiState.points.forEach { point ->
//                            OverlaySetupSession.addPoint(
//                                defaultX = point.x.toInt(),
//                                defaultY = point.y.toInt()
//                            )
//                        }
//
//                        if (uiState.points.isEmpty()) {
//                            OverlaySetupSession.addPoint(300, 500)
//                        }
//
//                        OverlayStarter.startOverlay(context)
//                    }
//                }
//            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GlassCard(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Saved Presets",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Open and manage your saved setups.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                PrimaryButton(text = "Open", onClick = onOpenPresets)
            }

            GlassCard(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Permissions",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Overlay and accessibility setup.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                PrimaryButton(text = "Setup", onClick = onOpenPermissions)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        SectionTitle(text = "Recent Presets")
        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.presets.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No presets yet",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Create your first setup to see it here.",
                    color = TextSecondary
                )
            }
        } else {
            uiState.presets.take(3).forEach { preset ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = preset.name,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${preset.type.name.replace("_", " ")} • ${preset.intervalMs} ms • ${preset.repeatCount} repeats",
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}