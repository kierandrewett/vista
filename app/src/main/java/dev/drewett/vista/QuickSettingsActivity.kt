package dev.drewett.vista

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.drewett.vista.domain.ThemeMode
import dev.drewett.vista.ui.home.SettingsViewModel
import dev.drewett.vista.ui.quicksettings.QuickSettingsPanel
import dev.drewett.vista.ui.theme.VistaTheme

/**
 * A translucent activity that shows only the quick-settings panel, launched over whatever app is
 * on screen by [dev.drewett.vista.service.VistaAccessibilityService] when the Settings key is pressed.
 */
class QuickSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
            val dark = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.AUTO -> isSystemInDarkTheme()
            }
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { visible = true }

            VistaTheme(dark = dark) {
                QuickSettingsPanel(
                    visible = visible,
                    themeMode = themeMode,
                    onCycleTheme = settingsViewModel::cycleTheme,
                    onDismiss = { finish() },
                )
            }
        }
    }
}
