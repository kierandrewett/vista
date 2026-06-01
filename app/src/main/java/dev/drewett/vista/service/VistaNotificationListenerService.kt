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

    override fun onListenerConnected() = refresh()

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        refresh()
        sbn ?: return
        // Only pop a card over OTHER apps, and skip ongoing/media notifications.
        if (VistaForeground.isForeground || sbn.isOngoing) return
        val item = toItem(sbn) ?: return
        val icon = runCatching { packageManager.getApplicationIcon(sbn.packageName) }.getOrNull()
        overlay.show(item, icon)
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

    /** Strip any HTML markup some apps embed in notification text (e.g. <font bgcolor=…>). */
    private fun plain(cs: CharSequence?): String {
        val s = cs?.toString()?.trim().orEmpty()
        return if ('<' in s && '>' in s) {
            androidx.core.text.HtmlCompat.fromHtml(s, androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY)
                .toString().trim()
        } else {
            s
        }
    }
}
