package com.algorithm.tapflow.assist.onboarding

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.algorithm.tapflow.assist.service.TouchAccessibilityService

@Composable
fun PermissionSetupScreen(
    onContinue: () -> Unit
) {
    val context = LocalContext.current

    var refreshKey by remember { mutableIntStateOf(0) }
    var showAccessibilityDisclosure by remember { mutableStateOf(false) }

    var isOverlayEnabled by remember { mutableStateOf(false) }
    var isAccessibilityEnabled by remember { mutableStateOf(false) }

    fun refreshPermissions() {
        isOverlayEnabled = Settings.canDrawOverlays(context)
        isAccessibilityEnabled = isAccessibilityServiceEnabled(
            context = context,
            serviceClass = TouchAccessibilityService::class.java
        )
    }

    LaunchedEffect(refreshKey) {
        refreshPermissions()
    }

    DisposableEffect(Unit) {
        val lifecycleOwner = androidx.lifecycle.ProcessLifecycleOwner.get()

        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                refreshKey++
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val canContinue = isOverlayEnabled && isAccessibilityEnabled

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF17234A),
                        Color(0xFF090B13),
                        Color(0xFF05060B)
                    ),
                    radius = 1200f
                )
            )
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(WindowInsets.navigationBars.asPaddingValues())
    ) {
        SoftPermissionGlow(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 72.dp),
            color = Color(0xFF7C3DFF)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp)
                .padding(top = 34.dp, bottom = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Setup Required",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Enable the required permissions so TapFlow Assist can show controls and run your saved presets.",
                color = Color.White.copy(alpha = 0.74f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            PermissionCard(
                title = "Floating Overlay",
                description = "Shows tap points and controls over other apps.",
                buttonText = "Enable Overlay",
                isEnabled = isOverlayEnabled,
                iconType = PermissionIconType.Overlay,
                accentStart = Color(0xFF42A5FF),
                accentEnd = Color(0xFF7C3DFF),
                onClick = {
                    openOverlaySettings(context)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            PermissionCard(
                title = "Accessibility Service",
                description = "Performs only the taps, long presses, and swipes from presets you create and start.",
                buttonText = "Enable Accessibility",
                isEnabled = isAccessibilityEnabled,
                iconType = PermissionIconType.Accessibility,
                accentStart = Color(0xFF8B5CFF),
                accentEnd = Color(0xFFC45CFF),
                onClick = {
                    showAccessibilityDisclosure = true
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            PermissionCard(
                title = "Battery Optimization",
                description = "Optional: helps keep controls reliable while a preset is running.",
                buttonText = "Open Settings",
                isEnabled = false,
                optionalLabel = "Optional",
                iconType = PermissionIconType.Battery,
                accentStart = Color(0xFF3D9BFF),
                accentEnd = Color(0xFF3C6DFF),
                onClick = {
                    openBatteryOptimizationSettings(context)
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            AnimatedVisibility(visible = !canContinue) {
                Text(
                    text = "Overlay and Accessibility are required to continue.",
                    color = Color.White.copy(alpha = 0.58f),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Button(
                onClick = onContinue,
                enabled = canContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .shadow(
                        elevation = if (canContinue) 18.dp else 0.dp,
                        shape = RoundedCornerShape(18.dp),
                        ambientColor = Color.White.copy(alpha = 0.18f),
                        spotColor = Color.White.copy(alpha = 0.18f)
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF080B15),
                    disabledContainerColor = Color.White.copy(alpha = 0.18f),
                    disabledContentColor = Color.White.copy(alpha = 0.38f)
                )
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }

    if (showAccessibilityDisclosure) {
        AccessibilityDisclosureDialog(
            onDecline = {
                showAccessibilityDisclosure = false
            },
            onAgree = {
                showAccessibilityDisclosure = false
                openAccessibilitySettings(context)
            }
        )
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    buttonText: String,
    isEnabled: Boolean,
    optionalLabel: String? = null,
    iconType: PermissionIconType,
    accentStart: Color,
    accentEnd: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.13f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(accentStart, accentEnd)
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.22f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (iconType) {
                            PermissionIconType.Overlay -> Icons.Rounded.Layers
                            PermissionIconType.Accessibility -> Icons.Rounded.Security
                            PermissionIconType.Battery -> Icons.Rounded.BatteryChargingFull
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(27.dp)
                    )
                }

                Spacer(modifier = Modifier.size(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )

                        if (isEnabled) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF6DFFB5),
                                    modifier = Modifier.size(18.dp)
                                )

                                Text(
                                    text = "Enabled",
                                    color = Color(0xFF6DFFB5),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        } else if (optionalLabel != null) {
                            Text(
                                text = optionalLabel,
                                color = Color.White.copy(alpha = 0.62f),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = description,
                        color = Color.White.copy(alpha = 0.70f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isEnabled) {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(15.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.18f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White.copy(alpha = 0.78f)
                    )
                ) {
                    Text(
                        text = "Open Settings",
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF080B15)
                    )
                ) {
                    Text(
                        text = buttonText,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun AccessibilityDisclosureDialog(
    onDecline: () -> Unit,
    onAgree: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDecline,
        containerColor = Color(0xFF141824),
        titleContentColor = Color.White,
        textContentColor = Color.White.copy(alpha = 0.78f),
        shape = RoundedCornerShape(26.dp),
        title = {
            Text(
                text = "Accessibility Permission",
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Text(
                text = "TapFlow Assist uses Accessibility Service only to perform tap, long press, and swipe gestures for presets that you create and start yourself.\n\n" +
                    "The app does not read your screen content, messages, passwords, contacts, or personal data.\n\n" +
                    "TapFlow Assist does not collect or share Accessibility data.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                Text(
                    text = "Decline",
                    color = Color.White.copy(alpha = 0.70f),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAgree,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF080B15)
                )
            ) {
                Text(
                    text = "Agree",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    )
}

private enum class PermissionIconType {
    Overlay,
    Accessibility,
    Battery
}

@Composable
private fun SoftPermissionGlow(
    modifier: Modifier = Modifier,
    color: Color
) {
    Box(
        modifier = modifier
            .size(260.dp)
            .blur(70.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.40f),
                        color.copy(alpha = 0.12f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            )
    )
}

private fun openOverlaySettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:${context.packageName}")
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    context.startActivity(intent)
}

private fun openAccessibilitySettings(context: Context) {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    context.startActivity(intent)
}

private fun openBatteryOptimizationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    context.startActivity(intent)
}

private fun isAccessibilityServiceEnabled(
    context: Context,
    serviceClass: Class<*>
): Boolean {
    val expectedComponentName = "${context.packageName}/${serviceClass.name}"

    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServices)

    while (colonSplitter.hasNext()) {
        val enabledService = colonSplitter.next()
        if (enabledService.equals(expectedComponentName, ignoreCase = true)) {
            return true
        }
    }

    return false
}