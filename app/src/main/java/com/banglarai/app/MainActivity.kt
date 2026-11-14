package com.banglarai.app

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
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
import android.view.ViewGroup
import android.widget.FrameLayout
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
        // Replace with the deployed shared backend URL before public release.
        private const val AI_BACKEND_URL =
            "https://YOUR-BANGLAR-AI-WORKER.workers.dev/v1/chat/completions"

        private const val MIC_PERMISSION = 101
        private const val CAMERA_PERMISSION = 102
    }

    private val green = Color.rgb(0, 230, 118)
    private val dark = Color.rgb(5, 13, 12)
    private val cardColor = Color.rgb(15, 31, 26)
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var root: LinearLayout
    private lateinit var contentHost: FrameLayout
    private lateinit var homeBody: LinearLayout
    private lateinit var chatHeading: LinearLayout
    private lateinit var inputRow: LinearLayout
    private var selectedTab = 0
    private lateinit var chatLayout: LinearLayout
    private lateinit var chatScroll: ScrollView
    private lateinit var inputBox: EditText
    private lateinit var statusText: TextView
    private lateinit var powerButton: Button
    private lateinit var hangupButton: Button
    private lateinit var aiOrb: TextView
    private var orbPulse: ObjectAnimator? = null

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
            setBackgroundColor(dark)
        }
        contentHost = FrameLayout(this)
        homeBody = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(10))
            setBackgroundColor(dark)
        }

        // Top bar
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brandBlock = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        val heading = TextView(this).apply {
            text = "✦ বাংলার AI"
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(green)
        }
        val tagline = TextView(this).apply {
            text = "আপনার ব্যক্তিগত AI সহকারী"
            textSize = 11f
            setTextColor(Color.LTGRAY)
        }
        brandBlock.addView(heading)
        brandBlock.addView(tagline)
        val settingsButton = makeButton("⚙")
        settingsButton.textSize = 20f
        settingsButton.setOnClickListener { renderTab(4) }
        header.addView(brandBlock, LinearLayout.LayoutParams(0, dp(54), 1f))
        header.addView(settingsButton, LinearLayout.LayoutParams(dp(48), dp(46)))

        // Futuristic AI hero card
        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.rgb(8, 48, 34), Color.rgb(12, 27, 23))
            ).apply { cornerRadius = dp(22).toFloat() }
        }
        val orb = TextView(this).apply {
            text = "✦"
            textSize = 48f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(0, 255, 150), Color.rgb(0, 105, 72), Color.rgb(4, 30, 24))
            ).apply {
                shape = GradientDrawable.OVAL
                setStroke(dp(2), Color.rgb(70, 255, 175))
            }
            elevation = dp(8).toFloat()
        }
        aiOrb = orb
        val heroTitle = TextView(this).apply {
            text = "আমি আপনার কথা শুনতে প্রস্তুত"
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(0, dp(8), 0, dp(2))
        }
        statusText = TextView(this).apply {
            text = "● AI প্রস্তুত"
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(green)
        }
        powerButton = makeButton("☎  কল করুন")
        powerButton.textSize = 16f
        powerButton.setTextColor(Color.WHITE)
        powerButton.background = rounded(Color.rgb(0, 170, 90), 28)
        powerButton.setOnClickListener { startVoiceCall() }
        hangupButton = makeButton("✕  কল কাটুন")
        hangupButton.textSize = 16f
        hangupButton.setTextColor(Color.WHITE)
        hangupButton.background = rounded(Color.rgb(205, 45, 58), 28)
        hangupButton.setOnClickListener { endVoiceCall() }
        updateCallButtons()

        hero.addView(orb, LinearLayout.LayoutParams(dp(172), dp(172)))
        hero.addView(heroTitle)
        hero.addView(statusText)
        val callRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(18), 0, dp(4))
        }
        val callParams = LinearLayout.LayoutParams(0, dp(54), 1f)
        callParams.setMargins(dp(5), 0, dp(5), 0)
        callRow.addView(powerButton, callParams)
        val hangupParams = LinearLayout.LayoutParams(0, dp(54), 1f)
        hangupParams.setMargins(dp(5), 0, dp(5), 0)
        callRow.addView(hangupButton, hangupParams)
        hero.addView(callRow)

        // Chat panel
        chatHeading = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val chatTitle = sectionTitle("AI CHAT")
        val online = TextView(this).apply {
            text = "● LIVE"
            textSize = 10f
            setTextColor(green)
            gravity = Gravity.CENTER_VERTICAL
        }
        chatHeading.addView(chatTitle, LinearLayout.LayoutParams(0, dp(34), 1f))
        chatHeading.addView(online)

        chatScroll = ScrollView(this).apply {
            isFillViewport = true
            background = rounded(cardColor, 18)
            setPadding(dp(10), dp(10), dp(10), dp(10))
        }
        chatLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        chatScroll.addView(chatLayout)

        // Message composer
        inputRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, dp(4))
        }
        inputBox = EditText(this).apply {
            hint = "আপনার প্রশ্ন লিখুন..."
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            textSize = 14f
            isSingleLine = true
            setPadding(dp(14), 0, dp(8), 0)
            background = GradientDrawable().apply {
                setColor(Color.rgb(18, 34, 29))
                cornerRadius = dp(24).toFloat()
                setStroke(dp(1), Color.rgb(35, 82, 61))
            }
        }
        val micButton = makeButton("🎙")
        micButton.textSize = 17f
        micButton.setOnClickListener {
            if (listening) {
                stopListening()
                statusText.text = "● AI প্রস্তুত"
            } else {
                checkMicPermissionAndListen()
            }
        }
        val sendButton = makeButton("➤")
        sendButton.textSize = 18f
        sendButton.setTextColor(dark)
        sendButton.background = rounded(green, 24)
        sendButton.setOnClickListener { sendMessage() }

        inputRow.addView(inputBox, LinearLayout.LayoutParams(0, dp(48), 1f))
        val micParams = LinearLayout.LayoutParams(dp(46), dp(46))
        micParams.setMargins(dp(6), 0, dp(4), 0)
        inputRow.addView(micButton, micParams)
        inputRow.addView(sendButton, LinearLayout.LayoutParams(dp(46), dp(46)))

        homeBody.addView(header)
        val heroParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        heroParams.setMargins(0, dp(8), 0, dp(8))
        homeBody.addView(hero, heroParams)
        val homeHint = TextView(this).apply {
            text = "সবকিছু বলুন—আমি শুনছি"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
            setPadding(0, dp(18), 0, dp(8))
        }
        homeBody.addView(homeHint)

        contentHost.addView(homeBody, FrameLayout.LayoutParams(-1, -1))
        root.addView(contentHost, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(createBottomNavigation())
        setContentView(root)
        restoreConversation()
        if (conversation.isEmpty()) {
            addMessage(false, "আসসালামু আলাইকুম! আমি বাংলার AI। আমাকে যেকোনো প্রশ্ন করতে পারেন।")
        }
    }

    // BOTTOM NAVIGATION AND FULL-PAGE SCREENS

    private fun createBottomNavigation(): LinearLayout {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(5), dp(7), dp(5), dp(7))
            setBackgroundColor(Color.rgb(9, 23, 19))
        }
        val items = listOf("⌂\nহোম", "✉\nচ্যাট", "✧\nস্টোরি", "◷\nইতিহাস", "⚙\nসেটিংস")
        items.forEachIndexed { index, label ->
            val button = TextView(this).apply {
                text = label
                gravity = Gravity.CENTER
                textSize = 11f
                typeface = if (selectedTab == index) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                setTextColor(if (selectedTab == index) green else Color.LTGRAY)
                setPadding(dp(2), dp(3), dp(2), dp(3))
                setOnClickListener { renderTab(index) }
            }
            bar.addView(button, LinearLayout.LayoutParams(0, dp(52), 1f))
        }
        return bar
    }

    private fun renderTab(index: Int) {
        selectedTab = index
        contentHost.removeAllViews()
        (chatHeading.parent as? ViewGroup)?.removeView(chatHeading)
        (chatScroll.parent as? ViewGroup)?.removeView(chatScroll)
        (inputRow.parent as? ViewGroup)?.removeView(inputRow)
        when (index) {
            0 -> contentHost.addView(homeBody, FrameLayout.LayoutParams(-1, -1))
            1 -> {
                val page = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(14), dp(12), dp(14), dp(8))
                    setBackgroundColor(dark)
                }
                page.addView(chatHeading)
                page.addView(chatScroll, LinearLayout.LayoutParams(-1, 0, 1f))
                page.addView(inputRow)
                contentHost.addView(page, FrameLayout.LayoutParams(-1, -1))
            }
            2 -> contentHost.addView(buildStoryPage(), FrameLayout.LayoutParams(-1, -1))
            3 -> contentHost.addView(buildHistoryPage(), FrameLayout.LayoutParams(-1, -1))
            4 -> contentHost.addView(buildSettingsPage(), FrameLayout.LayoutParams(-1, -1))
        }
        if (root.childCount > 1) root.removeViewAt(root.childCount - 1)
        root.addView(createBottomNavigation())
    }

    private fun pageShell(title: String, subtitle: String): LinearLayout {
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            setBackgroundColor(dark)
        }
        page.addView(TextView(this).apply {
            text = title
            textSize = 25f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(green)
        })
        page.addView(TextView(this).apply {
            text = subtitle
            textSize = 13f
            setTextColor(Color.LTGRAY)
            setPadding(0, dp(5), 0, dp(16))
        })
        return page
    }

    private fun pageAction(parent: LinearLayout, title: String, detail: String, action: () -> Unit) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15), dp(13), dp(15), dp(13))
            background = rounded(cardColor, 16)
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
        card.addView(TextView(this).apply {
            text = title
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        })
        card.addView(TextView(this).apply {
            text = detail
            textSize = 12f
            setTextColor(Color.LTGRAY)
            setPadding(0, dp(4), 0, 0)
        })
        val params = LinearLayout.LayoutParams(-1, -2)
        params.setMargins(0, 0, 0, dp(10))
        parent.addView(card, params)
    }

    private fun buildStoryPage(): View {
        val page = pageShell("স্টোরি", "বাংলার AI-এর সঙ্গে তৈরি করা গল্প ও লেখার জায়গা।")
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        pageAction(list, "✍ নতুন গল্প লিখুন", "চ্যাটে গিয়ে গল্পের বিষয় বলুন; AI গল্প তৈরি করবে।") { renderTab(1) }
        pageAction(list, "💬 AI চ্যাট খুলুন", "আপনার গল্পের ধারণা বা চরিত্র নিয়ে কথা বলুন।") { renderTab(1) }
        page.addView(list, LinearLayout.LayoutParams(-1, -1))
        return page
    }

    private fun buildToolsPage(): View {
        val page = pageShell("স্মার্ট টুলস", "ফোনের কাজগুলো প্রয়োজন অনুযায়ী খুলুন।")
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        pageAction(list, "🔦 ফ্ল্যাশলাইট", "ফোনের ফ্ল্যাশ চালু বা বন্ধ করুন।") { toggleFlashlight() }
        pageAction(list, "📷 ক্যামেরা", "ক্যামেরা খুলুন।") { openCamera() }
        pageAction(list, "🖼 গ্যালারি ও ফাইল", "ছবি বা ডকুমেন্ট নির্বাচন করুন।") { openGallery() }
        pageAction(list, "📶 Wi-Fi সেটিংস", "ফোনের Wi-Fi পেজ খুলুন।") { openSettings(Settings.ACTION_WIFI_SETTINGS) }
        pageAction(list, "Bluetooth সেটিংস", "Bluetooth সংযোগের পেজ খুলুন।") { openSettings(Settings.ACTION_BLUETOOTH_SETTINGS) }
        pageAction(list, "▶ YouTube", "YouTube খুলুন।") { openUrl("https://www.youtube.com") }
        pageAction(list, "Facebook", "Facebook খুলুন।") { openUrl("https://www.facebook.com") }
        pageAction(list, "☀ ডিসপ্লে", "Brightness ও display সেটিংস খুলুন।") { openSettings(Settings.ACTION_DISPLAY_SETTINGS) }
        scroll.addView(list)
        page.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        return page
    }

    private fun buildHistoryPage(): View {
        val page = pageShell("চ্যাটের ইতিহাস", "এই ডিভাইসে সংরক্ষিত সাম্প্রতিক কথোপকথন।")
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        if (conversation.isEmpty()) {
            pageAction(list, "এখনো কোনো ইতিহাস নেই", "AI-এর সঙ্গে কথা বললে এখানে দেখা যাবে।") { renderTab(1) }
        } else {
            conversation.takeLast(80).forEach { item ->
                pageAction(list, if (item.first == "user") "আপনি" else "বাংলার AI", item.second) { renderTab(1) }
            }
            pageAction(list, "ইতিহাস মুছুন", "এই ডিভাইসে সংরক্ষিত কথোপকথন মুছে যাবে।") {
                clearChat()
                renderTab(3)
            }
        }
        scroll.addView(list)
        page.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        return page
    }

    private fun buildSettingsPage(): View {
        val page = pageShell("সেটিংস", "প্রতিটি অনুমতির কাজ বুঝে তারপর চালু করুন। API Key এখানে লাগবে না।")
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        pageAction(list, "🎙 মাইক্রোফোন অনুমতি", "ভয়েসে কথা বলা ও Speech Recognition-এর জন্য।") {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), MIC_PERMISSION)
            } else Toast.makeText(this, "মাইক্রোফোন অনুমতি চালু আছে", Toast.LENGTH_SHORT).show()
        }
        pageAction(list, "📷 ক্যামেরা অনুমতি", "ক্যামেরা-সম্পর্কিত ফিচারের জন্য।") {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION)
            } else openCamera()
        }
        pageAction(list, "🪟 Floating AI অনুমতি", "অন্য অ্যাপের ওপর ছোট AI বাটন দেখানোর জন্য।") {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            } else toggleFloatingAssistant()
        }
        pageAction(list, "♿ Accessibility", "স্ক্রিনের দৃশ্যমান লেখা পড়া বা সহায়ক কাজের জন্য। প্রয়োজন না হলে চালু করবেন না।") {
            openSettings(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        }
        pageAction(list, "🔔 নোটিফিকেশন অনুমতি", "বাংলার AI-এর নিজের নোটিফিকেশনের জন্য; অন্য অ্যাপের নোটিফিকেশন পড়ার অনুমতি নয়।") {
            if (android.os.Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 203)
            } else Toast.makeText(this, "নোটিফিকেশন অনুমতি চালু আছে অথবা আলাদা অনুমতি প্রয়োজন নেই", Toast.LENGTH_LONG).show()
        }
        pageAction(list, "📁 ফাইল ও ছবি নির্বাচন", "Android-এর নিরাপদ File Picker খুলবে; প্রয়োজনের ফাইল নিজে নির্বাচন করুন।") { openGallery() }
        pageAction(list, "🛡 গোপনীয়তা", "চ্যাটের ইতিহাস এই ডিভাইসে থাকে; AI উত্তর পেতে প্রশ্ন Backend-এ পাঠানো হয়।") {
            AlertDialog.Builder(this).setTitle("গোপনীয়তা")
                .setMessage("আপনার লেখা প্রশ্ন AI উত্তর তৈরির জন্য বাংলার AI Backend-এ পাঠানো হয়। API Key অ্যাপের মধ্যে রাখা হয় না। চ্যাটের ইতিহাস এই ডিভাইসের অ্যাপ ডেটায় থাকে।")
                .setPositiveButton("ঠিক আছে", null).show()
        }
        pageAction(list, "ℹ অ্যাপ সম্পর্কে", "বাংলার AI • বাংলা ভাষার ব্যক্তিগত সহকারী") {
            AlertDialog.Builder(this).setTitle("বাংলার AI")
                .setMessage("বাংলার AI একটি বাংলা ভয়েস ও চ্যাট সহকারী। কিছু ফিচার চালাতে ইন্টারনেট ও Android অনুমতি প্রয়োজন।")
                .setPositiveButton("বন্ধ", null).show()
        }
        scroll.addView(list)
        page.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        return page
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
            gravity = if (isUser) Gravity.END else Gravity.START
            setTextColor(
                if (isUser) Color.WHITE else Color.rgb(210, 255, 225)
            )
            setPadding(dp(13), dp(10), dp(13), dp(10))
            background = rounded(
                if (isUser) Color.rgb(0, 112, 70)
                else Color.rgb(25, 44, 37),
                15
            )
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = if (isUser) Gravity.END else Gravity.START
        params.setMargins(dp(2), dp(5), dp(2), dp(5))

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
        if (handleLocalCommand(message)) return
        askGroq()
    }

    /**
     * Small, explicit on-device actions. AI chat remains the fallback.
     * Accessibility actions work only after the user enables the service in Android Settings.
     */
    private fun handleLocalCommand(message: String): Boolean {
        val text = message.trim()
        val lower = text.lowercase(Locale.ROOT)

        if (lower.contains("youtube") && (lower.contains("সার্চ") || lower.contains("খুঁজে") || lower.contains("search"))) {
            val query = text.substringAfter("youtube", "")
                .replace(Regex("(?i)^(এ|তে|\u0020)*"), "")
                .replace(Regex("(সার্চ করো|সার্চ দাও|খুঁজে দাও|search করে দাও|search)$", RegexOption.IGNORE_CASE), "")
                .trim()
            if (query.isNotBlank()) {
                openUrl("https://www.youtube.com/results?search_query=" + Uri.encode(query))
                completeLocalAction("YouTube-এ “$query” সার্চ খুলে দিয়েছি।")
            } else {
                openUrl("https://www.youtube.com")
                completeLocalAction("YouTube খুলে দিয়েছি। কী সার্চ করতে চান বলুন।")
            }
            return true
        }

        if (lower.contains("youtube") && (lower.contains("খুলে") || lower.contains("চালু"))) {
            openUrl("https://www.youtube.com")
            completeLocalAction("YouTube খুলে দিয়েছি।")
            return true
        }

        if ((lower.contains("স্ক্রিনে কী আছে") || lower.contains("পেজে কী আছে") ||
             lower.contains("পর্দায় কী আছে") || lower.contains("স্ক্রিন পড়ে") ||
             lower.contains("স্ক্রিন পড়ে")) && lower.contains("বল")) {
            val visible = BanglarAccessibilityService.readVisibleText()
            val answer = if (visible.isNullOrBlank()) {
                "অন্য অ্যাপের স্ক্রিন পড়তে Settings থেকে বাংলার AI Accessibility চালু করুন। তারপর আবার বলুন।"
            } else {
                "বর্তমান স্ক্রিনে যে লেখা পড়তে পারছি:\\n" + visible.take(2200)
            }
            completeLocalAction(answer)
            return true
        }

        if (lower.contains("ক্লিক করো") || lower.contains("চাপ দাও")) {
            val target = text.substringAfter("স্ক্রিনে", "")
                .substringBefore("লেখা")
                .substringBefore("ক্লিক করো")
                .substringBefore("চাপ দাও")
                .trim()
            if (target.isBlank()) {
                completeLocalAction("কোন লেখাযুক্ত বোতাম বা আইটেমে ক্লিক করব তা বলুন। যেমন: “স্ক্রিনে Shorts লেখা খুঁজে ক্লিক করো।”")
            } else {
                val clicked = BanglarAccessibilityService.clickVisibleText(target)
                completeLocalAction(
                    if (clicked) "“$target” আইটেমে ক্লিক করেছি।"
                    else "“$target” লেখা স্ক্রিনে পাওয়া যায়নি, অথবা সেটিতে ক্লিক করা সম্ভব হয়নি।"
                )
            }
            return true
        }
        return false
    }

    private fun completeLocalAction(reply: String) {
        addMessage(false, reply)
        conversation.add(Pair("assistant", reply))
        saveConversation()
        if (voiceEnabled && ttsReady) speak(reply)
    }

    // GROQ AI

    private fun askGroq() {
        busy = true
        statusText.text = "● AI চিন্তা করছে..."
        statusText.setTextColor(Color.YELLOW)
        Thread {
            var answer: String
            var connection: HttpURLConnection? = null
            try {
                connection = URL(AI_BACKEND_URL).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.connectTimeout = 20000
                connection.readTimeout = 45000
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.setRequestProperty("Accept", "application/json")
                val messages = JSONArray()
                messages.put(JSONObject().put("role", "system").put("content",
                    "তুমি বাংলার AI। সহজ, পরিষ্কার ও স্বাভাবিক বাংলায় উত্তর দেবে। প্রশ্ন অনুযায়ী প্রয়োজনীয় তথ্য বুঝিয়ে বলবে।"))
                for (item in conversation.takeLast(16)) {
                    messages.put(JSONObject().put("role", item.first).put("content", item.second))
                }
                val body = JSONObject().put("messages", messages)
                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
                val code = connection.responseCode
                val response = if (code in 200..299) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                }
                answer = if (code in 200..299) {
                    JSONObject(response).getJSONArray("choices").getJSONObject(0)
                        .getJSONObject("message").getString("content").trim()
                } else when (code) {
                    401, 403 -> "AI সার্ভারের অনুমোদন সমস্যা হয়েছে। পরে আবার চেষ্টা করুন।"
                    429 -> "এখন অনেক ব্যবহারকারী AI ব্যবহার করছেন। একটু পরে আবার চেষ্টা করুন।"
                    else -> "AI সার্ভারে সমস্যা হয়েছে ($code)। কিছুক্ষণ পরে আবার চেষ্টা করুন।"
                }
            } catch (_: Exception) {
                answer = "বাংলার AI সার্ভারে সংযোগ করা যাচ্ছে না। ইন্টারনেট পরীক্ষা করুন।"
            } finally {
                connection?.disconnect()
            }
            runOnUiThread {
                busy = false
                statusText.text = "● AI প্রস্তুত"
                statusText.setTextColor(green)
                addMessage(false, answer)
                conversation.add(Pair("assistant", answer))
                saveConversation()
                if (voiceEnabled) speak(answer)
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
                    override fun onStart(utteranceId: String?) {
                handler.post { startOrbAnimation() }
            }

                    override fun onDone(utteranceId: String?) {
                        handler.post {
                            stopOrbAnimation()
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
        if (voiceEnabled) endVoiceCall() else startVoiceCall()
    }

    private fun startVoiceCall() {
        if (voiceEnabled) return
        voiceEnabled = true
        statusText.text = "● কল সংযোগ হচ্ছে..."
        statusText.setTextColor(Color.rgb(0, 220, 130))
        updateCallButtons()
        checkMicPermissionAndListen()
    }

    private fun endVoiceCall() {
        voiceEnabled = false
        stopListening()
        tts?.stop()
        stopOrbAnimation()
        statusText.text = "● কল শেষ হয়েছে"
        statusText.setTextColor(Color.LTGRAY)
        updateCallButtons()
    }

    private fun updateCallButtons() {
        if (!::powerButton.isInitialized || !::hangupButton.isInitialized) return
        powerButton.isEnabled = !voiceEnabled
        hangupButton.isEnabled = voiceEnabled
        powerButton.alpha = if (voiceEnabled) 0.45f else 1f
        hangupButton.alpha = if (voiceEnabled) 1f else 0.45f
    }

    private fun startOrbAnimation() {
        if (!::aiOrb.isInitialized || orbPulse?.isRunning == true) return
        orbPulse = ObjectAnimator.ofFloat(aiOrb, "rotation", -4f, 4f, -4f).apply {
            duration = 1800
            repeatCount = ValueAnimator.INFINITE
            start()
        }
        aiOrb.animate().scaleX(1.08f).scaleY(1.08f).setDuration(700).start()
    }

    private fun stopOrbAnimation() {
        orbPulse?.cancel()
        orbPulse = null
        if (::aiOrb.isInitialized) aiOrb.animate().rotation(0f).scaleX(1f).scaleY(1f).setDuration(350).start()
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
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "application/pdf", "text/plain"))
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

    private fun showSettings() { renderTab(4) }

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
                updateCallButtons()
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
