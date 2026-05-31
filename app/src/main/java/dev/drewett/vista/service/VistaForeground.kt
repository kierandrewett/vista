package dev.drewett.vista.service

/** Tracks whether Vista itself is in the foreground, so we only overlay notifications over OTHER apps. */
object VistaForeground {
    @Volatile
    var isForeground: Boolean = false
}
