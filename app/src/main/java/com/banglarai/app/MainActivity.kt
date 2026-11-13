package com.banglarai.app

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    companion object {
         private const val OVERLAY_PERMISSION = 401
        private const val MODEL = "openai/gpt-oss-120b"
        private const val API_URL =
            "https://api.groq.com/openai/v1/chat/completions"

        private const val MIC_PERMISSION = 101
        private const val CAMERA_PERMISSION = 102
    }

    private val green = Color.rgb(0, 230, 118)
    private val dark = Color.rgb(5, 13, 12)
    private val cardColor = Color.rgb(15, 31, 26)
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var root: LinearLayout
    private lateinit var chatLayout: LinearLayout
    private lateinit var chatScroll: ScrollView
    private lateinit var inputBox: EditText
    private lateinit var statusText: TextView
    private lateinit var powerButton: Button

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var recognizer: SpeechRecognizer? = null
    private var voiceEnabled = false
    private var listening = false
    private var busy = false
    private var torchOn = false

    private val conversation = mutableListOf<Pair<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = dark
        window.navigationBarColor = dark

        tts = TextToSpeech(this, this)
        showIntro()
    }

    // INTRO SCREEN

    private fun showIntro() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(24), dp(24), dp(24))
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.rgb(0, 55, 35), dark)
            )
        }

        val symbol = TextView(this).apply {
            text = "✦"
            textSize = 82f
            gravity = Gravity.CENTER
            setTextColor(green)
        }

        val title = TextView(this).apply {
            text = "বাংলার AI"
            textSize = 34f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        }

        val subtitle = TextView(this).apply {
            text = "FUTURISTIC PERSONAL ASSISTANT"
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(green)
        }

        val version = TextView(this).apply {
            text = "GROQ AI  •  বাংলা ভয়েস অ্যাসিস্ট্যান্ট"
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
        }

        layout.addView(symbol)
        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(version)

        setContentView(layout)

        handler.postDelayed({
            showHome()
        }, 1800)
    }

    // MAIN SCREEN

    private fun showHome() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(8))
            setBackgroundColor(dark)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val heading = TextView(this).apply {
            text = "✦ বাংলার AI"
            textSize = 23f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(green)
        }

        val settingsButton = makeButton("⚙")
        settingsButton.setOnClickListener { showSettings() }

        header.addView(
            heading,
            LinearLayout.LayoutParams(0, dp(48), 1f)
        )
        header.addView(
            settingsButton,
            LinearLayout.LayoutParams(dp(52), dp(44))
        )

        statusText = TextView(this).apply {
            text = "● AI প্রস্তুত"
            textSize = 12f
            setTextColor(green)
            setPadding(dp(3), dp(3), dp(3), dp(5))
        }

        val orb = TextView(this).apply {
            text = "◉"
            textSize = 48f
            gravity = Gravity.CENTER
            setTextColor(green)
            setPadding(0, dp(2), 0, dp(2))
        }

        powerButton = makeButton("AI চালু করুন")
        powerButton.setOnClickListener { toggleVoice() }

        chatScroll = ScrollView(this).apply {
            isFillViewport = true
            background = rounded(cardColor, 16)
            setPadding(dp(9), dp(9), dp(9), dp(9))
        }

        chatLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        chatScroll.addView(chatLayout)

        val inputRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, dp(5))
        }

        inputBox = EditText(this).apply {
            hint = "এখানে লিখুন..."
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            textSize = 15f
            isSingleLine = true
            setPadding(dp(12), 0, dp(8), 0)
            background = rounded(cardColor, 13)
        }

        val micButton = makeButton("🎙")
        micButton.setOnClickListener {
            if (listening) {
                stopListening()
                statusText.text = "● AI প্রস্তুত"
            } else {
                checkMicPermissionAndListen()
            }
        }

        val sendButton = makeButton("➤")
        sendButton.setTextColor(dark)
        sendButton.background = rounded(green, 13)
        sendButton.setOnClickListener { sendMessage() }

        inputRow.addView(
            inputBox,
            LinearLayout.LayoutParams(0, dp(49), 1f)
        )
        inputRow.addView(
            micButton,
            LinearLayout.LayoutParams(dp(49), dp(49))
        )
        inputRow.addView(
            sendButton,
            LinearLayout.LayoutParams(dp(49), dp(49))
        )

        val controls1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val controls2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val controls3 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        addControl(controls1, "Wi-Fi") {
            openSettings(Settings.ACTION_WIFI_SETTINGS)
        }
        addControl(controls1, "Bluetooth") {
            openSettings(Settings.ACTION_BLUETOOTH_SETTINGS)
        }
        addControl(controls1, "Flashlight") {
            toggleFlashlight()
        }

        addControl(controls2, "Camera") { openCamera() }
        addControl(controls2, "YouTube") {
            openUrl("https://www.youtube.com")
        }
        addControl(controls2, "Settings") {
            openSettings(Settings.ACTION_SETTINGS)
        }

        addControl(controls3, "Facebook") {
            openUrl("https://www.facebook.com")
        }
        addControl(controls3, "Gallery") { openGallery() }
        addControl(controls3, "Brightness") {
            openSettings(Settings.ACTION_DISPLAY_SETTINGS)
        }

        root.addView(header)
        root.addView(statusText)
        root.addView(orb)
        root.addView(powerButton)
        root.addView(sectionTitle("AI CHAT"))

        root.addView(
            chatScroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        root.addView(inputRow)
        root.addView(sectionTitle("SMART MOBILE CONTROL"))
        root.addView(controls1)
        root.addView(controls2)
        root.addView(controls3)

        setContentView(root)

        restoreConversation()
        if (conversation.isEmpty()) {
            addMessage(false, "আসসালামু আলাইকুম! আমি বাংলার AI। আমাকে যেকোনো প্রশ্ন করতে পারেন।")
        }
    }

    // DESIGN HELPERS

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun rounded(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun makeButton(label: String): Button {
        return Button(this).apply {
            text = label
            textSize = 13f
            setTextColor(green)
            isAllCaps = false
            background = rounded(cardColor, 13)
            minHeight = 0
            minimumHeight = 0
            minWidth = 0
            minimumWidth = 0
            setPadding(dp(4), dp(2), dp(4), dp(2))
        }
    }

    private fun sectionTitle(label: String): TextView {
        return TextView(this).apply {
            text = label
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(green)
            setPadding(dp(3), dp(7), dp(3), dp(5))
        }
    }

    private fun addControl(
        row: LinearLayout,
        label: String,
        action: () -> Unit
    ) {
        val button = makeButton(label)
        button.textSize = 11f
        button.setOnClickListener { action() }

        val params = LinearLayout.LayoutParams(
            0,
            dp(39),
            1f
        )
        params.setMargins(dp(3), dp(2), dp(3), dp(2))
        row.addView(button, params)
    }

    // CHAT MESSAGES

    private fun addMessage(isUser: Boolean, message: String) {
        val bubble = TextView(this).apply {
            text = (if (isUser) "আপনি: " else "বাংলার AI: ") + message
            textSize = 14f
            setTextColor(
                if (isUser) Color.WHITE else Color.rgb(210, 255, 225)
            )
            setPadding(dp(11), dp(9), dp(11), dp(9))
            background = rounded(
                if (isUser) Color.rgb(24, 68, 48)
                else Color.rgb(25, 44, 37),
                12
            )
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, dp(4), 0, dp(4))

        chatLayout.addView(bubble, params)
        chatScroll.post {
            chatScroll.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun sendMessage() {
        val message = inputBox.text.toString().trim()
        if (message.isEmpty()) return

        if (busy) {
            Toast.makeText(this, "একটু অপেক্ষা করুন", Toast.LENGTH_SHORT).show()
            return
        }

        inputBox.setText("")
        addMessage(true, message)
        conversation.add(Pair("user", message))
        saveConversation()
        askGroq()
    }

    // GROQ AI

    private fun askGroq() {
        val apiKey = ApiKeyStore.get(this)
        if (apiKey.isBlank()) {
            addMessage(
                false,
                "API Key বসানো হয়নি। কোডের API_KEY জায়গায় আপনার Groq API Key বসান।"
            )
            return
        }

        busy = true
        statusText.text = "● AI চিন্তা করছে..."
        statusText.setTextColor(Color.YELLOW)

        Thread {
            var answer: String

            try {
                val connection =
                    URL(API_URL).openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.connectTimeout = 20000
                connection.readTimeout = 30000
                connection.doOutput = true

                connection.setRequestProperty(
                    "Authorization",
                    "Bearer $apiKey"
                )
                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )

                val messages = JSONArray()

                messages.put(
                    JSONObject()
                        .put("role", "system")
                        .put(
                            "content",
                            "তুমি বাংলার AI। সহজ, পরিষ্কার ও স্বাভাবিক বাংলায় উত্তর দেবে। " +
                                "প্রশ্ন অনুযায়ী প্রয়োজনীয় তথ্য বুঝিয়ে বলবে।"
                        )
                )

                for (item in conversation.takeLast(16)) {
                    messages.put(
                        JSONObject()
                            .put("role", item.first)
                            .put("content", item.second)
                    )
                }

                val body = JSONObject()
                    .put("model", MODEL)
                    .put("messages", messages)
                    .put("max_completion_tokens", 1200)

                connection.outputStream.use { output ->
                    output.write(body.toString().toByteArray(Charsets.UTF_8))
                }

                val code = connection.responseCode

                val response = if (code in 200..299) {
                    connection.inputStream.bufferedReader().use {
                        it.readText()
                    }
                } else {
                    connection.errorStream?.bufferedReader()?.use {
                        it.readText()
                    } ?: "HTTP $code"
                }

                if (code in 200..299) {
                    val json = JSONObject(response)
                    answer = json.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                        .trim()
                } else {
                    answer = "সার্ভার সমস্যা ($code)। API Key বা মডেল পরীক্ষা করুন।"
                }

                connection.disconnect()

            } catch (e: Exception) {
                answer = "AI সার্ভারে সংযোগ করা যায়নি। ইন্টারনেট পরীক্ষা করুন।"
            }

            runOnUiThread {
                busy = false
                statusText.text = "● AI প্রস্তুত"
                statusText.setTextColor(green)

                addMessage(false, answer)
                conversation.add(Pair("assistant", answer))
                saveConversation()
                speak(answer)
            }
        }.start()
    }

    // TEXT TO SPEECH

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("bn", "BD"))

            ttsReady =
                result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED

            if (!ttsReady) {
                val fallback = tts?.setLanguage(Locale("bn", "IN"))
                ttsReady =
                    fallback != TextToSpeech.LANG_MISSING_DATA &&
                    fallback != TextToSpeech.LANG_NOT_SUPPORTED
            }

            tts?.setSpeechRate(0.90f)

            tts?.setOnUtteranceProgressListener(
                object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}

                    override fun onDone(utteranceId: String?) {
                        handler.post {
                            if (voiceEnabled && !busy) {
                                handler.postDelayed({
                                    startListening()
                                }, 600)
                            }
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {}
                }
            )
        }
    }

    private fun speak(message: String) {
        if (ttsReady) {
            tts?.speak(
                message,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "banglar_ai_reply"
            )
        }
    }

    // VOICE INPUT

    private fun toggleVoice() {
        voiceEnabled = !voiceEnabled

        if (voiceEnabled) {
            powerButton.text = "AI বন্ধ করুন"
            statusText.text = "● ভয়েস চালু"
            checkMicPermissionAndListen()
        } else {
            powerButton.text = "AI চালু করুন"
            statusText.text = "● AI প্রস্তুত"
            stopListening()
            tts?.stop()
        }
    }

    private fun checkMicPermissionAndListen() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                MIC_PERMISSION
            )
        } else {
            startListening()
        }
    }

    private fun startListening() {
        if (busy || listening) return

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(
                this,
                "Speech Recognition পাওয়া যায়নি",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        try {
            if (recognizer == null) {
                recognizer = SpeechRecognizer.createSpeechRecognizer(this)

                recognizer?.setRecognitionListener(
                    object : RecognitionListener {

                        override fun onReadyForSpeech(params: Bundle?) {
                            listening = true
                            statusText.text = "● শুনছি..."
                            statusText.setTextColor(Color.rgb(255, 170, 40))
                        }

                        override fun onBeginningOfSpeech() {}
                        override fun onRmsChanged(rmsdB: Float) {}
                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            listening = false
                        }

                        override fun onError(error: Int) {
                            listening = false
                            if (voiceEnabled && !busy) {
                                handler.postDelayed({
                                    startListening()
                                }, 1200)
                            }
                        }

                        override fun onResults(results: Bundle?) {
                            listening = false

                            val words = results?.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION
                            )

                            val spoken = words?.firstOrNull()?.trim()

                            if (!spoken.isNullOrEmpty()) {
                                inputBox.setText(spoken)
                                sendMessage()
                            } else if (voiceEnabled) {
                                handler.postDelayed({
                                    startListening()
                                }, 700)
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {}
                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    }
                )
            }

            val intent = Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            ).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-BD")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            }

            recognizer?.startListening(intent)

        } catch (e: Exception) {
            listening = false
            Toast.makeText(
                this,
                "মাইক্রোফোন চালু করা যায়নি",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun stopListening() {
        listening = false
        try {
            recognizer?.cancel()
        } catch (_: Exception) {
        }
    }

    // PHONE CONTROLS

    private fun openSettings(action: String) {
        try {
            startActivity(Intent(action))
        } catch (_: Exception) {
            Toast.makeText(this, "সেটিংস খোলা যাচ্ছে না", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openUrl(url: String) {
        try {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
            )
        } catch (_: Exception) {
            Toast.makeText(this, "লিংক খোলা যাচ্ছে না", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCamera() {
        try {
            startActivity(
                Intent("android.media.action.IMAGE_CAPTURE")
            )
        } catch (_: Exception) {
            Toast.makeText(this, "ক্যামেরা পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        try {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            startActivityForResult(intent, 300)
        } catch (_: Exception) {
            Toast.makeText(this, "গ্যালারি খোলা যাচ্ছে না", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleFlashlight() {
        if (checkSelfPermission(Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION
            )
            return
        }

        try {
            val manager = getSystemService(CAMERA_SERVICE) as CameraManager

            val cameraId = manager.cameraIdList.firstOrNull { id ->
                manager.getCameraCharacteristics(id).get(
                    CameraCharacteristics.FLASH_INFO_AVAILABLE
                ) == true
            }

            if (cameraId == null) {
                Toast.makeText(this, "ফ্ল্যাশলাইট পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                return
            }

            torchOn = !torchOn
            manager.setTorchMode(cameraId, torchOn)

            Toast.makeText(
                this,
                if (torchOn) "ফ্ল্যাশলাইট চালু" else "ফ্ল্যাশলাইট বন্ধ",
                Toast.LENGTH_SHORT
            ).show()

        } catch (_: Exception) {
            torchOn = false
            Toast.makeText(
                this,
                "ফ্ল্যাশলাইট চালু করা যায়নি",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // SETTINGS AND CLEAR CHAT

    private fun showSettings() {
        val options = arrayOf(
            "চ্যাট মুছে ফেলুন",
            "ভয়েস চালু / বন্ধ",
            "API Key সেট করুন",
            "ফ্লোটিং AI চালু / বন্ধ",
            "ফোনের Settings"
        )

        AlertDialog.Builder(this)
            .setTitle("বাংলার AI সেটিংস")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> clearChat()
                    1 -> toggleVoice()
                    2 -> showApiKeyDialog()
                    3 -> toggleFloatingAssistant()
                    4 -> openSettings(Settings.ACTION_SETTINGS)
                }
            }
            .setNegativeButton("বন্ধ", null)
            .show()
    }

    private fun clearChat() {
        chatLayout.removeAllViews()
        conversation.clear()
        getSharedPreferences("banglar_ai", MODE_PRIVATE).edit().remove("conversation").apply()
        addMessage(false, "চ্যাট মুছে ফেলা হয়েছে। আবার কথা শুরু করুন।")
    }

    private fun saveConversation() {
        val array = JSONArray()
        conversation.takeLast(80).forEach { item ->
            array.put(JSONObject().put("role", item.first).put("text", item.second))
        }
        getSharedPreferences("banglar_ai", MODE_PRIVATE).edit()
            .putString("conversation", array.toString()).apply()
    }

    private fun restoreConversation() {
        val saved = getSharedPreferences("banglar_ai", MODE_PRIVATE)
            .getString("conversation", null) ?: return
        try {
            val array = JSONArray(saved)
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val role = item.getString("role")
                val message = item.getString("text")
                conversation.add(Pair(role, message))
                addMessage(role == "user", message)
            }
        } catch (_: Exception) {
            conversation.clear()
            getSharedPreferences("banglar_ai", MODE_PRIVATE).edit().remove("conversation").apply()
        }
    }

    private fun showApiKeyDialog() {
        val field = EditText(this).apply {
            hint = "Groq API Key"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setTextColor(Color.WHITE)
        }
        val container = LinearLayout(this).apply {
            setPadding(dp(20), dp(8), dp(20), dp(4))
            addView(field)
        }
        AlertDialog.Builder(this)
            .setTitle("API Key সেট করুন")
            .setMessage("API Key অ্যাপের কোডে রাখা হবে না; এই ডিভাইসে এনক্রিপ্ট করে রাখা হবে।")
            .setView(container)
            .setPositiveButton("সংরক্ষণ") { _, _ ->
                val key = field.text.toString().trim()
                if (key.isNotEmpty()) {
                    ApiKeyStore.save(this, key)
                    Toast.makeText(this, "API Key সংরক্ষণ করা হয়েছে", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("বাতিল", null)
            .show()
    }

    private fun toggleFloatingAssistant() {
        val prefs = getSharedPreferences("banglar_ai", MODE_PRIVATE)
        val enabled = prefs.getBoolean("floating_enabled", false)
        if (enabled) {
            stopService(Intent(this, FloatingAssistantService::class.java))
            prefs.edit().putBoolean("floating_enabled", false).apply()
            Toast.makeText(this, "ফ্লোটিং AI বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
            return
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&
            !Settings.canDrawOverlays(this)
        ) {
            startActivityForResult(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")),
                OVERLAY_PERMISSION
            )
            return
        }
        startService(Intent(this, FloatingAssistantService::class.java))
        prefs.edit().putBoolean("floating_enabled", true).apply()
        Toast.makeText(this, "ফ্লোটিং AI চালু হয়েছে", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION) {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M ||
                Settings.canDrawOverlays(this)
            ) {
                startService(Intent(this, FloatingAssistantService::class.java))
                getSharedPreferences("banglar_ai", MODE_PRIVATE).edit()
                    .putBoolean("floating_enabled", true).apply()
                Toast.makeText(this, "ফ্লোটিং AI চালু হয়েছে", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "ফ্লোটিং AI চালাতে Overlay অনুমতি প্রয়োজন", Toast.LENGTH_LONG).show()
            }
        }
    }

    // PERMISSIONS

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

        if (requestCode == MIC_PERMISSION) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                startListening()
            } else {
                voiceEnabled = false
                powerButton.text = "AI চালু করুন"
                Toast.makeText(
                    this,
                    "মাইক্রোফোন অনুমতি প্রয়োজন",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        if (requestCode == CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                toggleFlashlight()
            } else {
                Toast.makeText(
                    this,
                    "ক্যামেরা অনুমতি প্রয়োজন",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // APP LIFECYCLE

    override fun onPause() {
        super.onPause()
        stopListening()
        tts?.stop()
    }

    override fun onResume() {
        super.onResume()
        if (voiceEnabled && !busy) {
            handler.postDelayed({
                if (!isFinishing && voiceEnabled && !busy) startListening()
            }, 500)
        }
    }

    override fun onDestroy() {
        stopListening()
        recognizer?.destroy()
        recognizer = null
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
