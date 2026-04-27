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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.algorithm.tapflow.assist.permissions.PermissionHelpers
import com.algorithm.tapflow.assist.ui.components.AccessibilityDisclosureDialog
import com.algorithm.tapflow.assist.ui.components.AppTopBar
import com.algorithm.tapflow.assist.ui.components.GlassCard
import com.algorithm.tapflow.assist.ui.components.PrimaryButton
import com.algorithm.tapflow.assist.ui.components.SettingRow
import com.algorithm.tapflow.assist.ui.theme.AppBackground
import com.algorithm.tapflow.assist.ui.theme.TextPrimary
import com.algorithm.tapflow.assist.ui.theme.TextSecondary
import com.algorithm.tapflow.assist.ui.viewmodel.PermissionsViewModel

@Composable
fun PermissionsScreen(
    viewModel: PermissionsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showAccessibilityDisclosure by rememberSaveable {
        mutableStateOf(false)
    }

    DisposableEffect(Unit) {
        viewModel.setOverlayGranted(PermissionHelpers.hasOverlayPermission(context))
        viewModel.setAccessibilityGranted(PermissionHelpers.isAccessibilityServiceEnabled(context))
        viewModel.setBatteryIgnored(PermissionHelpers.isIgnoringBatteryOptimizations(context))

        onDispose { }
    }

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

        GlassCard(modifier = Modifier.fillMaxWidth()) {
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
                checked = uiState.overlayGranted,
                onCheckedChange = {
                    PermissionHelpers.openOverlaySettings(context)
                }
            )

            SettingRow(
                title = "Accessibility Service",
                subtitle = "Run your saved taps, swipes, and gestures.",
                checked = uiState.accessibilityGranted,
                onCheckedChange = {
                    if (!uiState.accessibilityGranted) {
                        showAccessibilityDisclosure = true
                    }
                }
            )

            SettingRow(
                title = "Battery Optimization",
                subtitle = "Allow better session stability.",
                checked = uiState.batteryOptimizationIgnored,
                onCheckedChange = {
                    PermissionHelpers.openBatteryOptimizationSettings(context)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            PrimaryButton(
                text = if (uiState.accessibilityGranted) {
                    "Accessibility Enabled"
                } else {
                    "Enable Accessibility Service"
                },
                onClick = {
                    if (!uiState.accessibilityGranted) {
                        showAccessibilityDisclosure = true
                    }
                }
            )
        }
    }

    if (showAccessibilityDisclosure) {
        AccessibilityDisclosureDialog(
            onAgree = {
                showAccessibilityDisclosure = false
                PermissionHelpers.openAccessibilitySettings(context)
            },
            onDecline = {
                showAccessibilityDisclosure = false
            }
        )
    }
}