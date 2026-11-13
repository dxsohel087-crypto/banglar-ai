package com.banglarai.app

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * Opt-in accessibility extension point.
 * This initial version deliberately does not inspect or control other apps.
 */
class BanglarAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No cross-app content is collected or acted upon in this version.
    }

    override fun onInterrupt() {
        // Nothing to interrupt.
    }
}
