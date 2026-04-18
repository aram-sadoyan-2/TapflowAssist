package com.algorithm.tapflow.assist.service

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager

object AccessibilityHelper {

    fun isTouchServiceEnabled(context: Context): Boolean {
        val manager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

        val expectedComponent = ComponentName(
            context,
            TouchAccessibilityService::class.java
        )

        val enabledServices = manager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        )

        return enabledServices.any { info ->
            val serviceInfo = info.resolveInfo?.serviceInfo
            val enabledComponent = serviceInfo?.let {
                ComponentName(it.packageName, it.name)
            }
            enabledComponent == expectedComponent
        }
    }

    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}