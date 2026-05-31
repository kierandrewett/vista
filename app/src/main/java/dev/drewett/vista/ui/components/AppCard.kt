package dev.drewett.vista.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Glow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import dev.drewett.vista.domain.AppEntry

private val CardShape = RoundedCornerShape(14.dp)

/**
 * A focusable app tile. tv-material [Surface] gives us the premium focus treatment for free:
 * scale-up, glowing accent border, elevation — all animated on D-pad focus.
 */
@Composable
fun AppCard(
    entry: AppEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    flatFocus: Boolean = false,
) {
    Surface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier.aspectRatio(16f / 9f),
        shape = ClickableSurfaceDefaults.shape(CardShape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = if (flatFocus) 1f else 1.07f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = if (flatFocus) {
                Border.None
            } else {
                Border(BorderStroke(3.dp, MaterialTheme.colorScheme.primary), shape = CardShape)
            },
        ),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = if (flatFocus) Glow.None else Glow(MaterialTheme.colorScheme.primary, 12.dp),
        ),
    ) {
        if (entry.hasBanner) {
            DrawableImage(
                drawable = entry.banner!!,
                contentDescription = entry.label,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DrawableImage(
                    drawable = entry.icon,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                )
                Box(Modifier.size(0.dp, 10.dp))
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
