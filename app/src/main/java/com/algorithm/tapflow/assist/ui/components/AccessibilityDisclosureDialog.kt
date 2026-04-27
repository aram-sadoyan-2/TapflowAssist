package com.algorithm.tapflow.assist.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AccessibilityDisclosureDialog(
    onAgree: () -> Unit,
    onDecline: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            // Back press / outside tap = decline.
            // Never treat dismiss as consent.
            onDecline()
        },
        title = {
            Text(
                text = "Accessibility permission disclosure",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "TapFlow Assist uses the Android AccessibilityService API to run the touch presets that you create."
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "When enabled, the app can perform taps, swipes, and gestures on your device screen only when you start a preset."
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "TapFlow Assist does not use AccessibilityService to read your personal messages, passwords, banking information, or other private screen content. The app does not collect, sell, or share AccessibilityService data."
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "This permission is required for the main feature of the app: running your saved tap presets over other apps."
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Do you agree to enable AccessibilityService for TapFlow Assist?",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDecline
            ) {
                Text("Not now")
            }
        },
        confirmButton = {
            Button(
                onClick = onAgree
            ) {
                Text("I agree")
            }
        }
    )
}