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
import android.provider.Settings
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

    private var playView: TextView? = null
    private var pauseView: TextView? = null
    private var stopView: TextView? = null

    override fun onCreate() {
        super.onCreate()
        startInForeground()
        createOverlay()

        val preset = PresetRuntimeSession.activePreset.value
        TouchAccessibilityService.instance?.showPresetPoints(preset)

        updateButtonStates(isRunning = false, isStopped = true)
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Overlay permission is required", Toast.LENGTH_SHORT).show()
            stopSelf()
            return
        }

        val dragHandle = createDragHandle()

        playView = actionText("▶") {
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
            updateButtonStates(isRunning = true, isStopped = false)
        }

        pauseView = actionText("⏸") {
            Log.d("TapFlowRun", "Pause pressed")
            TouchAccessibilityService.instance?.pausePreset()
            updateButtonStates(isRunning = false, isStopped = false)
        }

        stopView = actionText("■") {
            Log.d("TapFlowRun", "Stop pressed")
            TouchAccessibilityService.instance?.stopPreset()
            updateButtonStates(isRunning = false, isStopped = true)
            stopSelf()
        }

        overlayView = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16, 16, 16, 16)
            setBackgroundColor(0xDD111A2E.toInt())

            addView(dragHandle)
            addView(playView)
            addView(pauseView)
            addView(stopView)
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

        runCatching {
            windowManager?.addView(overlayView, overlayParams)
        }.onFailure { error ->
            Log.e("TapFlowRun", "Failed to add floating overlay", error)
            Toast.makeText(this, "Failed to show floating controls", Toast.LENGTH_SHORT).show()
            stopSelf()
        }
    }

    private fun createDragHandle(): TextView {
        return TextView(this).apply {
            text = "≡"
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(24, 22, 24, 22)
            setBackgroundColor(0x334A5A7A)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = 14
            }
        }
    }

    private fun actionText(label: String, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            text = label
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(34, 22, 34, 22)
            setBackgroundColor(0x223A4A6A)
            isClickable = true
            isFocusable = true

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = 12
            }

            setOnClickListener { onClick() }
        }
    }

    private fun updateButtonStates(isRunning: Boolean, isStopped: Boolean) {
        val normalBg = 0x223A4A6A
        val activeBg = 0xFF2E7D32.toInt()
        val pauseBg = 0xFFF9A825.toInt()
        val stopBg = 0xFFC62828.toInt()

        playView?.apply {
            isSelected = isRunning
            setBackgroundColor(if (isRunning) activeBg else normalBg)
            setTextColor(Color.WHITE)
            alpha = if (isRunning) 1f else 0.9f
        }

        pauseView?.apply {
            val selected = !isRunning && !isStopped
            isSelected = selected
            setBackgroundColor(if (selected) pauseBg else normalBg)
            setTextColor(Color.WHITE)
            alpha = if (selected) 1f else 0.9f
        }

        stopView?.apply {
            isSelected = isStopped
            setBackgroundColor(if (isStopped) stopBg else normalBg)
            setTextColor(Color.WHITE)
            alpha = if (isStopped) 1f else 0.9f
        }
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

                when (event.actionMasked) {
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

                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> return true
                }
                return false
            }
        })
    }

    private fun removeOverlay() {
        overlayView?.let { view ->
            runCatching { windowManager?.removeView(view) }
        }
        overlayView = null
        overlayParams = null
        playView = null
        pauseView = null
        stopView = null
        windowManager = null
    }

    companion object {
        private const val CHANNEL_ID = "tapflow_overlay_channel"
        private const val NOTIFICATION_ID = 1002
    }
}