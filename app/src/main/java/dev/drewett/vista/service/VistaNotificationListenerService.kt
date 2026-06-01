package dev.drewett.vista.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dev.drewett.vista.data.notifications.NotificationRepository
import dev.drewett.vista.domain.NotificationItem

/**
 * Captures live system notifications for the quick-settings panel. Enabled via adb
 * (settings put secure enabled_notification_listeners …) since users can't easily grant it on TV.
 */
class VistaNotificationListenerService : NotificationListenerService() {

    private val overlay by lazy { NotificationOverlay(this) }
    private var lastOverlayKey: String? = null

    override fun onListenerConnected() = refresh()

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        refresh()
        sbn ?: return
        if (VistaForeground.isForeground) return
        if (!shouldOverlay(sbn)) return
        if (sbn.key == lastOverlayKey) return // don't re-pop the same notification on each update
        val item = toItem(sbn) ?: return
        lastOverlayKey = sbn.key
        val icon = runCatching { packageManager.getApplicationIcon(sbn.packageName) }.getOrNull()
        overlay.show(item, icon)
    }

    /** Skip ongoing, media/transport, and service notifications — only real, dismissible alerts pop up. */
    private fun shouldOverlay(sbn: StatusBarNotification): Boolean {
        if (sbn.isOngoing) return false
        val n = sbn.notification ?: return false
        if (n.category in SUPPRESSED_CATEGORIES) return false
        if (n.extras?.containsKey(Notification.EXTRA_MEDIA_SESSION) == true) return false
        if (n.flags and Notification.FLAG_FOREGROUND_SERVICE != 0) return false
        return true
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) = refresh()

    private fun refresh() {
        val items = runCatching { activeNotifications }.getOrNull()
            ?.mapNotNull(::toItem)
            .orEmpty()
        NotificationRepository.submit(items)
    }

    private fun toItem(sbn: StatusBarNotification): NotificationItem? {
        if (sbn.packageName == packageName) return null
        val extras = sbn.notification?.extras ?: return null
        val title = plain(extras.getCharSequence(Notification.EXTRA_TITLE)).takeIf { it.isNotBlank() }
            ?: return null
        val text = plain(extras.getCharSequence(Notification.EXTRA_TEXT))
        return NotificationItem(
            key = sbn.key,
            packageName = sbn.packageName,
            title = title,
            text = text,
            postTime = sbn.postTime,
        )
    }

    /** Strip HTML some apps embed (e.g. <font bgcolor=…>) and drop junk placeholders. */
    private fun plain(cs: CharSequence?): String {
        var s = cs?.toString()?.trim().orEmpty()
        if ('<' in s && '>' in s) {
            s = androidx.core.text.HtmlCompat.fromHtml(s, androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY)
                .toString().trim()
        }
        return if (s.equals("undefined", ignoreCase = true) || s.equals("null", ignoreCase = true)) "" else s
    }

    private companion object {
        val SUPPRESSED_CATEGORIES = setOf(
            Notification.CATEGORY_TRANSPORT,
            Notification.CATEGORY_PROGRESS,
            Notification.CATEGORY_SERVICE,
            Notification.CATEGORY_CALL,
            Notification.CATEGORY_NAVIGATION,
        )
    }
}
