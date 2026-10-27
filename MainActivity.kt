package uz.stepuz.app

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private lateinit var root: LinearLayout
    private lateinit var stepsText: TextView
    private lateinit var statusText: TextView
    private lateinit var startButton: Button
    private var started = false
    private var baseline = -1f
    private var currentSteps = 0
    private var sensorAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(7, 27, 54)
        window.navigationBarColor = android.graphics.Color.rgb(7, 27, 54)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        sensorAvailable = stepSensor != null
        showSplash()
    }

    private fun showSplash() {
        val splash = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 28, 28, 28)
            setBackgroundColor(android.graphics.Color.rgb(7, 27, 54))
        }
        val logo = TextView(this).apply {
            text = "👟"
            textSize = 66f
            gravity = Gravity.CENTER
        }
        val name = TextView(this).apply {
            text = "STEPUZ"
            textSize = 36f
            setTextColor(android.graphics.Color.WHITE)
            gravity = Gravity.CENTER
            letterSpacing = 0.18f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        val tagline = TextView(this).apply {
            text = "Har qadam muhim"
            textSize = 16f
            setTextColor(0xFF75D6B0.toInt())
            gravity = Gravity.CENTER
        }
        splash.addView(logo)
        splash.addView(name)
        splash.addView(tagline)
        setContentView(splash)
        val anim = ScaleAnimation(0.65f, 1f, 0.65f, 1f, 1, .5f, 1, .5f).apply { duration = 900 }
        logo.startAnimation(anim)
        name.startAnimation(AlphaAnimation(0f, 1f).apply { duration = 1100 })
        splash.postDelayed({ showPermissionsThenHome() }, 1500)
    }

    private fun showPermissionsThenHome() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 100)
            return
        }
        askNotifications()
    }

    private fun askNotifications() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            return
        }
        askBackgroundRun()
    }

    private fun askBackgroundRun() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                // This is a system settings screen, not a normal runtime permission dialog.
                try {
                    val i = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    i.data = Uri.parse("package:$packageName")
                    startActivityForResult(i, 102)
                    return
                } catch (_: Exception) {
                    // Some manufacturers do not support the direct exemption screen.
                }
            }
        }
        showHome()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            100 -> askNotifications()
            101 -> askBackgroundRun()
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 102) showHome()
    }

    private fun showHome() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(android.graphics.Color.rgb(7, 27, 54))
        }
        val header = TextView(this).apply {
            text = "STEPUZ"
            textSize = 25f
            setTextColor(0xFF36C78A.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        val sub = TextView(this).apply {
            text = "Bugungi faolliging"
            textSize = 16f
            setTextColor(0xFFB7C8E5.toInt())
            gravity = Gravity.CENTER
        }
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(18, 36, 18, 30)
            setBackgroundColor(0xFF18345E.toInt())
        }
        stepsText = TextView(this).apply {
            text = "0"
            textSize = 68f
            setTextColor(android.graphics.Color.WHITE)
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        val stepLabel = TextView(this).apply {
            text = "QADAM"
            textSize = 15f
            setTextColor(0xFF9CB4D8.toInt())
            gravity = Gravity.CENTER
        }
        statusText = TextView(this).apply {
            text = if (sensorAvailable) "Tayyor. Boshlashni bosing." else "Telefoningizda qadam sensori topilmadi."
            textSize = 14f
            setTextColor(if (sensorAvailable) 0xFF75D6B0.toInt() else 0xFFFFB4A9.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 10)
        }
        startButton = Button(this).apply {
            text = "BOSHLASH"
            textSize = 17f
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF24B879.toInt()))
            setOnClickListener { toggleTracking() }
        }
        val resetButton = Button(this).apply {
            text = "Hisobni 0 dan boshlash"
            setOnClickListener {
                currentSteps = 0
                baseline = -1f
                stepsText.text = "0"
                statusText.text = if (started) "Hisob qayta boshlandi. Yurishni davom eting." else "Hisob 0 ga qaytarildi."
            }
        }
        val note = TextView(this).apply {
            text = "Qadamlar telefonning ichki sensori orqali sanaladi. Natija telefon modeli va sensor aniqligiga bog‘liq."
            textSize = 12f
            setTextColor(0xFF9CB4D8.toInt())
            gravity = Gravity.CENTER
            setPadding(8, 18, 8, 0)
        }
        card.addView(stepsText)
        card.addView(stepLabel)
        card.addView(statusText)
        root.addView(header)
        root.addView(sub)
        root.addView(card, LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = 24; bottomMargin = 18 })
        root.addView(startButton, LinearLayout.LayoutParams(-1, -2))
        root.addView(resetButton, LinearLayout.LayoutParams(-1, -2))
        root.addView(note)
        setContentView(root)
        if (!sensorAvailable) startButton.isEnabled = false
    }

    private fun toggleTracking() {
        if (!sensorAvailable) return
        if (!started) {
            val ok = sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
            if (!ok) {
                statusText.text = "Sensorni ishga tushirib bo‘lmadi."
                return
            }
            started = true
            startButton.text = "TO‘XTATISH"
            statusText.text = "Sanayapti — telefonni yoningizda olib yuring."
        } else {
            sensorManager.unregisterListener(this)
            started = false
            startButton.text = "BOSHLASH"
            statusText.text = "To‘xtatildi. Jami: $currentSteps qadam."
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!started || event == null) return
        val sensor = stepSensor ?: return
        if (sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSinceBoot = event.values[0]
            if (baseline < 0f) {
                baseline = totalSinceBoot
                statusText.text = "Sanayapti — bir necha qadam yuring."
            }
            currentSteps = (totalSinceBoot - baseline).toInt().coerceAtLeast(0)
        } else if (sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            currentSteps += event.values[0].toInt().coerceAtLeast(1)
        }
        stepsText.text = currentSteps.toString()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onPause() {
        super.onPause()
        // Sensor registration is kept while this Activity is paused, but Android may suspend
        // the process. This prototype does not yet implement a foreground service.
    }
    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }
}
