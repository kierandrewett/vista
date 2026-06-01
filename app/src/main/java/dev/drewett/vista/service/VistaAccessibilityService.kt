package dev.drewett.vista.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import dev.drewett.vista.QuickSettingsActivity

/**
 * Captures the remote's Settings/dashboard key globally so the Vista quick-settings panel opens
 * over any app — not just the launcher. Enabled via adb (it can't be requested at runtime on TV).
 */
class VistaAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_SETTINGS) {
            if (event.action == KeyEvent.ACTION_DOWN) {
                runCatching {
                    startActivity(
                        Intent(this, QuickSettingsActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }
            }
            return true // consume both down and up so the system dashboard doesn't also open
        }
        return false
    }
}
