package com.algorithm.tapflow.assist.service

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.algorithm.tapflow.assist.data.model.TouchPreset

object FloatingOverlayStarter {

    fun start(context: Context, preset: TouchPreset) {
        Log.d("TapFlowRun", "FloatingOverlayStarter.start preset=${preset.name} id=${preset.id}")
        PresetRuntimeSession.setPreset(preset)

        val intent = Intent(context, FloatingOverlayService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, intent)
        } else {
            context.startService(intent)
        }
    }

    fun stop(context: Context) {
        context.stopService(Intent(context, FloatingOverlayService::class.java))
        PresetRuntimeSession.stop()
    }
}