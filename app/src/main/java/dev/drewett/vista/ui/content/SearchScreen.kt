package dev.drewett.vista.ui.content

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import dev.drewett.vista.R
import dev.drewett.vista.domain.AppEntry
import dev.drewett.vista.domain.ContentCard
import dev.drewett.vista.ui.components.AppCard
import dev.drewett.vista.ui.components.ContentCardView

@Composable
fun SearchScreen(
    apps: List<AppEntry>,
    content: List<ContentCard>,
    onLaunchApp: (AppEntry) -> Unit,
    onLaunchContent: (ContentCard) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val fieldFocus = remember { FocusRequester() }
    BackHandler { onDismiss() }
    LaunchedEffect(Unit) { runCatching { fieldFocus.requestFocus() } }

    val q = query.trim().lowercase()
    val appResults = if (q.isEmpty()) emptyList() else apps.filter { it.label.lowercase().contains(q) }
    val contentResults = if (q.isEmpty()) emptyList() else content.filter { it.title.lowercase().contains(q) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 48.dp, end = 48.dp, top = 64.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Search field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(28.dp))
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)),
            )
            Box(Modifier.padding(start = 16.dp).fillMaxWidth()) {
                if (query.isEmpty()) {
                    Text(
                        "Search films, shows, apps and more",
                        style = TextStyle(fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)),
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().focusRequester(fieldFocus),
                )
            }
        }

        if (q.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Type to search your apps and content",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
            }
        } else if (appResults.isEmpty() && contentResults.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No results for \"$query\"",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
            }
        } else {
            if (appResults.isNotEmpty()) {
                Text("Apps", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(appResults, key = { it.packageName }) { app ->
                        AppCard(app, { onLaunchApp(app) }, Modifier.width(220.dp))
                    }
                }
            }
            if (contentResults.isNotEmpty()) {
                Text("Content", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 220.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    contentPadding = PaddingValues(bottom = 40.dp),
                ) {
                    gridItems(contentResults, key = { it.id }) { card ->
                        ContentCardView(card, { onLaunchContent(card) })
                    }
                }
            }
        }
    }
}
