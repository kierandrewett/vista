package dev.drewett.vista.data.channels

import android.content.Context
import android.content.Intent
import android.database.Cursor
import androidx.tvprovider.media.tv.PreviewProgram
import androidx.tvprovider.media.tv.TvContractCompat
import androidx.tvprovider.media.tv.WatchNextProgram
import dev.drewett.vista.domain.ContentCard

/**
 * Reads other apps' published TV content via the TvProvider (requires READ_TV_LISTINGS, granted
 * over adb). Watch-next entries → "Continue watching"; preview programs → content rows.
 *
 * Honest limitation: the single system-aggregated watch-next table needs ACCESS_ALL_EPG_DATA
 * (signature|privileged, held only by the stock launcher). We read what a non-privileged launcher
 * can — apps' own published channels/programs — and degrade gracefully to empty otherwise.
 */
class ChannelsRepository(private val context: Context) {

    private val resolver get() = context.contentResolver
    private val iconCache = HashMap<String, android.graphics.drawable.Drawable?>()

    private fun iconFor(packageName: String?): android.graphics.drawable.Drawable? {
        packageName ?: return null
        return iconCache.getOrPut(packageName) {
            val pm = context.packageManager
            runCatching {
                val ai = pm.getApplicationInfo(packageName, 0)
                if (ai.icon != 0) {
                    pm.getResourcesForApplication(ai)
                        .getDrawableForDensity(ai.icon, android.util.DisplayMetrics.DENSITY_XXXHIGH, null)
                } else {
                    null
                }
            }.getOrNull() ?: runCatching { pm.getApplicationIcon(packageName) }.getOrNull()
        }
    }

    fun loadContinueWatching(): List<ContentCard> {
        val cards = query(TvContractCompat.WatchNextPrograms.CONTENT_URI, WatchNextProgram.PROJECTION) { c ->
            val p = WatchNextProgram.fromCursor(c)
            ContentCard(
                id = p.id,
                title = p.title.orEmpty(),
                description = p.description ?: p.longDescription,
                posterArtUri = p.posterArtUri?.toString(),
                intentUri = p.intentUri?.toString(),
                packageName = p.packageName,
                providerIcon = iconFor(p.packageName),
                seasonNumber = p.seasonNumber,
                episodeNumber = p.episodeNumber,
                episodeTitle = p.episodeTitle,
                reviewRating = runCatching { p.reviewRating }.getOrNull(),
                reviewRatingStyle = runCatching { p.reviewRatingStyle }.getOrDefault(-1),
                releaseDate = p.releaseDate,
                type = runCatching { p.type }.getOrDefault(-1),
                aspectRatioCode = runCatching { p.posterArtAspectRatio }.getOrDefault(-1),
                durationMillis = p.durationMillis,
                positionMillis = p.lastPlaybackPositionMillis,
                lastEngagement = p.lastEngagementTimeUtcMillis,
            )
        }.sortedByDescending { it.lastEngagement }
        return cards
    }

    fun loadPreviewPrograms(): List<ContentCard> {
        val cards = query(TvContractCompat.PreviewPrograms.CONTENT_URI, PreviewProgram.PROJECTION) { c ->
            val p = PreviewProgram.fromCursor(c)
            ContentCard(
                id = p.id,
                title = p.title.orEmpty(),
                description = p.description ?: p.longDescription,
                posterArtUri = p.posterArtUri?.toString(),
                intentUri = p.intentUri?.toString(),
                packageName = p.packageName,
                providerIcon = iconFor(p.packageName),
                seasonNumber = p.seasonNumber,
                episodeNumber = p.episodeNumber,
                episodeTitle = p.episodeTitle,
                reviewRating = runCatching { p.reviewRating }.getOrNull(),
                reviewRatingStyle = runCatching { p.reviewRatingStyle }.getOrDefault(-1),
                releaseDate = p.releaseDate,
                type = runCatching { p.type }.getOrDefault(-1),
                aspectRatioCode = runCatching { p.posterArtAspectRatio }.getOrDefault(-1),
                durationMillis = p.durationMillis,
                positionMillis = p.lastPlaybackPositionMillis,
                lastEngagement = 0L,
            )
        }
        return cards
    }

    /** Fire the publisher's own deep-link to resume/open the content. */
    fun launchContent(card: ContentCard): Boolean {
        val uri = card.intentUri ?: return false
        return runCatching {
            val intent = Intent.parseUri(uri, Intent.URI_INTENT_SCHEME)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    private inline fun query(
        uri: android.net.Uri,
        projection: Array<String>,
        map: (Cursor) -> ContentCard,
    ): List<ContentCard> {
        val out = ArrayList<ContentCard>()
        runCatching {
            resolver.query(uri, projection, null, null, null)?.use { c ->
                while (c.moveToNext()) {
                    runCatching { map(c) }.getOrNull()
                        ?.takeIf { it.title.isNotBlank() }
                        ?.let(out::add)
                }
            }
        }.onFailure { android.util.Log.w("VistaDiag", "query $uri failed: ${it.message}") }
        return out
    }
}
