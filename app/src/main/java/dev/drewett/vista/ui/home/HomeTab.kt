package dev.drewett.vista.ui.home

import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import dev.drewett.vista.domain.AppEntry
import dev.drewett.vista.domain.AppSection
import dev.drewett.vista.domain.ContentCard
import dev.drewett.vista.domain.ContentSection
import dev.drewett.vista.domain.HomeSection
import dev.drewett.vista.domain.Spotlight
import dev.drewett.vista.domain.dominantAspectRatio
import dev.drewett.vista.ui.components.CircleAppCard
import dev.drewett.vista.ui.components.ContentCardView
import dev.drewett.vista.ui.components.DrawableImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val CardWidth = 232.dp
private val ContentImageHeight = 132.dp
private val IconSize = 104.dp
private val RowEdgePadding = 48.dp
private val TopBarClearance = 104.dp
private val CardShape = RoundedCornerShape(14.dp)

// Fires content focus once per process so tab sweeps don't get their focus stolen.
private var initialFocusDone = false

// Anchors the focused item so its leading edge lands exactly at the content padding — the row
// (and column) slide under a fixed focus ring, respecting the left/top padding.
@OptIn(ExperimentalFoundationApi::class)
private fun leadingSpec(padPx: Float) = object : BringIntoViewSpec {
    override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float = offset - padPx
}

@Composable
fun HomeTab(
    sections: List<HomeSection>,
    favourites: Set<String>,
    onLaunch: (AppEntry) -> Unit,
    onLaunchContent: (ContentCard) -> Unit,
    onToggleFavourite: (AppEntry) -> Unit,
    topBarFocus: FocusRequester,
    resetSignal: Int,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf<Spotlight?>(null) }
    val heroItem = focused ?: sections.firstOrNull()?.let { firstSpotlightOf(it) }

    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Immersive full-bleed hero behind everything.
        HeroBackground(heroItem)
        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.height(TopBarClearance))
            HeroInfo(
                item = heroItem,
                modifier = Modifier.padding(start = RowEdgePadding, end = 380.dp),
            )
            Spacer(Modifier.weight(1f))
            SectionsList(
                sections = sections,
                onFocus = { focused = it },
                onLaunch = onLaunch,
                onLaunchContent = onLaunchContent,
                onToggleFavourite = onToggleFavourite,
                topBarFocus = topBarFocus,
                resetSignal = resetSignal,
                modifier = Modifier.height(330.dp),
            )
        }
    }
}

@Composable
private fun HeroBackground(item: Spotlight?) {
    val bg = MaterialTheme.colorScheme.background
    val ambient = rememberAmbient(item?.banner)
    val wash by animateColorAsState(ambient ?: bg, tween(600), label = "wash")

    Box(Modifier.fillMaxSize()) {
        Crossfade(targetState = item, animationSpec = tween(450), label = "heroBg") { current ->
            when {
                current?.posterUri != null -> AsyncImage(
                    model = current.posterUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().blur(22.dp),
                    contentScale = ContentScale.Crop,
                )

                // Apps have no high-res art — use an ambient colour wash from the icon/banner.
                current?.banner != null -> Box(
                    Modifier.fillMaxSize().background(
                        Brush.radialGradient(
                            colors = listOf(wash.copy(alpha = 0.55f), Color.Transparent),
                            radius = 1400f,
                        ),
                    ),
                )
            }
        }
        // Scrims: left + bottom fade to background so text and rows stay readable.
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(0f to bg, 0.5f to Color.Transparent)))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.35f to Color.Transparent, 1f to bg)))
    }
}

@Composable
private fun HeroInfo(item: Spotlight?, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = item,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
        label = "heroInfo",
        modifier = modifier,
    ) { current ->
        if (current != null) {
            Column {
                Text(
                    text = current.subtitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = current.title,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 44.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 3,
                )
                if (current.metadata != null) {
                    Text(
                        text = current.metadata,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                if (current.description != null) {
                    Text(
                        text = current.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SectionsList(
    sections: List<HomeSection>,
    onFocus: (Spotlight) -> Unit,
    onLaunch: (AppEntry) -> Unit,
    onLaunchContent: (ContentCard) -> Unit,
    onToggleFavourite: (AppEntry) -> Unit,
    topBarFocus: FocusRequester,
    resetSignal: Int,
    modifier: Modifier = Modifier,
) {
    val firstItem = remember { FocusRequester() }
    var focusedRow by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val rowPadPx = with(density) { RowEdgePadding.toPx() }
    val colPadPx = with(density) { 4.dp.toPx() }

    LaunchedEffect(sections.firstOrNull()?.key) {
        if (sections.isNotEmpty() && !initialFocusDone) {
            runCatching { firstItem.requestFocus() }
            initialFocusDone = true
        }
    }
    LaunchedEffect(resetSignal) {
        if (resetSignal > 0 && sections.isNotEmpty()) {
            runCatching { listState.scrollToItem(0) }
            runCatching { firstItem.requestFocus() }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .focusProperties {
                exit = { direction -> if (direction == FocusDirection.Up) topBarFocus else FocusRequester.Default }
            },
        contentPadding = PaddingValues(top = 4.dp, bottom = 56.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        itemsIndexed(sections, key = { _, s -> s.key }) { sectionIndex, section ->
            val rowFocused = focusedRow == section.key
            val rowAlpha by animateFloatAsState(
                if (focusedRow == null || rowFocused) 1f else 0.34f, tween(250), label = "rowAlpha",
            )

            // All items in a content row share the row's dominant poster aspect.
            val contentAspect = (section as? ContentSection)?.items?.dominantAspectRatio() ?: (16f / 9f)
            val contentCardW = ContentImageHeight * contentAspect

            Column(
                modifier = Modifier
                    .alpha(rowAlpha)
                    .onFocusChanged { if (it.hasFocus) focusedRow = section.key },
            ) {
                RowHeader(section)
                // Anchored scroll keeps the focused card at the leading pivot (ring looks fixed),
                // but at the end of the row the list can't scroll further so the ring moves right.
                CompositionLocalProvider(LocalBringIntoViewSpec provides leadingSpec(rowPadPx)) {
                    LazyRow(
                            contentPadding = PaddingValues(horizontal = RowEdgePadding),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .padding(vertical = 10.dp)
                                .focusGroup()
                                .focusProperties {
                                    exit = { dir ->
                                        if (dir == FocusDirection.Left || dir == FocusDirection.Right) {
                                            FocusRequester.Cancel
                                        } else {
                                            FocusRequester.Default
                                        }
                                    }
                                },
                        ) {
                            when (section) {
                                is AppSection -> itemsIndexed(section.items, key = { _, a -> a.packageName }) { itemIndex, app ->
                                    CardSlot(firstItem.takeIf { sectionIndex == 0 && itemIndex == 0 }) {
                                        CircleAppCard(
                                            entry = app,
                                            onClick = { onLaunch(app) },
                                            onLongClick = { onToggleFavourite(app) },
                                            flatFocus = true,
                                            modifier = Modifier.size(IconSize).cardFocus { onFocus(app.toSpotlight()) },
                                        )
                                    }
                                }

                                is ContentSection -> itemsIndexed(section.items, key = { _, c -> c.id }) { itemIndex, card ->
                                    CardSlot(firstItem.takeIf { sectionIndex == 0 && itemIndex == 0 }) {
                                        ContentCardView(
                                            card = card,
                                            onClick = { onLaunchContent(card) },
                                            flatFocus = true,
                                            aspectRatio = contentAspect,
                                            modifier = Modifier.width(contentCardW).cardFocus { onFocus(card.toSpotlight(section.title)) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

@Composable
private fun RowHeader(section: HomeSection) {
    val providerIcon = (section as? ContentSection)?.providerIcon
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = RowEdgePadding, bottom = 8.dp),
    ) {
        if (providerIcon != null) {
            DrawableImage(
                drawable = providerIcon,
                contentDescription = null,
                modifier = Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit,
            )
            Spacer(Modifier.width(12.dp))
        }
        Text(
            text = section.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun CardSlot(focusRequester: FocusRequester?, content: @Composable () -> Unit) {
    Box(modifier = if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier) {
        content()
    }
}

@Composable
private fun rememberAmbient(banner: Drawable?): Color? {
    return produceState<Color?>(initialValue = null, banner) {
        value = banner?.let { drawable ->
            withContext(Dispatchers.Default) {
                runCatching {
                    val bmp = drawable.toBitmap(width = 96, height = 54, config = Bitmap.Config.ARGB_8888)
                    val palette = Palette.from(bmp).generate()
                    val c = palette.getVibrantColor(palette.getMutedColor(palette.getDominantColor(0)))
                    if (c != 0) Color(c) else null
                }.getOrNull()
            }
        }
    }.value
}

private fun Modifier.cardFocus(onFocused: () -> Unit): Modifier =
    this.onFocusChanged { if (it.hasFocus) onFocused() }

private fun firstSpotlightOf(section: HomeSection): Spotlight? = when (section) {
    is AppSection -> section.items.firstOrNull()?.toSpotlight()
    is ContentSection -> section.items.firstOrNull()?.toSpotlight(section.title)
}

private fun AppEntry.toSpotlight() = Spotlight(label, categoryLabel(this), null, null, banner, null)

private fun ContentCard.toSpotlight(sectionTitle: String) =
    Spotlight(title, sectionTitle, metadataLine, description, null, posterArtUri)

private fun categoryLabel(entry: AppEntry): String = when {
    entry.isGame -> "Game"
    entry.category == ApplicationInfo.CATEGORY_VIDEO -> "Video"
    entry.category == ApplicationInfo.CATEGORY_AUDIO -> "Music"
    entry.category == ApplicationInfo.CATEGORY_SOCIAL -> "Social"
    entry.category == ApplicationInfo.CATEGORY_NEWS -> "News"
    entry.isLeanback -> "TV app"
    else -> "App"
}
