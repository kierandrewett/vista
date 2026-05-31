package dev.drewett.vista.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.tv.material3.MaterialTheme

/**
 * Adaptive day/night theme. Day = light & bright (projector readability in daylight);
 * night = deep cinematic. Driven by the system uiMode we control elsewhere.
 */
@Composable
fun VistaTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (dark) VistaDarkColors else VistaLightColors,
        typography = VistaTypography,
        content = content,
    )
}
