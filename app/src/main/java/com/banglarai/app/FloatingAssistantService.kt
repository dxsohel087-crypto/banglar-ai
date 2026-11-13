package com.banglarai.app

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

/** Small optional floating shortcut. It does not read other apps' screen content. */
class FloatingAssistantService : Service() {
    private var windowManager: WindowManager? = null
    private var bubble: TextView? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (bubble == null) showBubble()
        return START_NOT_STICKY
    }

    private fun showBubble() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val view = TextView(this).apply {
            text = "বাংলার AI"
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFF052018.toInt())
            gravity = Gravity.CENTER
            setPadding(24, 14, 24, 14)
            background = GradientDrawable().apply {
                setColor(0xFF00E676.toInt())
                cornerRadius = 48f
            }
            setOnClickListener {
                val launch = packageManager.getLaunchIntentForPackage(packageName)
                launch?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (launch != null) startActivity(launch)
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
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 20
            y = 180
        }
        windowManager?.addView(view, params)
        bubble = view
    }

    override fun onDestroy() {
        bubble?.let { view ->
            try { windowManager?.removeView(view) } catch (_: Exception) {}
        }
        bubble = null
        windowManager = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
