package com.banglarai.app

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.ArrayDeque
import java.util.Locale

/**
 * Optional, user-enabled accessibility bridge.
 * It reads visible text or clicks a matching visible item only after an explicit user command.
 * It does not collect or upload screen content in the background.
 */
class BanglarAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile private var activeService: BanglarAccessibilityService? = null

        fun readVisibleText(): String? = activeService?.readCurrentWindow()

        fun clickVisibleText(label: String): Boolean =
            activeService?.clickMatchingText(label) ?: false
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        activeService = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // The screen is inspected only when readVisibleText/clickVisibleText is called
        // in response to a direct user command.
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        if (activeService === this) activeService = null
        super.onDestroy()
    }

    private fun readCurrentWindow(): String? {
        val root = rootInActiveWindow ?: return null
        val lines = LinkedHashSet<String>()
        walk(root) { node ->
            val label = node.text?.toString()?.trim()
            val description = node.contentDescription?.toString()?.trim()
            if (!label.isNullOrBlank()) lines.add(label)
            if (!description.isNullOrBlank()) lines.add(description)
        }
        return lines.take(100).joinToString("\\n").takeIf { it.isNotBlank() }
    }

    private fun clickMatchingText(label: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val wanted = label.trim().lowercase(Locale.ROOT)
        if (wanted.isBlank()) return false
        var clicked = false
        walk(root) { node ->
            if (!clicked) {
                val text = node.text?.toString()?.trim()?.lowercase(Locale.ROOT).orEmpty()
                val description = node.contentDescription?.toString()
                    ?.trim()?.lowercase(Locale.ROOT).orEmpty()
                if (text == wanted || description == wanted ||
                    (wanted.length >= 3 && (text.contains(wanted) || description.contains(wanted)))) {
                    var candidate: AccessibilityNodeInfo? = AccessibilityNodeInfo.obtain(node)
                    while (candidate != null && !candidate.isClickable) {
                        val parent = candidate.parent
                        candidate.recycle()
                        candidate = parent
                    }
                    if (candidate != null) {
                        clicked = candidate.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        candidate.recycle()
                    }
                }
            }
        }
        return clicked
    }

    private fun walk(root: AccessibilityNodeInfo, action: (AccessibilityNodeInfo) -> Unit) {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(AccessibilityNodeInfo.obtain(root))
        var visited = 0
        while (queue.isNotEmpty() && visited < 600) {
            val node = queue.removeFirst()
            visited++
            action(node)
            for (index in 0 until node.childCount) {
                val child = node.getChild(index)
                if (child != null) queue.add(child)
            }
            node.recycle()
        }
    }
}
