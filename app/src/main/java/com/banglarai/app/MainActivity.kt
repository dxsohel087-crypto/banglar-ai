package com.banglarai.app

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
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
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private val green = Color.rgb(0, 255, 145)
    private val dark = Color.rgb(5, 15, 12)

    private lateinit var tts: TextToSpeech
    private var ttsReady = false
    private var recognizer: SpeechRecognizer? = null

    private lateinit var chatBox: LinearLayout
    private lateinit var chatScroll: ScrollView
    private lateinit var inputBox: EditText
    private lateinit var statusText: TextView

    private val permissionCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = dark
        window.navigationBarColor = dark

        tts = TextToSpeech(this, this)

        showIntro()
    }

    // প্রথম স্ক্রিন: আগের সবুজ-কালো পরিচিতি ডিজাইন
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
                    Color.rgb(0, 0, 0)
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

        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing) showHome()
        }, 2200)
    }

    // দ্বিতীয় স্ক্রিন: মূল AI অ্যাপ
    private fun showHome() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setBackgroundColor(Color.rgb(7, 17, 14))
        }

        // উপরের হেডার
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
                        "বাংলা ভয়েস এবং মাইক্রোফোন পরীক্ষা করুন। " +
                        "AI সার্ভার সংযোগ পরবর্তী ধাপে যোগ করা হবে।"
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

        // স্ট্যাটাস
        statusText = TextView(this).apply {
            text = "● প্রস্তুত"
            textSize = 13f
            setTextColor(green)
            setPadding(dp(4), dp(5), 0, dp(12))
        }
        root.addView(statusText)

        // চ্যাট অংশ
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

        addMessage(
            "আসসালামু আলাইকুম! আমি বাংলার AI। " +
            "আপনার কথা লিখে অথবা মাইক্রোফোনে বলে পরীক্ষা করতে পারেন।",
            false
        )

        // নিচের মেসেজ পাঠানোর অংশ
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

        val micButton = Button(this).apply {
            text = "🎙"
            textSize = 20f
            setTextColor(Color.WHITE)
            background = rounded(Color.rgb(20, 65, 43), 18)
            setOnClickListener {
                checkMicrophonePermission()
            }
        }

        bottom.addView(
            inputBox,
            LinearLayout.LayoutParams(0, dp(52), 1f)
        )

        bottom.addView(
            micButton,
            LinearLayout.LayoutParams(dp(55), dp(52)).apply {
                marginStart = dp(6)
            }
        )

        bottom.addView(
            sendButton,
            LinearLayout.LayoutParams(dp(55), dp(52)).apply {
                marginStart = dp(6)
            }
        )

        root.addView(bottom)

        setContentView(root)
    }

    // চ্যাটে মেসেজ যোগ করা
    private fun addMessage(message: String, isUser: Boolean) {
        val bubble = TextView(this).apply {
            text = message
            textSize = 15f
            setTextColor(Color.WHITE)
            setPadding(dp(14), dp(11), dp(14), dp(11))
            background = rounded(
                if (isUser) Color.rgb(17, 91, 57)
                else Color.rgb(25, 39, 32),
                17
            )
        }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = if (isUser) Gravity.END else Gravity.START
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

        if (::chatScroll.isInitialized) {
            chatScroll.post {
                chatScroll.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    // লেখা পাঠানো
    private fun sendMessage() {
        val message = inputBox.text.toString().trim()

        if (message.isEmpty()) {
            Toast.makeText(this, "আগে কিছু লিখুন", Toast.LENGTH_SHORT).show()
            return
        }

        inputBox.setText("")
        addMessage(message, true)
        giveTestReply()
    }

    // আপাতত পরীক্ষামূলক উত্তর
    private fun giveTestReply() {
        val reply =
            "আপনার কথাটি পেয়েছি। এখনো AI সার্ভারের সঙ্গে সংযোগ করা হয়নি। " +
            "পরবর্তী ধাপে আসল AI সংযোগ যোগ করব।"

        addMessage(reply, false)

        if (ttsReady) {
            tts.speak(reply, TextToSpeech.QUEUE_FLUSH, null, "banglar_ai_reply")
        }
    }

    // মাইক্রোফোন অনুমতি পরীক্ষা
    private fun checkMicrophonePermission() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                permissionCode
            )
        } else {
            startListening()
        }
    }

    // বাংলা কথা শোনা
    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(
                this,
                "এই ফোনে Speech Recognition পাওয়া যাচ্ছে না",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        try {
            if (recognizer == null) {
                recognizer = SpeechRecognizer.createSpeechRecognizer(this)
            }

            recognizer?.setRecognitionListener(object : RecognitionListener {

                override fun onReadyForSpeech(params: Bundle?) {
                    statusText.text = "● শুনছি..."
                }

                override fun onBeginningOfSpeech() {}

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    statusText.text = "● কথা বুঝছি..."
                }

                override fun onError(error: Int) {
                    statusText.text = "● প্রস্তুত"
                    Toast.makeText(
                        this@MainActivity,
                        "কথা শোনা যায়নি। আবার চেষ্টা করুন।",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onResults(results: Bundle?) {
                    statusText.text = "● প্রস্তুত"

                    val matches = results?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )

                    val spokenText = matches?.firstOrNull()

                    if (!spokenText.isNullOrBlank()) {
                        addMessage(spokenText, true)
                        giveTestReply()
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-BD")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            recognizer?.startListening(intent)

        } catch (e: Exception) {
            statusText.text = "● প্রস্তুত"
            Toast.makeText(
                this,
                "মাইক্রোফোন চালু করা যায়নি",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == permissionCode) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                startListening()
            } else {
                Toast.makeText(
                    this,
                    "মাইক্রোফোন অনুমতি প্রয়োজন",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // বাংলা Text-to-Speech
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale("bn", "BD"))
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    private fun rounded(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        recognizer?.destroy()
        recognizer = null

        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }

        super.onDestroy()
    }

    // সবুজ আলোকিত AI Orb
    private inner class OrbView : View(this@MainActivity) {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val cx = width / 2f
            val cy = height / 2f
            val radius = width.coerceAtMost(height) * 0.38f

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
                floatArrayOf(0f, 0.35f, 0.72f, 1f),
                Shader.TileMode.CLAMP
            )

            canvas.drawCircle(cx, cy, radius * 1.8f, paint)
            paint.shader = null

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp(2).toFloat()
            paint.color = green
            canvas.drawCircle(cx, cy, radius, paint)

            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(25, 180, 105)
            canvas.drawCircle(cx, cy, radius * 0.65f, paint)

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
