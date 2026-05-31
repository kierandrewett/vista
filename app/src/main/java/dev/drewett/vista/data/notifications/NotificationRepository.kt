package dev.drewett.vista.data.notifications

import dev.drewett.vista.domain.NotificationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Process-wide store of live notifications, fed by [VistaNotificationListenerService]. */
object NotificationRepository {
    private val _active = MutableStateFlow<List<NotificationItem>>(emptyList())
    val active = _active.asStateFlow()

    fun submit(items: List<NotificationItem>) {
        _active.value = items.sortedByDescending { it.postTime }
    }
}
