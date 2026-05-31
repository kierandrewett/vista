package dev.drewett.vista.domain

import android.content.ComponentName
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable

/** A launchable app, sourced live from PackageManager — nothing hardcoded. */
@Immutable
data class AppEntry(
    val packageName: String,
    val componentName: ComponentName,
    val label: String,
    /** 16:9 leanback banner if the app ships one; null for plain phone/sideloaded apps. */
    val banner: Drawable?,
    /** Always present; used as fallback art and in the icon grid. */
    val icon: Drawable,
    /** ApplicationInfo.category (VIDEO/AUDIO/GAME/SOCIAL/NEWS/…) or UNDEFINED. */
    val category: Int,
    /** True when the app advertises a real TV (LEANBACK_LAUNCHER) entry point. */
    val isLeanback: Boolean,
) {
    val isGame: Boolean get() = category == ApplicationInfo.CATEGORY_GAME
    val hasBanner: Boolean get() = banner != null
}
