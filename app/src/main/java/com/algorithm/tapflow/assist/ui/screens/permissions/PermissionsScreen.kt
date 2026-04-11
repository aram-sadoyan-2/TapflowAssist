package com.algorithm.tapflow.assist.ui.screens.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.algorithm.tapflow.assist.ui.components.AppTopBar
import com.algorithm.tapflow.assist.ui.components.GlassCard
import com.algorithm.tapflow.assist.ui.components.PrimaryButton
import com.algorithm.tapflow.assist.ui.components.SettingRow
import com.algorithm.tapflow.assist.ui.theme.AppBackground
import com.algorithm.tapflow.assist.ui.theme.TextPrimary
import com.algorithm.tapflow.assist.ui.theme.TextSecondary

@Composable
fun PermissionsScreen(
    onBack: () -> Unit
) {
    var overlayEnabled by remember { mutableStateOf(false) }
    var accessibilityEnabled by remember { mutableStateOf(false) }
    var batteryIgnored by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppTopBar(
            title = "Permissions",
            showBack = true,
            onBack = onBack
        )

        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Enable Accessibility",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Grant the required permissions to use touch assistance controls.",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            SettingRow(
                title = "Overlay Permission",
                subtitle = "Show floating controls above apps.",
                checked = overlayEnabled,
                onCheckedChange = { overlayEnabled = it }
            )

            SettingRow(
                title = "Accessibility Service",
                subtitle = "Enable touch assistance actions.",
                checked = accessibilityEnabled,
                onCheckedChange = { accessibilityEnabled = it }
            )

            SettingRow(
                title = "Battery Optimization",
                subtitle = "Allow better session stability.",
                checked = batteryIgnored,
                onCheckedChange = { batteryIgnored = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            PrimaryButton(
                text = "Enable Service",
                onClick = {}
            )
        }
    }
}