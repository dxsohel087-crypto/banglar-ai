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
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    companion object {
         private const val OVERLAY_PERMISSION = 401
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
    private var orbScaleX: ObjectAnimator? = null
    private var orbScaleY: ObjectAnimator? = null

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var recognizer: SpeechRecognizer? = null
    private var voiceEnabled = false
    private var listening = false
    private var busy = false
    private var torchOn = false
    private var pendingTorchState: Boolean? = null

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
            text = "NEXVIO"
            textSize = 34f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        }

        val subtitle = TextView(this).apply {
            text = "OFFLINE VOICE PHONE CONTROL"
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(green)
        }

        val version = TextView(this).apply {
            text = "YOUR VOICE • YOUR CONTROL"
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
            text = "✦ NEXVIO"
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(green)
        }
        val tagline = TextView(this).apply {
            text = "আপনার কণ্ঠে ফোন নিয়ন্ত্রণ"
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
            text = "● NEXVIO প্রস্তুত"
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(green)
        }
        powerButton = makeButton("☎  কল করুন")
        powerButton.textSize = 16f
        powerButton.setTextColor(Color.WHITE)
        powerButton.background = rounded(Color.rgb(0, 170, 90), 28)
        powerButton.setOnClickListener { animateCallButton(powerButton) { startVoiceCall() } }
        hangupButton = makeButton("✕  কল কাটুন")
        hangupButton.textSize = 16f
        hangupButton.setTextColor(Color.WHITE)
        hangupButton.background = rounded(Color.rgb(205, 45, 58), 28)
        hangupButton.setOnClickListener { animateCallButton(hangupButton) { endVoiceCall() } }
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
        val chatTitle = sectionTitle("NEXVIO COMMAND")
        val online = TextView(this).apply {
            text = "● OFFLINE"
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
            addMessage(false, "আমি NEXVIO। আপনার ফোনে যে কাজগুলো অনুমোদিত, সেগুলো মুখে বলুন বা নিচের কমান্ড থেকে বেছে নিন।")
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
        val items = listOf("⌂\nহোম", "✉\nচ্যাট", "✧\nকমান্ড", "◷\nইতিহাস", "⚙\nসেটিংস")
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
            2 -> contentHost.addView(buildToolsPage(), FrameLayout.LayoutParams(-1, -1))
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
        val page = pageShell("স্টোরি", "NEXVIO-এর সঙ্গে তৈরি করা গল্প ও লেখার জায়গা।")
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        pageAction(list, "✍ নতুন গল্প লিখুন", "চ্যাটে গিয়ে গল্পের বিষয় বলুন; AI গল্প তৈরি করবে।") { renderTab(1) }
        pageAction(list, "💬 AI চ্যাট খুলুন", "আপনার গল্পের ধারণা বা চরিত্র নিয়ে কথা বলুন।") { renderTab(1) }
        page.addView(list, LinearLayout.LayoutParams(-1, -1))
        return page
    }

    private fun buildToolsPage(): View {
        val page = pageShell("কমান্ড লাইব্রেরি", "প্রতিটি বিভাগের প্রশ্ন দেখুন, নিচের কমান্ডে চাপ দিন অথবা নিজের ভাষায় বলুন।")
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(18))
        }
        commandSection(list, "📱 অ্যাপ ও ফোন", "কোন অ্যাপ বা ফোনের অংশ খুলতে চান?", listOf(
            "YouTube খোলো", "Facebook খোলো", "Chrome খোলো", "ক্যালকুলেটর খোলো",
            "ক্যামেরা খোলো", "সেটিংস খোলো", "সব অ্যাপের তালিকা দেখাও", "হোম স্ক্রিনে যাও"
        ))
        commandSection(list, "🔦 আলো ও ডিসপ্লে", "ফোনের আলো বা স্ক্রিনে কী করতে চান?", listOf(
            "টর্চ জ্বালাও", "টর্চ বন্ধ করো", "ব্রাইটনেস সেটিংস খোলো"
        ))
        commandSection(list, "🔊 শব্দ নিয়ন্ত্রণ", "শব্দের মাত্রা কীভাবে বদলাবেন?", listOf(
            "ভলিউম বাড়াও", "ভলিউম কমাও", "সাউন্ড মিউট করো", "সাউন্ড চালু করো"
        ))
        commandSection(list, "📶 সংযোগ ও নেটওয়ার্ক", "কোন সংযোগের সেটিংস খুলতে চান?", listOf(
            "Wi-Fi সেটিংস খোলো", "Bluetooth সেটিংস খোলো", "Airplane mode সেটিংস খোলো"
        ))
        commandSection(list, "📂 ফাইল ও স্ক্রিন", "কোন ফাইল দেখতে বা স্ক্রিনে কী আছে জানতে চান?", listOf(
            "গ্যালারি খোলো", "ফাইল বাছাই করো", "স্ক্রিনে কী আছে পড়ে বলো",
            "স্ক্রিনে Shorts লেখা খুঁজে ক্লিক করো"
        ))
        commandSection(list, "☎️ ফোনের কাজ", "ফোনের কোন কাজে যেতে চান?", listOf(
            "ডায়ালার খোলো", "এখন সময় কত", "কমান্ড দেখাও"
        ))
        scroll.addView(list)
        page.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        return page
    }

    private fun commandSection(parent: LinearLayout, title: String, question: String, commands: List<String>) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(13), dp(14), dp(13))
            background = rounded(cardColor, 17)
        }
        card.addView(TextView(this).apply {
            text = title
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(green)
        })
        card.addView(TextView(this).apply {
            text = question
            textSize = 12f
            setTextColor(Color.LTGRAY)
            setPadding(0, dp(5), 0, dp(9))
        })
        commands.forEach { command ->
            val button = makeButton("›  " + command)
            button.gravity = Gravity.START or Gravity.CENTER_VERTICAL
            button.textSize = 13f
            button.setTextColor(Color.WHITE)
            button.background = rounded(Color.rgb(24, 47, 39), 12)
            val params = LinearLayout.LayoutParams(-1, dp(42))
            params.setMargins(0, 0, 0, dp(6))
            card.addView(button, params)
            button.setOnClickListener { runSuggestedCommand(command) }
        }
        val params = LinearLayout.LayoutParams(-1, -2)
        params.setMargins(0, 0, 0, dp(11))
        parent.addView(card, params)
    }

private fun buildSettingsPage(): View {
        val page = pageShell("সেটিংস", "NEXVIO অফলাইনে চলে। প্রয়োজনীয় অনুমতি আপনি নিজে নিয়ন্ত্রণ করবেন।")
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        pageAction(list, "🎙 মাইক্রোফোন অনুমতি", "আপনার কথা শোনার জন্য।") {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), MIC_PERMISSION)
            } else Toast.makeText(this, "মাইক্রোফোন অনুমতি চালু আছে", Toast.LENGTH_SHORT).show()
        }
        pageAction(list, "📷 ক্যামেরা অনুমতি", "ফ্ল্যাশলাইট ও ক্যামেরা কমান্ডের জন্য।") {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION)
            } else openCamera()
        }
        pageAction(list, "♿ Accessibility চালু করুন", "আপনার সরাসরি নির্দেশে অন্য অ্যাপের দৃশ্যমান লেখা পড়া বা নির্বাচিত আইটেমে চাপ দেওয়ার জন্য।") {
            openSettings(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        }
        pageAction(list, "🪟 ভাসমান কন্ট্রোল", "অন্য অ্যাপের ওপর NEXVIO শর্টকাট দেখানোর ঐচ্ছিক সুবিধা।") {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&
                !Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName)))
            } else {
                toggleFloatingAssistant()
            }
        }
        pageAction(list, "🗣 অফলাইন বাংলা Speech Settings", "ফোনে On-device recognition থাকলে বাংলা Offline ভাষা প্যাক ইনস্টল করুন।") {
            try {
                startActivity(Intent("android.speech.action.MULTI_LANGUAGE_SETTINGS"))
            } catch (_: Exception) {
                openSettings(Settings.ACTION_SETTINGS)
            }
        }
        pageAction(list, "📁 ফাইল পিকার", "নিজে নির্বাচন করে ছবি, ভিডিও বা ডকুমেন্ট খুলুন।") { openGallery() }
        pageAction(list, "🛡 গোপনীয়তা", "কমান্ড এই ফোনেই যাচাই হয়। NEXVIO-তে Internet permission বা AI server connection নেই।") {
            AlertDialog.Builder(this)
                .setTitle("NEXVIO গোপনীয়তা")
                .setMessage("আপনার কমান্ড ও কথোপকথনের ইতিহাস এই ডিভাইসেই থাকে। NEXVIO কোনো AI সার্ভারে লেখা, ভয়েস বা স্ক্রিন পাঠায় না। অফলাইন ভয়েসের জন্য ফোনের নিজস্ব On-device Speech Recognition ব্যবহার হয়।")
                .setPositiveButton("ঠিক আছে", null).show()
        }
        pageAction(list, "ℹ অ্যাপ সম্পর্কে", "NEXVIO • Offline Voice Phone Control") {
            AlertDialog.Builder(this)
                .setTitle("NEXVIO")
                .setMessage("আপনার কণ্ঠে ফোনের অনুমোদিত কাজ সম্পন্ন করার অফলাইন কন্ট্রোলার। সব Android সেটিংস সরাসরি বদলানো যায় না; কিছু ক্ষেত্রে Settings পেজ খুলবে।")
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
            text = (if (isUser) "আপনি: " else "NEXVIO: ") + message
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
        if (!handleLocalCommand(message)) {
            completeLocalAction(
                "এই কমান্ডটি এখনো শেখানো হয়নি। ‘কমান্ড’ পেজে প্রস্তুত নির্দেশগুলো দেখুন। " +
                    "NEXVIO অফলাইনে কাজ করে; অনলাইন AI চ্যাট ব্যবহার করে না।"
            )
        }
    }

    private fun handleLocalCommand(message: String): Boolean {
        val text = message.trim()
        val lower = text.lowercase(Locale.ROOT)
        fun has(vararg words: String): Boolean = words.any { lower.contains(it) }

        if (has("স্ক্রিনে কী আছে", "পেজে কী আছে", "পর্দায় কী আছে", "পর্দায় কী আছে",
                "স্ক্রিন পড়ে", "স্ক্রিন পড়ে", "লেখাগুলো পড়ে", "লেখাগুলো পড়ে")) {
            val visible = BanglarAccessibilityService.readVisibleText()
            val answer = if (visible.isNullOrBlank()) {
                "স্ক্রিন পড়তে Android Settings থেকে NEXVIO Accessibility চালু করুন। তারপর আবার বলুন।"
            } else {
                "বর্তমান স্ক্রিনে দেখা যাচ্ছে:\n" + visible.take(2200)
            }
            completeLocalAction(answer)
            return true
        }

        if (has("ক্লিক করো", "চাপ দাও", "ট্যাপ করো", "চাপুন")) {
            val target = text.substringAfter("স্ক্রিনে", "")
                .substringBefore("লেখা")
                .substringBefore("ক্লিক করো")
                .substringBefore("চাপ দাও")
                .substringBefore("ট্যাপ করো")
                .trim()
            if (target.isBlank()) {
                completeLocalAction("কোন লেখা বা বোতামে চাপ দেব? যেমন: স্ক্রিনে Shorts লেখা খুঁজে ক্লিক করো।")
            } else {
                val clicked = BanglarAccessibilityService.clickVisibleText(target)
                completeLocalAction(
                    if (clicked) "“" + target + "” আইটেমে চাপ দিয়েছি।"
                    else "“" + target + "” পাওয়া যায়নি। Accessibility চালু আছে কি না দেখুন।"
                )
            }
            return true
        }

        if (has("টর্চ", "ফ্ল্যাশলাইট", "ফ্ল্যাশ")) {
            when {
                has("নেভাও", "বন্ধ", "অফ", "off") -> {
                    toggleFlashlight(false)
                    completeLocalAction("টর্চ বন্ধ করার নির্দেশ দেওয়া হয়েছে।")
                }
                has("জ্বালাও", "চালু", "অন", "on") -> {
                    toggleFlashlight(true)
                    completeLocalAction("টর্চ চালু করার নির্দেশ দেওয়া হয়েছে।")
                }
                else -> {
                    toggleFlashlight()
                    completeLocalAction("টর্চের অবস্থা পরিবর্তন করেছি।")
                }
            }
            return true
        }

        if (has("ভলিউম", "শব্দ", "সাউন্ড")) {
            val audio = getSystemService(AUDIO_SERVICE) as android.media.AudioManager
            when {
                has("বাড়াও", "বাড়াও", "বেশি", "উচ্চ") -> {
                    audio.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC,
                        android.media.AudioManager.ADJUST_RAISE, android.media.AudioManager.FLAG_SHOW_UI)
                    completeLocalAction("মিডিয়া ভলিউম বাড়িয়েছি।")
                }
                has("কমাও", "কমান", "নিচু") -> {
                    audio.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC,
                        android.media.AudioManager.ADJUST_LOWER, android.media.AudioManager.FLAG_SHOW_UI)
                    completeLocalAction("মিডিয়া ভলিউম কমিয়েছি।")
                }
                has("আনমিউট", "শব্দ চালু", "সাউন্ড চালু", "unmute") -> {
                    audio.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC,
                        android.media.AudioManager.ADJUST_UNMUTE, android.media.AudioManager.FLAG_SHOW_UI)
                    completeLocalAction("মিডিয়া সাউন্ড চালু করেছি।")
                }
                has("মিউট", "নীরব", "mute", "বন্ধ") -> {
                    audio.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC,
                        android.media.AudioManager.ADJUST_MUTE, android.media.AudioManager.FLAG_SHOW_UI)
                    completeLocalAction("মিডিয়া সাউন্ড মিউট করেছি।")
                }
                else -> {
                    openSettings(Settings.ACTION_SOUND_SETTINGS)
                    completeLocalAction("সাউন্ড সেটিংস খুলে দিয়েছি।")
                }
            }
            return true
        }

        if (has("ওয়াইফাই", "ওয়াইফাই", "wi-fi", "wifi")) {
            openSettings(Settings.ACTION_WIFI_SETTINGS)
            completeLocalAction("Wi-Fi সেটিংস খুলে দিয়েছি। Android-এর নিয়ম অনুযায়ী এখান থেকে সংযোগ বদলান।")
            return true
        }
        if (has("ব্লুটুথ", "bluetooth")) {
            openSettings(Settings.ACTION_BLUETOOTH_SETTINGS)
            completeLocalAction("Bluetooth সেটিংস খুলে দিয়েছি।")
            return true
        }
        if (has("এয়ারপ্লেন", "এয়ারপ্লেন", "ফ্লাইট মোড", "airplane")) {
            openSettings(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
            completeLocalAction("Airplane mode সেটিংস খুলে দিয়েছি।")
            return true
        }
        if (has("ব্রাইটনেস", "স্ক্রিনের আলো", "ডিসপ্লে", "উজ্জ্বলতা")) {
            openSettings(Settings.ACTION_DISPLAY_SETTINGS)
            completeLocalAction("Display ও brightness সেটিংস খুলে দিয়েছি।")
            return true
        }

        if (has("ক্যামেরা", "camera") && has("খোলো", "খুলে", "চালু", "open")) {
            openCamera()
            completeLocalAction("ক্যামেরা খুলেছি।")
            return true
        }
        if (has("গ্যালারি", "ছবি দেখাও", "ফটো", "gallery", "photos", "ফাইল বাছাই") &&
            has("খোলো", "খুলে", "দেখাও", "বাছাই", "open", "করো")) {
            openGallery()
            completeLocalAction("ছবি ও ফাইল বাছাই করার স্ক্রিন খুলেছি।")
            return true
        }
        if (has("ডায়ালার", "ডায়ালার", "ফোন অ্যাপ", "কল করার স্ক্রিন", "নাম্বার") &&
            has("খোলো", "খুলে", "চালু", "open")) {
            try {
                startActivity(Intent(Intent.ACTION_DIAL))
                completeLocalAction("ফোনের ডায়ালার খুলেছি।")
            } catch (_: Exception) {
                completeLocalAction("এই ফোনে ডায়ালার খোলা যায়নি।")
            }
            return true
        }
        if (has("সব অ্যাপ", "অ্যাপের তালিকা", "ইনস্টল করা অ্যাপ")) {
            openSettings(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
            completeLocalAction("ইনস্টল করা অ্যাপের তালিকা দেখার সেটিংস খুলেছি।")
            return true
        }
        if (has("হোম স্ক্রিন", "হোমে যাও", "home screen", "গো হোম")) {
            try {
                startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME))
                completeLocalAction("হোম স্ক্রিনে যাওয়ার নির্দেশ দিয়েছি।")
            } catch (_: Exception) {
                completeLocalAction("হোম স্ক্রিন খোলা যায়নি।")
            }
            return true
        }
        if (has("সেটিংস", "settings") && has("খোলো", "খুলে", "চালু", "open")) {
            openSettings(Settings.ACTION_SETTINGS)
            completeLocalAction("ফোনের Settings খুলেছি।")
            return true
        }

        if (has("খোলো", "খুলে দাও", "খুলুন", "চালু করো", "চালু করুন", "ওপেন", "open")) {
            val appAlias = when {
                has("youtube", "ইউটিউব") -> "youtube"
                has("facebook", "ফেসবুক") -> "facebook"
                has("chrome", "ক্রোম", "ব্রাউজার") -> "chrome"
                has("ক্যালকুলেটর", "calculator", "হিসাব") -> "calculator"
                has("ফাইল ম্যানেজার", "file manager", "files", "ফাইল") -> "files"
                has("মিউজিক", "গান", "music", "মিউজিক প্লেয়ার") -> "music"
                else -> null
            }
            if (appAlias != null) {
                val opened = openInstalledApp(appAlias)
                completeLocalAction(
                    if (opened) appAlias + " অ্যাপ খুলেছি।"
                    else appAlias + " অ্যাপটি এই ফোনে পাওয়া যায়নি।"
                )
                return true
            }
        }

        if (has("সময় কত", "এখন কয়টা", "এখন কয়টা", "সময় বল", "সময় বল")) {
            val time = java.text.SimpleDateFormat("hh:mm a", Locale.getDefault()).format(java.util.Date())
            completeLocalAction("এখন সময় " + time + "।")
            return true
        }
        if (has("কমান্ড দেখাও", "কী কী করতে পারো", "কি কি করতে পারো", "সাহায্য", "help")) {
            completeLocalAction("আমি অফলাইনে টর্চ, ভলিউম, Wi-Fi/Bluetooth সেটিংস, ক্যামেরা, ফাইল পিকার, ডায়ালার, ইনস্টল করা কয়েকটি অ্যাপ খোলা এবং Accessibility দিয়ে স্ক্রিনের লেখা পড়া/নির্বাচিত লেখা চাপার কাজ করতে পারি। ‘কমান্ড’ পেজে উদাহরণ আছে।")
            return true
        }
        return false
    }

    private fun openInstalledApp(alias: String): Boolean {
        val packageCandidates = when (alias) {
            "youtube" -> listOf("com.google.android.youtube")
            "facebook" -> listOf("com.facebook.katana")
            "chrome" -> listOf("com.android.chrome")
            "music" -> listOf("com.google.android.apps.youtube.music", "com.spotify.music")
            else -> emptyList()
        }
        for (appPackage in packageCandidates) {
            val launch = packageManager.getLaunchIntentForPackage(appPackage)
            if (launch != null) {
                try {
                    startActivity(launch)
                    return true
                } catch (_: Exception) {}
            }
        }
        val wanted = when (alias) {
            "calculator" -> listOf("calculator", "ক্যালকুলেটর")
            "files" -> listOf("files", "file manager", "ফাইল")
            "music" -> listOf("music", "মিউজিক", "গান")
            else -> listOf(alias)
        }
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val candidates = packageManager.queryIntentActivities(launcher, 0)
        val match = candidates.firstOrNull { info ->
            val label = info.loadLabel(packageManager).toString().lowercase(Locale.ROOT)
            wanted.any { label.contains(it) }
        } ?: return false
        val appIntent = packageManager.getLaunchIntentForPackage(match.activityInfo.packageName) ?: return false
        return try {
            startActivity(appIntent)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun runSuggestedCommand(command: String) {
        if (!::inputBox.isInitialized) return
        renderTab(1)
        handler.postDelayed({
            inputBox.setText(command)
            sendMessage()
        }, 120)
    }

    private fun completeLocalAction(reply: String) {
        addMessage(false, reply)
        conversation.add(Pair("assistant", reply))
        saveConversation()
        if (voiceEnabled) {
            if (ttsReady) {
                speak(reply)
            } else {
                handler.postDelayed({
                    if (voiceEnabled && !busy) startListening()
                }, 650)
            }
        }
    }

    // TEXT TO SPEECH

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
        orbScaleX = ObjectAnimator.ofFloat(aiOrb, "scaleX", 1f, 1.08f, 1f).apply {
            duration = 1400
            repeatCount = ValueAnimator.INFINITE
            start()
        }
        orbScaleY = ObjectAnimator.ofFloat(aiOrb, "scaleY", 1f, 1.08f, 1f).apply {
            duration = 1400
            repeatCount = ValueAnimator.INFINITE
            start()
        }
    }

    private fun stopOrbAnimation() {
        orbPulse?.cancel()
        orbScaleX?.cancel()
        orbScaleY?.cancel()
        orbPulse = null
        orbScaleX = null
        orbScaleY = null
        if (::aiOrb.isInitialized) aiOrb.animate().rotation(0f).scaleX(1f).scaleY(1f).setDuration(350).start()
    }

    private fun animateCallButton(button: View, action: () -> Unit) {
        button.animate().scaleX(0.93f).scaleY(0.93f).setDuration(90).withEndAction {
            button.animate().scaleX(1f).scaleY(1f).setDuration(130).withEndAction {
                action()
            }.start()
        }.start()
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
        if (android.os.Build.VERSION.SDK_INT < 31 ||
            !SpeechRecognizer.isOnDeviceRecognitionAvailable(this)) {
            voiceEnabled = false
            updateCallButtons()
            statusText.text = "● অফলাইন বাংলা ভয়েস সুবিধা পাওয়া যায়নি"
            statusText.setTextColor(Color.YELLOW)
            Toast.makeText(this,
                "এই ফোনে অন-ডিভাইস Speech Recognition বা বাংলা Offline ভাষা প্যাক পাওয়া যায়নি। Speech Settings-এ বাংলা Offline ভাষা ইনস্টল করুন।",
                Toast.LENGTH_LONG).show()
            return
        }
        try {
            if (recognizer == null) {
                recognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(this)
                recognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        listening = true
                        statusText.text = "● অফলাইনে শুনছি..."
                        statusText.setTextColor(Color.rgb(255, 170, 40))
                    }
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() { listening = false }
                    override fun onError(error: Int) {
                        listening = false
                        if (voiceEnabled && !busy) {
                            if (error == SpeechRecognizer.ERROR_NO_MATCH ||
                                error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                                handler.postDelayed({ startListening() }, 650)
                            } else {
                                voiceEnabled = false
                                updateCallButtons()
                                statusText.text = "● ভয়েস বন্ধ — অফলাইন ভাষা পরীক্ষা করুন"
                                statusText.setTextColor(Color.YELLOW)
                                Toast.makeText(this@MainActivity,
                                    "অফলাইন ভয়েস কাজ করেনি। বাংলা Offline ভাষা প্যাক ও Speech Settings পরীক্ষা করুন।",
                                    Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    override fun onResults(results: Bundle?) {
                        listening = false
                        val words = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spoken = words?.firstOrNull()?.trim()
                        if (!spoken.isNullOrEmpty()) {
                            inputBox.setText(spoken)
                            sendMessage()
                        } else if (voiceEnabled) {
                            handler.postDelayed({ startListening() }, 650)
                        }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-BD")
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            }
            recognizer?.startListening(intent)
        } catch (_: Exception) {
            listening = false
            voiceEnabled = false
            updateCallButtons()
            Toast.makeText(this, "অফলাইন মাইক্রোফোন চালু করা যায়নি। Speech Settings পরীক্ষা করুন।", Toast.LENGTH_LONG).show()
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

    private fun toggleFlashlight(forceState: Boolean? = null) {
        if (checkSelfPermission(Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            pendingTorchState = forceState
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

            torchOn = forceState ?: !torchOn
            pendingTorchState = null
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
                toggleFlashlight(pendingTorchState)
            } else {
                pendingTorchState = null
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
