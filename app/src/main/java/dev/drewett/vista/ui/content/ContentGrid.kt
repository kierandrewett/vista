package dev.drewett.vista.ui.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import dev.drewett.vista.domain.ContentCard
import dev.drewett.vista.domain.dominantAspectRatio
import dev.drewett.vista.ui.components.ContentCardView

/** A titled grid of content tiles — reused by Movies, Shows and Search results. */
@Composable
fun ContentGrid(
    title: String,
    items: List<ContentCard>,
    onLaunch: (ContentCard) -> Unit,
    modifier: Modifier = Modifier,
    emptyMessage: String = "Nothing here yet",
) {
    Column(modifier.fillMaxSize().padding(top = 96.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 48.dp, bottom = 8.dp),
        )
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
            }
        } else {
            val aspect = items.dominantAspectRatio()
            val cellWidth = if (aspect < 1f) 168.dp else 240.dp
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = cellWidth),
                contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 12.dp, bottom = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                items(items, key = { it.id }) { card ->
                    ContentCardView(
                        card = card,
                        onClick = { onLaunch(card) },
                        modifier = Modifier.fillMaxWidth(),
                        aspectRatio = aspect,
                    )
                }
            }
        }
    }
}
