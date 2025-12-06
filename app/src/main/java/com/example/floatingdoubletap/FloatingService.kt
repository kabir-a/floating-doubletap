package com.example.floatingdoubletap

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.preference.PreferenceManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.NotificationCompat

class FloatingService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private lateinit var prefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createOverlay()
        startForegroundIfNeeded()
    }

    private fun startForegroundIfNeeded() {
        val channelId = "floating_doubletap_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Floating Button Service",
                NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Floating DoubleTap")
            .setContentText("Tap the button to send double-tap")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()

        startForeground(1, notification)
    }

    private fun createOverlay() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.floating_button_layout, null)

        val imageView = floatingView!!.findViewById<ImageView>(R.id.fab)

        val layoutType =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 200
        params.y = 400

        windowManager.addView(floatingView, params)

        // touch + drag + click detection
        var startX = 0
        var startY = 0
        var initialX = 0
        var initialY = 0

        imageView.setOnTouchListener { _, event ->
            val locked = prefs.getBoolean(MainActivity.PREF_OVERLAY_LOCKED, false)

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX.toInt()
                    startY = event.rawY.toInt()
                    initialX = params.x
                    initialY = params.y
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!locked) {
                        params.x = initialX + (event.rawX.toInt() - startX)
                        params.y = initialY + (event.rawY.toInt() - startY)
                        windowManager.updateViewLayout(floatingView, params)
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    val dx = event.rawX.toInt() - startX
                    val dy = event.rawY.toInt() - startY

                    // treat as click
                    if (kotlin.math.abs(dx) < 15 && kotlin.math.abs(dy) < 15) {
                        performClickAtCurrentLocation(params)
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun performClickAtCurrentLocation(params: WindowManager.LayoutParams) {
        val view = floatingView ?: return

        view.post {
            val screenPos = IntArray(2)
            view.getLocationOnScreen(screenPos)

            val cx = screenPos[0] + view.width / 2
            val cy = screenPos[1] + view.height / 2

            // send broadcast → AccessibilityService handles gesture
            val intent = Intent(DoubleTapAccessibilityService.ACTION_DO_TAPS)
            intent.putExtra(DoubleTapAccessibilityService.EXTRA_X, cx)
            intent.putExtra(DoubleTapAccessibilityService.EXTRA_Y, cy)
            sendBroadcast(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingView?.let { windowManager.removeView(it) }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}            .setContentTitle("FloatingDoubleTap")
            .setContentText("Tap the floating button to trigger double-tap")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()

        startForeground(1, notification)
    }

    private fun createOverlay() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.floating_button_layout, null)
        val imageView = floatingView!!.findViewById<ImageView>(R.id.fab)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 100
        params.y = 300

        windowManager.addView(floatingView, params)

        var lastX = 0
        var lastY = 0
        var startX = 0
        var startY = 0

        imageView.setOnTouchListener { _, event ->
            val locked = prefs.getBoolean(MainActivity.PREF_OVERLAY_LOCKED, false)

            if (locked) {
                if (event.action == MotionEvent.ACTION_UP) {
                    performClickAtCurrentLocation(params)
                }
                return@setOnTouchListener true
            }

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX.toInt()
                    startY = event.rawY.toInt()
                    lastX = params.x
                    lastY = params.y
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX.toInt() - startX
                    val dy = event.rawY.toInt() - startY
                    params.x = lastX + dx
                    params.y = lastY + dy
                    windowManager.updateViewLayout(floatingView, params)
                    true
                }

                MotionEvent.ACTION_UP -> {
                    val dx = event.rawX.toInt() - startX
                    val dy = event.rawY.toInt() - startY

                    if (kotlin.math.abs(dx) < 10 && kotlin.math.abs(dy) < 10) {
                        performClickAtCurrentLocation(params)
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun performClickAtCurrentLocation(params: WindowManager.LayoutParams) {
        val view = floatingView ?: return

        view.post {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            val cx = location[0] + view.width / 2
            val cy = location[1] + view.height / 2

            // Explicit broadcast → REQUIRED for Android 8+
            val intent = Intent(DoubleTapAccessibilityService.ACTION_DO_TAPS)
            intent.setClass(this, DoubleTapAccessibilityService::class.java)
            intent.putExtra(DoubleTapAccessibilityService.EXTRA_X, cx)
            intent.putExtra(DoubleTapAccessibilityService.EXTRA_Y, cy)

            sendBroadcast(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingView?.let { windowManager.removeView(it) }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
