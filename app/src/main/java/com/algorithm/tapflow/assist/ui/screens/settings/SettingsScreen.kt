package com.algorithm.tapflow.assist.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.algorithm.tapflow.assist.ui.components.AppTopBar
import com.algorithm.tapflow.assist.ui.components.GlassCard
import com.algorithm.tapflow.assist.ui.components.SettingRow
import com.algorithm.tapflow.assist.ui.theme.AppBackground

@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    var vibrationEnabled by remember { mutableStateOf(true) }
    var showTouchMarkers by remember { mutableStateOf(true) }
    var countdownEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppTopBar(
            title = "Settings",
            showBack = true,
            onBack = onBack
        )

        GlassCard {
            SettingRow(
                title = "Vibration",
                subtitle = "Use haptic feedback during sessions.",
                checked = vibrationEnabled,
                onCheckedChange = { vibrationEnabled = it }
            )

            SettingRow(
                title = "Show touch markers",
                subtitle = "Display visible touch points.",
                checked = showTouchMarkers,
                onCheckedChange = { showTouchMarkers = it }
            )

            SettingRow(
                title = "Countdown before start",
                subtitle = "Show a short delay before sessions begin.",
                checked = countdownEnabled,
                onCheckedChange = { countdownEnabled = it }
            )

            SettingRow(
                title = "Dark theme",
                subtitle = "Use the default dark UI.",
                checked = darkModeEnabled,
                onCheckedChange = { darkModeEnabled = it }
            )
        }
    }
}