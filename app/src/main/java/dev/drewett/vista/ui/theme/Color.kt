package dev.drewett.vista.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.tv.material3.darkColorScheme
import androidx.tv.material3.lightColorScheme

// Night: deep, cinematic. Glowing accent for focus.
val VistaDarkColors = darkColorScheme(
    primary = Color(0xFF6E9BFF),
    onPrimary = Color(0xFF06122B),
    secondary = Color(0xFF9DB7FF),
    background = Color(0xFF0B0E14),
    onBackground = Color(0xFFE6EAF2),
    surface = Color(0xFF10131C),
    onSurface = Color(0xFFE6EAF2),
    surfaceVariant = Color(0xFF1A1F2B),
    border = Color(0xFF2A3142),
)

// Day: light & bright for projector readability in daylight.
val VistaLightColors = lightColorScheme(
    primary = Color(0xFF2C5BD8),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF3D63C9),
    background = Color(0xFFF4F6FB),
    onBackground = Color(0xFF161A22),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF161A22),
    surfaceVariant = Color(0xFFE6EAF3),
    border = Color(0xFFC7D0E2),
)
