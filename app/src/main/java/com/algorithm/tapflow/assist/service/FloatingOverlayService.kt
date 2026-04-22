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
import android.view.MotionEvent
import android.view.View
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
    private var overlayParams: WindowManager.LayoutParams? = null

    override fun onCreate() {
        super.onCreate()
        startInForeground()
        createOverlay()

        val preset = PresetRuntimeSession.activePreset.value
        TouchAccessibilityService.instance?.showPresetPoints(preset)
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

        val dragHandle = TextView(this).apply {
            text = "≡"
            textSize = 18f
            setTextColor(Color.WHITE)
            setPadding(20, 10, 20, 10)
            setBackgroundColor(0x00000000)
        }

        val playView = actionText("▶") {
            val preset = PresetRuntimeSession.activePreset.value
            if (preset == null) {
                Toast.makeText(this, "No preset loaded", Toast.LENGTH_SHORT).show()
                Log.d("TapFlowRun", "Play pressed but preset is null")
                return@actionText
            }

            val service = TouchAccessibilityService.instance
            if (service == null) {
                Toast.makeText(
                    this,
                    "Accessibility service is not connected",
                    Toast.LENGTH_SHORT
                ).show()
                return@actionText
            }

            Log.d("TapFlowRun", "Starting preset: ${preset.name}")
            service.showPresetPoints(preset)
            service.startPreset(preset)
        }

        val pauseView = actionText("⏸") {
            Log.d("TapFlowRun", "Pause pressed")
            TouchAccessibilityService.instance?.pausePreset()
        }

        val stopView = actionText("■") {
            Log.d("TapFlowRun", "Stop pressed")
            TouchAccessibilityService.instance?.stopPreset()
            stopSelf()
        }

        val buttonsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(playView)
            addView(pauseView)
            addView(stopView)
        }

        overlayView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
            setBackgroundColor(0xDD111A2E.toInt())

            addView(dragHandle)
            addView(buttonsRow)
        }

        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        overlayParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 80
            y = 220
        }

        attachDragBehavior(dragHandle)

        windowManager?.addView(overlayView, overlayParams)
    }

    private fun attachDragBehavior(dragHandle: View) {
        dragHandle.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val params = overlayParams ?: return false
                val view = overlayView ?: return false
                val wm = windowManager ?: return false

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        wm.updateViewLayout(view, params)
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun actionText(label: String, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            text = label
            textSize = 22f
            setTextColor(Color.WHITE)
            setPadding(28, 16, 28, 16)
            setOnClickListener { onClick() }
        }
    }

    private fun removeOverlay() {
        overlayView?.let { view ->
            runCatching { windowManager?.removeView(view) }
        }
        overlayView = null
        overlayParams = null
        windowManager = null
    }

    companion object {
        private const val CHANNEL_ID = "tapflow_overlay_channel"
        private const val NOTIFICATION_ID = 1002
    }
}