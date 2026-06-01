package dev.drewett.vista.ui.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import dev.drewett.vista.R
import dev.drewett.vista.data.system.AccountRepository
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class VistaTab(val label: String) {
    FOR_YOU("For you"),
    MOVIES("Movies"),
    SHOWS("Shows"),
    APPS("Apps"),
    LIBRARY("Library"),
}

private val BarHeight = 48.dp

@Composable
fun VistaTopBar(
    selected: VistaTab,
    onSelect: (VistaTab) -> Unit,
    onOpenQuickSettings: () -> Unit,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier,
    barFocusRequester: FocusRequester? = null,
    contentFocus: FocusRequester? = null,
) {
    val scrim = MaterialTheme.colorScheme.background
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(108.dp)
            .background(Brush.verticalGradient(0f to scrim.copy(alpha = 0.9f), 1f to Color.Transparent)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp, vertical = 22.dp)
                .focusGroup()
                // Pressing down from any bar item enters the active screen's content.
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown &&
                        event.key == Key.DirectionDown &&
                        contentFocus != null
                    ) {
                        runCatching { contentFocus.requestFocus(); true }.getOrDefault(false)
                    } else {
                        false
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SegmentedTabs(selected = selected, onSelect = onSelect, barFocusRequester = barFocusRequester)
            Box(Modifier.weight(1f))
            Clock()
            Box(Modifier.size(16.dp))
            IconButton(R.drawable.ic_search, "Search", onClick = onOpenSearch)
            Box(Modifier.size(12.dp))
            IconButton(R.drawable.ic_settings, "Quick settings", onClick = onOpenQuickSettings)
        }
    }
}

@Composable
private fun SegmentedTabs(
    selected: VistaTab,
    onSelect: (VistaTab) -> Unit,
    barFocusRequester: FocusRequester? = null,
) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val density = LocalDensity.current
    val tabs = VistaTab.entries
    val bounds = remember { mutableStateListOf(*Array(tabs.size) { 0f to 0f }) }

    val sel = selected.ordinal
    val (targetXpx, targetWpx) = bounds.getOrElse(sel) { 0f to 0f }
    val pillX by animateDpAsState(with(density) { targetXpx.toDp() }, tween(280), label = "pillX")
    val pillW by animateDpAsState(with(density) { targetWpx.toDp() }, tween(280), label = "pillW")

    Box(
        Modifier
            .focusGroup()
            .height(BarHeight)
            .clip(RoundedCornerShape(50))
            .background(onBg.copy(alpha = 0.12f))
            .padding(4.dp),
    ) {
        if (targetWpx > 0f) {
            Box(
                Modifier
                    .offset(x = pillX)
                    .width(pillW)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(onBg.copy(alpha = 0.92f)),
            )
        }
        Row(Modifier.fillMaxHeight()) {
            tabs.forEachIndexed { i, tab ->
                SegmentTab(
                    label = tab.label,
                    selected = i == sel,
                    onSelect = { onSelect(tab) },
                    onBounds = { x, w -> bounds[i] = x to w },
                    focusRequester = barFocusRequester.takeIf { i == sel },
                )
            }
        }
    }
}

@Composable
private fun SegmentTab(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
    onBounds: (Float, Float) -> Unit,
    focusRequester: FocusRequester? = null,
) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val bg = MaterialTheme.colorScheme.background
    Box(
        Modifier
            .fillMaxHeight()
            .onGloballyPositioned { onBounds(it.positionInParent().x, it.size.width.toFloat()) }
            .onFocusChanged { if (it.hasFocus) onSelect() },
    ) {
        Surface(
            onClick = onSelect,
            modifier = (focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier).fillMaxHeight(),
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(50)),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                pressedContainerColor = Color.Transparent,
                contentColor = if (selected) bg else onBg.copy(alpha = 0.6f),
                focusedContentColor = if (selected) bg else onBg,
                pressedContentColor = if (selected) bg else onBg,
            ),
        ) {
            Box(
                modifier = Modifier.fillMaxHeight().padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium.copy(
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                    ),
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun IconButton(iconRes: Int, contentDescription: String, onClick: () -> Unit) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    var focused by remember { mutableStateOf(false) }
    Surface(
        onClick = onClick,
        modifier = Modifier.size(40.dp).onFocusChanged { focused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = onBg.copy(alpha = 0.12f),
            focusedContainerColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(22.dp),
                colorFilter = ColorFilter.tint(if (focused) onPrimary else onBg),
            )
        }
    }
}

@Composable
private fun Clock() {
    var time by remember { mutableStateOf(currentTime()) }
    LaunchedEffect(Unit) {
        while (true) {
            time = currentTime()
            delay(20_000)
        }
    }
    Text(
        text = time,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

private fun currentTime(): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
