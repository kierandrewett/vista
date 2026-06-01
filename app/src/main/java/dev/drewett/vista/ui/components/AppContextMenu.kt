package dev.drewett.vista.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import dev.drewett.vista.domain.AppEntry

private val MenuShape = RoundedCornerShape(20.dp)
private val ItemShape = RoundedCornerShape(12.dp)

/** Long-press menu for an app: open, pin/unpin, app info, uninstall. */
@Composable
fun AppContextMenu(
    app: AppEntry?,
    isFavourite: Boolean,
    onDismiss: () -> Unit,
    onOpen: (AppEntry) -> Unit,
    onToggleFavourite: (AppEntry) -> Unit,
    onAppInfo: (String) -> Unit,
    onUninstall: (String) -> Unit,
) {
    app ?: return
    BackHandler { onDismiss() }
    val firstItem = remember { FocusRequester() }
    LaunchedEffect(app.packageName) { runCatching { firstItem.requestFocus() } }

    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .width(420.dp)
                .background(MaterialTheme.colorScheme.surface, MenuShape)
                .focusGroup()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                DrawableImage(
                    drawable = app.icon,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                )
                Box(Modifier.width(14.dp))
                Text(
                    app.label,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(Modifier.size(0.dp, 8.dp))
            MenuItem("Open", Modifier.focusRequester(firstItem)) { onOpen(app); onDismiss() }
            MenuItem(if (isFavourite) "Remove from favourites" else "Add to favourites") {
                onToggleFavourite(app); onDismiss()
            }
            MenuItem("App info") { onAppInfo(app.packageName); onDismiss() }
            MenuItem("Uninstall") { onUninstall(app.packageName); onDismiss() }
        }
    }
}

@Composable
private fun MenuItem(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(ItemShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onSurface,
            focusedContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
        )
    }
}
