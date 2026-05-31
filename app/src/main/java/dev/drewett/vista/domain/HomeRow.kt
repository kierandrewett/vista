package dev.drewett.vista.domain

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable

enum class RowType { CONTINUE_WATCHING, JUMP_BACK_IN, FAVOURITES, WATCH, LISTEN, PLAY, FEATURED, APPS }

/** A section on the Home tab — either a row of apps or a row of real content. */
sealed interface HomeSection {
    val key: String
    val title: String
}

@Immutable
data class AppSection(
    val type: RowType,
    override val title: String,
    val items: List<AppEntry>,
) : HomeSection {
    override val key: String get() = "app:$type"
}

@Immutable
data class ContentSection(
    override val key: String,
    override val title: String,
    val items: List<ContentCard>,
    /** Owning app's icon for the row header, when the row is single-provider. */
    val providerIcon: Drawable? = null,
) : HomeSection

/** What the immersive hero spotlights — unified across app tiles and content cards. */
@Immutable
data class Spotlight(
    val title: String,
    val subtitle: String,
    val metadata: String?,
    val description: String?,
    val banner: Drawable?,
    val posterUri: String?,
)
