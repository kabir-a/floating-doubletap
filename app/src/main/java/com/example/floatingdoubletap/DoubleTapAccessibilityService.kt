package com.example.floatingdoubletap

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log

class DoubleTapAccessibilityService : AccessibilityService() {
    companion object {
        const val ACTION_DO_TAPS = "com.example.floatingdoubletap.DO_TAPS"
        const val EXTRA_X = "extra_x"
        const val EXTRA_Y = "extra_y"
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != ACTION_DO_TAPS) return
            val x = intent.getIntExtra(EXTRA_X, -1)
            val y = intent.getIntExtra(EXTRA_Y, -1)
            if (x >= 0 && y >= 0) {
                performTaps(x.toFloat(), y.toFloat())
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        registerReceiver(receiver, IntentFilter(ACTION_DO_TAPS))
        Log.d("DTService", "Accessibility service connected")
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun performSingleTap(x: Float, y: Float, callback: ((Boolean) -> Unit)? = null) {
        val path = Path().apply { moveTo(x, y) }
        val desc = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
        dispatchGesture(desc, object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                callback?.invoke(true)
            }
            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                callback?.invoke(false)
            }
        }, null)
    }

    private fun performTaps(x: Float, y: Float) {
        // Read preferences for tap count and interval
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val tapCount = prefs.getInt(MainActivity.PREF_TAP_COUNT, 2).coerceAtLeast(1)
        val interval = prefs.getInt(MainActivity.PREF_TAP_INTERVAL_MS, 80).coerceAtLeast(10)

        // perform taps sequentially with Handler
        val handler = Handler(Looper.getMainLooper())
        for (i in 0 until tapCount) {
            handler.postDelayed({
                performSingleTap(x, y, null)
            }, (i * interval).toLong())
        }
    }
}
