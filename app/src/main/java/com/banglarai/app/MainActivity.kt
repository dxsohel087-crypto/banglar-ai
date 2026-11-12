package com.banglarai.app

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val green = Color.rgb(0, 255, 145)
    private val dark = Color.rgb(5, 15, 12)

    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var chatBox: LinearLayout
    private lateinit var chatScroll: ScrollView
    private lateinit var inputBox: EditText

    private val introRunnable = Runnable {
        if (!isFinishing && !isDestroyed) {
            showHome()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = dark
        window.navigationBarColor = dark

        showIntro()
    }

    // INTRO SCREEN
    private fun showIntro() {

        val screen = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(20), dp(20), dp(20))

            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(3, 28, 18),
                    Color.rgb(3, 10, 9),
                    Color.BLACK
                )
            )
        }

        val orb = OrbView()

        screen.addView(
            orb,
            LinearLayout.LayoutParams(dp(190), dp(190))
        )

        val title = TextView(this).apply {
            text = "বাংলার AI"
            textSize = 34f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(green)
            gravity = Gravity.CENTER
            setPadding(0, dp(20), 0, dp(8))
        }

        val subtitle = TextView(this).apply {
            text = "আপনার বাংলা AI সহকারী"
            textSize = 16f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }

        val loading = TextView(this).apply {
            text = "প্রস্তুত হচ্ছে..."
            textSize = 13f
            setTextColor(green)
            gravity = Gravity.CENTER
            setPadding(0, dp(28), 0, 0)
        }

        screen.addView(title)
        screen.addView(subtitle)
        screen.addView(loading)

        setContentView(screen)

        mainHandler.removeCallbacks(introRunnable)
        mainHandler.postDelayed(introRunnable, 2200)
    }

    // MAIN CHAT SCREEN
    private fun showHome() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setBackgroundColor(Color.rgb(7, 17, 14))
        }

        // HEADER
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val heading = TextView(this).apply {
            text = "বাংলার AI"
            textSize = 25f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(green)
        }

        val settings = Button(this).apply {
            text = "⚙"
            textSize = 21f
            setTextColor(green)
            background = rounded(Color.rgb(18, 38, 29), 14)

            setOnClickListener {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("বাংলার AI সেটিংস")
                    .setMessage(
                        "বাংলার AI পরীক্ষামূলক সংস্করণ।\n\n" +
                        "পরবর্তী ধাপে মাইক্রোফোন ও AI সার্ভার যুক্ত করা হবে।"
                    )
                    .setPositiveButton("ঠিক আছে", null)
                    .show()
            }
        }

        header.addView(
            heading,
            LinearLayout.LayoutParams(0, dp(52), 1f)
        )

        header.addView(
            settings,
            LinearLayout.LayoutParams(dp(52), dp(48))
        )

        root.addView(header)

        // STATUS
        val status = TextView(this).apply {
            text = "● প্রস্তুত"
            textSize = 13f
            setTextColor(green)
            setPadding(dp(4), dp(5), 0, dp(12))
        }

        root.addView(status)

        // CHAT AREA
        chatScroll = ScrollView(this).apply {
            isFillViewport = true
        }

        chatBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(3), dp(8), dp(3), dp(12))
        }

        chatScroll.addView(chatBox)

        root.addView(
            chatScroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        // WELCOME MESSAGE
        addMessage(
            "আসসালামু আলাইকুম! আমি বাংলার AI। " +
            "আপনাকে স্বাগতম। আপনি এখানে লিখে কথা বলতে পারেন।",
            false
        )

        // INPUT AREA
        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(10), 0, dp(4))
        }

        inputBox = EditText(this).apply {
            hint = "এখানে লিখুন..."
            textSize = 15f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            singleLine = true
            setPadding(dp(14), 0, dp(10), 0)
            background = rounded(Color.rgb(24, 39, 32), 18)
        }

        val sendButton = Button(this).apply {
            text = "➤"
            textSize = 20f
            setTextColor(Color.BLACK)
            background = rounded(green, 18)

            setOnClickListener {
                sendMessage()
            }
        }

        bottom.addView(
            inputBox,
            LinearLayout.LayoutParams(0, dp(52), 1f)
        )

        bottom.addView(
            sendButton,
            LinearLayout.LayoutParams(dp(58), dp(52)).apply {
                marginStart = dp(7)
            }
        )

        root.addView(bottom)

        setContentView(root)
    }

    // ADD MESSAGE
    private fun addMessage(message: String, isUser: Boolean) {

        val bubble = TextView(this).apply {
            text = message
            textSize = 15f
            setTextColor(Color.WHITE)
            setPadding(dp(14), dp(11), dp(14), dp(11))

            background = rounded(
                if (isUser) {
                    Color.rgb(17, 91, 57)
                } else {
                    Color.rgb(25, 39, 32)
                },
                17
            )
        }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL

            gravity = if (isUser) {
                Gravity.END
            } else {
                Gravity.START
            }

            setPadding(0, dp(5), 0, dp(5))

            addView(
                bubble,
                LinearLayout.LayoutParams(
                    (resources.displayMetrics.widthPixels * 0.78f).toInt(),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }

        chatBox.addView(row)

        chatScroll.post {
            chatScroll.fullScroll(View.FOCUS_DOWN)
        }
    }

    // SEND MESSAGE
    private fun sendMessage() {

        val message = inputBox.text.toString().trim()

        if (message.isEmpty()) {
            Toast.makeText(
                this,
                "আগে কিছু লিখুন",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        inputBox.setText("")

        addMessage(message, true)

        giveTestReply()
    }

    // TEMPORARY AI REPLY
    private fun giveTestReply() {

        val reply =
            "আপনার কথাটি পেয়েছি। এটি বাংলার AI-এর পরীক্ষামূলক সংস্করণ। " +
            "পরবর্তী ধাপে আসল AI সার্ভার সংযোগ করা হবে।"

        addMessage(reply, false)
    }

    // ROUNDED BACKGROUND
    private fun rounded(
        color: Int,
        radius: Int
    ): GradientDrawable {

        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    // DP CONVERTER
    private fun dp(value: Int): Int {
        return (
            value * resources.displayMetrics.density
        ).toInt()
    }

    // ACTIVITY DESTROY
    override fun onDestroy() {
        mainHandler.removeCallbacks(introRunnable)
        super.onDestroy()
    }

    // GREEN AI ORB
    private inner class OrbView : View(this@MainActivity) {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val cx = width / 2f
            val cy = height / 2f

            val radius =
                width.coerceAtMost(height) * 0.38f

            paint.shader = RadialGradient(
                cx,
                cy,
                radius * 1.8f,
                intArrayOf(
                    Color.rgb(100, 255, 190),
                    Color.rgb(0, 190, 105),
                    Color.rgb(0, 55, 35),
                    Color.TRANSPARENT
                ),
                floatArrayOf(
                    0f,
                    0.35f,
                    0.72f,
                    1f
                ),
                Shader.TileMode.CLAMP
            )

            canvas.drawCircle(
                cx,
                cy,
                radius * 1.8f,
                paint
            )

            paint.shader = null

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp(2).toFloat()
            paint.color = green

            canvas.drawCircle(
                cx,
                cy,
                radius,
                paint
            )

            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(25, 180, 105)

            canvas.drawCircle(
                cx,
                cy,
                radius * 0.65f,
                paint
            )

            paint.color = Color.WHITE

            canvas.drawCircle(
                cx - radius * 0.18f,
                cy - radius * 0.18f,
                radius * 0.12f,
                paint
            )
        }
    }
}
