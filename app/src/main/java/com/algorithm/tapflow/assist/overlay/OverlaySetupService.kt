package com.algorithm.tapflow.assist.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.algorithm.tapflow.assist.R

import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

class OverlaySetupService : Service(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val lifecycleDispatcher = ServiceLifecycleDispatcher(this)
    private val vmStore = ViewModelStore()
    private val savedStateController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleDispatcher.lifecycle

    override val viewModelStore: ViewModelStore
        get() = vmStore

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateController.savedStateRegistry

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null

    override fun onCreate() {
        lifecycleDispatcher.onServicePreSuperOnCreate()
        savedStateController.performAttach()
        savedStateController.performRestore(null)
        super.onCreate()

        createNotificationChannel()

        val foregroundType =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            foregroundType
        )

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        showOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleDispatcher.onServicePreSuperOnStart()
        return START_NOT_STICKY
    }

    private fun showOverlay() {
        if (overlayView != null) return

        overlayView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlaySetupService)
            setViewTreeViewModelStoreOwner(this@OverlaySetupService)
            setViewTreeSavedStateRegistryOwner(this@OverlaySetupService)

            setContent {
                val points by OverlaySetupSession.points.collectAsState()
                val type by OverlaySetupSession.gestureType.collectAsState()

                OverlaySetupContent(
                    type = type,
                    points = points,
                    onAddPoint = { OverlaySetupSession.addPoint() },
                    onDeleteLast = { OverlaySetupSession.deleteLastPoint() },
                    onSave = { savePointsAndClose() },
                    onClose = {
                        bringAppToFront()
                        stopSelf()
                    },
                    onMovePoint = { id, x, y ->
                        OverlaySetupSession.movePoint(id, x, y)
                    },
                    onReplaceSinglePoint = { x, y ->
                        OverlaySetupSession.replaceWithSinglePoint(x, y)
                    }
                )
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        windowManager.addView(overlayView, params)
    }

    private fun savePointsAndClose() {
        val points = OverlaySetupSession.points.value
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = points.joinToString("|") { "${it.id},${it.x},${it.y}" }

        prefs.edit()
            .putString(KEY_POINTS, raw)
            .putLong(KEY_LAST_SAVED_AT, System.currentTimeMillis())
            .apply()

        bringAppToFront()
        stopSelf()
    }

    override fun onDestroy() {
        overlayView?.let {
            runCatching { windowManager.removeView(it) }
        }
        overlayView = null
        vmStore.clear()
        lifecycleDispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TapFlow Assist")
            .setContentText("Point setup is active")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Overlay Setup",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun bringAppToFront() {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
        }
        if (launchIntent != null) {
            startActivity(launchIntent)
        }
    }

    companion object {
        private const val CHANNEL_ID = "overlay_setup_channel"
        private const val NOTIFICATION_ID = 1001
        private const val PREFS_NAME = "overlay_setup_prefs"
        const val KEY_POINTS = "saved_points"
        const val KEY_LAST_SAVED_AT = "last_saved_at"
    }
}