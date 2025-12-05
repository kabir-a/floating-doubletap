package com.example.floatingdoubletap

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.widget.Button
import android.widget.ToggleButton
import android.widget.EditText
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.inputmethod.EditorInfo
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var lockToggle: ToggleButton
    private lateinit var etTapCount: EditText
    private lateinit var etTapInterval: EditText
    private lateinit var prefs: SharedPreferences

    companion object {
        const val PREF_TAP_COUNT = "tap_count"
        const val PREF_TAP_INTERVAL_MS = "tap_interval_ms"
        const val PREF_OVERLAY_LOCKED = "overlay_locked"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        startButton = findViewById(R.id.btn_start)
        stopButton = findViewById(R.id.btn_stop)
        lockToggle = findViewById(R.id.toggle_lock)
        etTapCount = findViewById(R.id.et_tap_count)
        etTapInterval = findViewById(R.id.et_tap_interval)

        // Load saved settings
        lockToggle.isChecked = prefs.getBoolean(PREF_OVERLAY_LOCKED, false)
        etTapCount.setText(prefs.getInt(PREF_TAP_COUNT, 2).toString())
        etTapInterval.setText(prefs.getInt(PREF_TAP_INTERVAL_MS, 80).toString())

        lockToggle.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(PREF_OVERLAY_LOCKED, isChecked).apply()
        }

        startButton.setOnClickListener {
            // Save values before starting
            val count = etTapCount.text.toString().toIntOrNull() ?: 2
            val interval = etTapInterval.text.toString().toIntOrNull() ?: 80
            prefs.edit()
                .putInt(PREF_TAP_COUNT, count)
                .putInt(PREF_TAP_INTERVAL_MS, interval)
                .apply()

            // Request overlay permission if needed
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName"))
                startActivity(intent)
                showEnableAccessibilityDialog()
                Toast.makeText(this, "Please grant overlay & accessibility permissions then press Start again.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Ask user to enable accessibility if not enabled
            showEnableAccessibilityDialog()

            // Start foreground service that adds the floating button
            val svc = Intent(this, FloatingService::class.java)
            ContextCompat.startForegroundService(this, svc)
            Toast.makeText(this, "Floating button started (if Accessibility is enabled).", Toast.LENGTH_SHORT).show()
        }

        stopButton.setOnClickListener {
            val svc = Intent(this, FloatingService::class.java)
            stopService(svc)
            Toast.makeText(this, "Floating button stopped.", Toast.LENGTH_SHORT).show()
        }

        // Save on keyboard "done"
        etTapCount.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val count = etTapCount.text.toString().toIntOrNull() ?: 2
                prefs.edit().putInt(PREF_TAP_COUNT, count).apply()
            }
            false
        }
        etTapInterval.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val interval = etTapInterval.text.toString().toIntOrNull() ?: 80
                prefs.edit().putInt(PREF_TAP_INTERVAL_MS, interval).apply()
            }
            false
        }
    }

    private fun showEnableAccessibilityDialog() {
        // Ask the user to enable Accessibility service
        AlertDialog.Builder(this)
            .setTitle("Enable Accessibility")
            .setMessage("To perform touches in other apps, please enable the Accessibility Service for this app in Settings → Accessibility.")
            .setPositiveButton("Open Accessibility Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
