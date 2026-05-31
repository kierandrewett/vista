package dev.drewett.vista.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Glow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import dev.drewett.vista.domain.AppEntry

/** Google-TV-style circular app icon for the home app rows. */
@Composable
fun CircleAppCard(
    entry: AppEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    flatFocus: Boolean = false,
) {
    Surface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier.aspectRatio(1f),
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = if (flatFocus) 1f else 1.08f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(BorderStroke(if (flatFocus) 4.dp else 3.dp, MaterialTheme.colorScheme.primary), shape = CircleShape),
        ),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = Glow(MaterialTheme.colorScheme.primary, 12.dp),
        ),
    ) {
        DrawableImage(
            drawable = entry.icon,
            contentDescription = entry.label,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}
