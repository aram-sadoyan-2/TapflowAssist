package com.algorithm.tapflow.assist.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.algorithm.tapflow.assist.R

class FloatingOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: LinearLayout? = null

    override fun onCreate() {
        super.onCreate()
        startInForeground()
        createOverlay()
    }

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startInForeground() {
        val channelId = CHANNEL_ID

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "TapFlow Overlay",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("TapFlow Assist")
            .setContentText("Floating controls are active")
            .setOngoing(true)
            .build()

        val foregroundType =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            foregroundType
        )
    }

    private fun createOverlay() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val playView = actionText("▶") {
            val preset = PresetRuntimeSession.activePreset.value
            if (preset == null) {
                Toast.makeText(this, "No preset loaded", Toast.LENGTH_SHORT).show()
                Log.d("TapFlowRun", "Play pressed but preset is null")
                return@actionText
            }

            val service = TouchAccessibilityService.instance
            if (service == null) {
                Toast.makeText(this, "Enable accessibility service first", Toast.LENGTH_SHORT).show()
                Log.d("TapFlowRun", "Play pressed but accessibility service is null")
                return@actionText
            }

            Log.d("TapFlowRun", "Starting preset: ${preset.name}")
            PresetRuntimeSession.start()
            service.startPreset(preset)
        }

        val pauseView = actionText("⏸") {
            Log.d("TapFlowRun", "Pause pressed")
            PresetRuntimeSession.pause()
            TouchAccessibilityService.instance?.pausePreset()
        }

        val stopView = actionText("■") {
            Log.d("TapFlowRun", "Stop pressed")
            TouchAccessibilityService.instance?.stopPreset()
            stopSelf()
        }

        overlayView = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(32, 20, 32, 20)
            setBackgroundColor(0xCC111A2E.toInt())

            addView(playView)
            addView(pauseView)
            addView(stopView)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            x = 0
            y = 200
        }

        windowManager?.addView(overlayView, params)
    }

    private fun actionText(label: String, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            text = label
            textSize = 22f
            setTextColor(Color.WHITE)
            setPadding(24, 10, 24, 10)
            setOnClickListener { onClick() }
        }
    }

    private fun removeOverlay() {
        overlayView?.let { view ->
            runCatching { windowManager?.removeView(view) }
        }
        overlayView = null
        windowManager = null
    }

    companion object {
        private const val CHANNEL_ID = "tapflow_overlay_channel"
        private const val NOTIFICATION_ID = 1002
    }
}