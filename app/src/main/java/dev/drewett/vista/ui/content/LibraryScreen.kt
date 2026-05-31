package dev.drewett.vista.ui.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import dev.drewett.vista.domain.AppEntry
import dev.drewett.vista.domain.ContentCard
import dev.drewett.vista.ui.components.AppCard
import dev.drewett.vista.ui.components.ContentCardView

@Composable
fun LibraryScreen(
    favourites: List<AppEntry>,
    continueWatching: List<ContentCard>,
    onLaunchApp: (AppEntry) -> Unit,
    onLaunchContent: (ContentCard) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (favourites.isEmpty() && continueWatching.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Pin apps (hold OK on one) and start watching to build your library",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            )
        }
        return
    }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 96.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            "Library",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 48.dp),
        )
        if (continueWatching.isNotEmpty()) {
            SectionLabel("Continue watching")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                items(continueWatching, key = { it.id }) { card ->
                    ContentCardView(card, { onLaunchContent(card) }, Modifier.width(232.dp))
                }
            }
        }
        if (favourites.isNotEmpty()) {
            SectionLabel("Favourites")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                items(favourites, key = { it.packageName }) { app ->
                    AppCard(app, { onLaunchApp(app) }, Modifier.width(232.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(start = 48.dp),
    )
}
