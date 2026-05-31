package dev.drewett.vista.ui.quicksettings

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import dev.drewett.vista.R
import dev.drewett.vista.data.notifications.NotificationRepository
import dev.drewett.vista.domain.ThemeMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PanelShape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)
private val TileShape = RoundedCornerShape(16.dp)

@Composable
fun QuickSettingsPanel(
    visible: Boolean,
    themeMode: ThemeMode,
    onCycleTheme: () -> Unit,
    onDismiss: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(visible, Modifier.fillMaxSize(), enter = fadeIn(tween(220)), exit = fadeOut(tween(200))) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
        }
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.align(Alignment.CenterEnd),
            enter = slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)),
            exit = slideOutHorizontally(tween(250)) { it } + fadeOut(tween(200)),
        ) {
            PanelContent(themeMode, onCycleTheme, onDismiss)
        }
    }
}

@Composable
private fun PanelContent(themeMode: ThemeMode, onCycleTheme: () -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val notifications by NotificationRepository.active.collectAsStateWithLifecycle()
    val firstTile = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { firstTile.requestFocus() } }

    val audioRepository = remember { dev.drewett.vista.data.system.AudioRepository(context) }
    var showAudio by remember { mutableStateOf(false) }
    var outputs by remember { mutableStateOf<List<dev.drewett.vista.data.system.AudioOutput>>(emptyList()) }

    fun open(action: String) {
        runCatching { context.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        onDismiss()
    }

    Column(
        modifier = Modifier
            .width(440.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface, PanelShape)
            .focusGroup()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 30.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(dateLine(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(timeLine(), style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onSurface)

        Tile("Theme", themeMode.label, R.drawable.ic_theme, Modifier.fillMaxWidth().focusRequester(firstTile), onClick = onCycleTheme)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GridTile("Wi-Fi", R.drawable.ic_wifi) { open(Settings.ACTION_WIFI_SETTINGS) }
            GridTile("Bluetooth", R.drawable.ic_bluetooth) { open(Settings.ACTION_BLUETOOTH_SETTINGS) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GridTile("Audio output", R.drawable.ic_audio) {
                outputs = audioRepository.outputs()
                showAudio = !showAudio
            }
            GridTile("Display", R.drawable.ic_display) { open(Settings.ACTION_DISPLAY_SETTINGS) }
        }
        if (showAudio) {
            Text(
                "Audio output",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            if (outputs.isEmpty()) {
                Text(
                    "Only the default output is available. Connect a Bluetooth device to switch.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            } else {
                outputs.forEach { out ->
                    Tile(out.name, if (out.selected) "●" else "○", R.drawable.ic_audio, Modifier.fillMaxWidth()) {
                        audioRepository.select(out.route)
                        outputs = audioRepository.outputs()
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GridTile("Accessibility", R.drawable.ic_accessibility) { open(Settings.ACTION_ACCESSIBILITY_SETTINGS) }
            GridTile("Date & time", R.drawable.ic_clock) { open(Settings.ACTION_DATE_SETTINGS) }
        }
        Tile("All settings", "Open", R.drawable.ic_settings, Modifier.fillMaxWidth()) { open(Settings.ACTION_SETTINGS) }

        Text(
            "Notifications",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 10.dp),
        )
        if (notifications.isEmpty()) {
            Text(
                "No new notifications",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        } else {
            notifications.take(6).forEach { n ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, TileShape)
                        .padding(14.dp),
                ) {
                    Text(n.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (n.text.isNotBlank()) {
                        Text(n.text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.GridTile(label: String, iconRes: Int, onClick: () -> Unit) {
    Tile(label, null, iconRes, Modifier.weight(1f).height(84.dp), onClick)
}

@Composable
private fun Tile(label: String, value: String?, iconRes: Int?, modifier: Modifier = Modifier, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val content = if (focused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Surface(
        onClick = onClick,
        modifier = modifier.onFocusChanged { focused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(TileShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onSurface,
            focusedContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary), shape = TileShape),
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (iconRes != null) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    colorFilter = ColorFilter.tint(content),
                )
                Spacer(Modifier.width(14.dp))
            }
            Text(label, style = MaterialTheme.typography.titleMedium, color = content, modifier = Modifier.weight(1f))
            if (value != null) {
                Text(value, style = MaterialTheme.typography.bodyLarge, color = content)
            }
        }
    }
}

private fun dateLine(): String = SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(Date())
private fun timeLine(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
