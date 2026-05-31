package dev.drewett.vista.domain

import androidx.compose.runtime.Immutable

/** A live system notification, captured via our NotificationListenerService. */
@Immutable
data class NotificationItem(
    val key: String,
    val packageName: String,
    val title: String,
    val text: String,
    val postTime: Long,
)
