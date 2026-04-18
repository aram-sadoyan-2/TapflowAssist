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
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.algorithm.tapflow.assist.R
import com.algorithm.tapflow.assist.data.model.GestureType

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

    private var controlPanelView: ComposeView? = null
    private var controlPanelParams: WindowManager.LayoutParams? = null

    private val pointViews = linkedMapOf<Long, ComposeView>()
    private val pointParams = linkedMapOf<Long, WindowManager.LayoutParams>()

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

        showControlPanel()
        syncPointViews()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleDispatcher.onServicePreSuperOnStart()
        return START_NOT_STICKY
    }

    private fun showControlPanel() {
        if (controlPanelView != null) return

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlaySetupService)
            setViewTreeViewModelStoreOwner(this@OverlaySetupService)
            setViewTreeSavedStateRegistryOwner(this@OverlaySetupService)

            setContent {
                val type by OverlaySetupSession.gestureType.collectAsState()

                DraggableControlPanelWindow(
                    showAddRemove = type != GestureType.SINGLE_TAP && type != GestureType.LONG_PRESS,
                    onAddPoint = {
                        OverlaySetupSession.addPoint()
                        syncPointViews()
                    },
                    onDeleteLast = {
                        OverlaySetupSession.deleteLastPoint()
                        syncPointViews()
                    },
                    onSave = { savePointsAndClose() },
                    onClose = {
                        bringAppToFront()
                        stopSelf()
                    },
                    onPanelMoved = { x, y ->
                        controlPanelParams?.let { params ->
                            params.x += x
                            params.y += y
                            controlPanelView?.let { v -> windowManager.updateViewLayout(v, params) }
                        }
                    }
                )
            }
        }

        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 120
        }

        controlPanelView = view
        controlPanelParams = params
        windowManager.addView(view, params)
    }

    private fun syncPointViews() {
        val currentPoints = OverlaySetupSession.points.value
        val currentIds = currentPoints.map { it.id }.toSet()

        val idsToRemove = pointViews.keys.filter { it !in currentIds }
        idsToRemove.forEach { id ->
            pointViews.remove(id)?.let { runCatching { windowManager.removeView(it) } }
            pointParams.remove(id)
        }

        currentPoints.forEachIndexed { index, point ->
            val existingView = pointViews[point.id]
            val type = OverlaySetupSession.gestureType.value
            val singlePointMode = type == GestureType.SINGLE_TAP || type == GestureType.LONG_PRESS

            if (existingView == null) {
                val view = ComposeView(this).apply {
                    setViewTreeLifecycleOwner(this@OverlaySetupService)
                    setViewTreeViewModelStoreOwner(this@OverlaySetupService)
                    setViewTreeSavedStateRegistryOwner(this@OverlaySetupService)

                    setContent {
                        PointWindow(
                            label = if (singlePointMode) null else "${index + 1}",
                            onMoved = { dx, dy ->
                                pointParams[point.id]?.let { params ->
                                    params.x += dx
                                    params.y += dy
                                    OverlaySetupSession.movePoint(point.id, params.x, params.y)
                                    windowManager.updateViewLayout(this, params)
                                }
                            }
                        )
                    }
                }

                val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }

                val params = WindowManager.LayoutParams(
                    dpToPx(46),
                    dpToPx(46),
                    windowType,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.START
                    x = point.x
                    y = point.y
                }

                pointViews[point.id] = view
                pointParams[point.id] = params
                windowManager.addView(view, params)
            } else {
                pointParams[point.id]?.let { params ->
                    params.x = point.x
                    params.y = point.y
                    runCatching { windowManager.updateViewLayout(existingView, params) }
                }

                existingView.setContent {
                    PointWindow(
                        label = if (singlePointMode) null else "${index + 1}",
                        onMoved = { dx, dy ->
                            pointParams[point.id]?.let { p ->
                                p.x += dx
                                p.y += dy
                                OverlaySetupSession.movePoint(point.id, p.x, p.y)
                                windowManager.updateViewLayout(existingView, p)
                            }
                        }
                    )
                }
            }
        }
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

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        controlPanelView?.let { runCatching { windowManager.removeView(it) } }
        controlPanelView = null
        controlPanelParams = null

        pointViews.values.forEach { runCatching { windowManager.removeView(it) } }
        pointViews.clear()
        pointParams.clear()

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

    companion object {
        private const val CHANNEL_ID = "overlay_setup_channel"
        private const val NOTIFICATION_ID = 1001
        private const val PREFS_NAME = "overlay_setup_prefs"
        const val KEY_POINTS = "saved_points"
        const val KEY_LAST_SAVED_AT = "last_saved_at"
    }
}