package dev.drewett.vista.ui.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.drewett.vista.domain.AppEntry
import dev.drewett.vista.ui.components.AppCard

/** All apps in a focusable grid. Sideloaded phone apps appear here too (Google TV hides them). */
@Composable
fun AppsScreen(
    apps: List<AppEntry>,
    onLaunch: (AppEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 240.dp),
        modifier = modifier.fillMaxSize(),
        // Top inset clears the transparent overlay bar.
        contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 104.dp, bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(apps, key = { it.packageName }) { app ->
            AppCard(entry = app, onClick = { onLaunch(app) })
        }
    }
}
