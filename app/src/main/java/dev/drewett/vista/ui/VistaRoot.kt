package dev.drewett.vista.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import android.content.Context
import android.media.AudioManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import dev.drewett.vista.domain.ThemeMode
import dev.drewett.vista.ui.home.HomeScreen
import dev.drewett.vista.ui.quicksettings.QuickSettingsPanel

@Composable
fun VistaRoot(themeMode: ThemeMode, onCycleTheme: () -> Unit) {
    var quickSettingsOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val audio = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                // Android-style focus navigation sounds (respects the system sound setting).
                when (event.key) {
                    Key.DirectionUp -> audio?.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_UP)
                    Key.DirectionDown -> audio?.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_DOWN)
                    Key.DirectionLeft -> audio?.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_LEFT)
                    Key.DirectionRight -> audio?.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_RIGHT)
                    Key.DirectionCenter, Key.Enter -> audio?.playSoundEffect(AudioManager.FX_KEY_CLICK)
                }
                when (event.key) {
                    Key.Menu, Key.Settings -> {
                        quickSettingsOpen = !quickSettingsOpen
                        true
                    }
                    Key.Back -> if (quickSettingsOpen) {
                        quickSettingsOpen = false
                        true
                    } else {
                        false
                    }
                    else -> false
                }
            },
    ) {
        HomeScreen(onOpenQuickSettings = { quickSettingsOpen = true })
        QuickSettingsPanel(
            visible = quickSettingsOpen,
            themeMode = themeMode,
            onCycleTheme = onCycleTheme,
            onDismiss = { quickSettingsOpen = false },
        )
    }
}
