package com.algorithm.tapflow.assist.overlay

import android.content.Context

object OverlaySavedPointsReader {

    fun read(context: Context): List<OverlayPoint> {
        val prefs = context.getSharedPreferences("overlay_setup_prefs", Context.MODE_PRIVATE)
        val raw = prefs.getString(OverlaySetupService.KEY_POINTS, null).orEmpty()

        if (raw.isBlank()) return emptyList()

        return raw.split("|").mapNotNull { item ->
            val parts = item.split(",")
            if (parts.size != 3) return@mapNotNull null

            val id = parts[0].toLongOrNull() ?: return@mapNotNull null
            val x = parts[1].toIntOrNull() ?: return@mapNotNull null
            val y = parts[2].toIntOrNull() ?: return@mapNotNull null

            OverlayPoint(id = id, x = x, y = y)
        }
    }
}