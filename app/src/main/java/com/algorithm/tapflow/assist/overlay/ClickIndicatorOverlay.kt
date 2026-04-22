package com.algorithm.tapflow.assist.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.algorithm.tapflow.assist.data.model.TouchPoint

class ClickIndicatorOverlay(
    private val context: Context
) {

    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var indicatorView: IndicatorView? = null
    private var isShown = false

    fun show() {
        if (isShown) return

        val view = IndicatorView(context)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        windowManager.addView(view, params)
        indicatorView = view
        isShown = true
    }

    fun hide() {
        val view = indicatorView ?: return
        if (!isShown) return
        windowManager.removeView(view)
        indicatorView = null
        isShown = false
    }

    fun setPersistentPoints(points: List<TouchPoint>) {
        indicatorView?.setPersistentPoints(points)
    }

    fun clearPersistentPoints() {
        indicatorView?.setPersistentPoints(emptyList())
    }

    fun pulse(x: Float, y: Float) {
        indicatorView?.pulse(x, y)
    }

    private class IndicatorView(context: Context) : View(context) {

        private val handler = Handler(Looper.getMainLooper())

        private val persistentOuterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(110, 0, 200, 255)
            style = Paint.Style.FILL
        }

        private val persistentInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(255, 255, 255, 255)
            style = Paint.Style.FILL
        }

        private val activeOuterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(140, 255, 59, 48)
            style = Paint.Style.FILL
        }

        private val activeInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(255, 255, 59, 48)
            style = Paint.Style.FILL
        }

        private data class PersistentPoint(
            val x: Float,
            val y: Float
        )

        private data class PulsePoint(
            val id: Long,
            val x: Float,
            val y: Float
        )

        private val persistentPoints = mutableListOf<PersistentPoint>()
        private val pulsePoints = mutableListOf<PulsePoint>()

        fun setPersistentPoints(points: List<TouchPoint>) {
            persistentPoints.clear()
            persistentPoints.addAll(
                points.map { PersistentPoint(it.x, it.y) }
            )
            invalidate()
        }

        fun pulse(x: Float, y: Float) {
            val item = PulsePoint(
                id = System.nanoTime(),
                x = x,
                y = y
            )
            pulsePoints.add(item)
            invalidate()

            handler.postDelayed({
                pulsePoints.removeAll { it.id == item.id }
                invalidate()
            }, 250L)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            persistentPoints.forEach { point ->
                canvas.drawCircle(point.x, point.y, 28f, persistentOuterPaint)
                canvas.drawCircle(point.x, point.y, 10f, persistentInnerPaint)
            }

            pulsePoints.forEach { point ->
                canvas.drawCircle(point.x, point.y, 42f, activeOuterPaint)
                canvas.drawCircle(point.x, point.y, 16f, activeInnerPaint)
            }
        }
    }
}