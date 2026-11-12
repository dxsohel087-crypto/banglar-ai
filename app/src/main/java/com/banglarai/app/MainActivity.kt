
package com.banglarai.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.rgb(5, 15, 12))
            setPadding(24, 24, 24, 24)
        }

        val title = TextView(this).apply {
            text = "বাংলার AI"
            textSize = 32f
            setTextColor(Color.rgb(0, 255, 130))
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "আপনার বাংলা AI সহকারী প্রস্তুত হচ্ছে..."
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        layout.addView(title)
        layout.addView(subtitle)

        setContentView(layout)
    }
}
