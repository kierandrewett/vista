package dev.drewett.vista.domain

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable

/**
 * A real piece of content surfaced from another app's published TV channel / watch-next entry,
 * read via READ_TV_LISTINGS. Tapping it fires the publisher's own deep-link [intentUri].
 */
@Immutable
data class ContentCard(
    val id: Long,
    val title: String,
    val description: String?,
    val posterArtUri: String?,
    val intentUri: String?,
    val packageName: String?,
    val providerIcon: Drawable?,
    val seasonNumber: String?,
    val episodeNumber: String?,
    val episodeTitle: String?,
    val reviewRating: String?,
    val reviewRatingStyle: Int,
    val releaseDate: String?,
    /** TvContract program type (TYPE_MOVIE=0, TYPE_TV_SERIES=3, SEASON=4, EPISODE=5, CLIP=6…). */
    val type: Int,
    /** TvContract poster aspect ratio code (16:9=0, 3:2=1, 4:3=2, 1:1=3, 2:3=4, movie-poster=5). */
    val aspectRatioCode: Int,
    val durationMillis: Int,
    val positionMillis: Int,
    val lastEngagement: Long,
) {
    /** width / height for this poster. */
    val aspectRatioFloat: Float
        get() = when (aspectRatioCode) {
            4 -> 2f / 3f
            5 -> 1f / 1.441f
            1 -> 3f / 2f
            2 -> 4f / 3f
            3 -> 1f
            else -> 16f / 9f
        }

    /** Formatted rating per its style (stars 0, thumbs 1, percentage 2). */
    val ratingLabel: String?
        get() {
            val value = reviewRating?.toFloatOrNull() ?: return null
            return when (reviewRatingStyle) {
                0 -> "★ ${"%.1f".format(value)}"          // RATING_STYLE_STARS (0..5)
                2 -> "${value.toInt()}%"                   // RATING_STYLE_PERCENTAGE (0..100)
                1 -> if (value >= 0.5f) "👍" else "👎"      // RATING_STYLE_THUMBS
                else -> null
            }
        }

    val isMovie: Boolean get() = type == 0
    val isShow: Boolean get() = type in intArrayOf(3, 4, 5)

    /** Resume progress 0f..1f when the publisher provides position + duration. */
    val progress: Float?
        get() = if (durationMillis > 0 && positionMillis in 1..durationMillis) {
            positionMillis.toFloat() / durationMillis
        } else {
            null
        }

    private val remainingMillis: Int?
        get() = if (durationMillis > 0 && positionMillis in 0..durationMillis) {
            durationMillis - positionMillis
        } else {
            null
        }

    /** e.g. "S1 · E5" or null for films. */
    val episodeLabel: String?
        get() = when {
            !seasonNumber.isNullOrBlank() && !episodeNumber.isNullOrBlank() -> "S$seasonNumber · E$episodeNumber"
            !episodeNumber.isNullOrBlank() -> "E$episodeNumber"
            else -> null
        }

    /** e.g. "23 min left" or "1h 12m left". */
    val timeLeftLabel: String?
        get() {
            val ms = remainingMillis ?: return null
            val totalMin = ms / 60_000
            if (totalMin <= 0) return null
            val h = totalMin / 60
            val m = totalMin % 60
            return when {
                h > 0 -> "${h}h ${m}m left"
                else -> "$m min left"
            }
        }

    /** Combined one-liner for the card / hero: "S1 · E5 · 23 min left · The Episode". */
    val metadataLine: String?
        get() = listOfNotNull(episodeLabel, timeLeftLabel, episodeTitle?.takeIf { it.isNotBlank() })
            .takeIf { it.isNotEmpty() }
            ?.joinToString("  ·  ")
}

/** The dominant poster aspect in a row, so all items can be sized consistently. */
fun List<ContentCard>.dominantAspectRatio(): Float {
    if (isEmpty()) return 16f / 9f
    return groupingBy { it.aspectRatioFloat }.eachCount().maxByOrNull { it.value }?.key ?: 16f / 9f
}
