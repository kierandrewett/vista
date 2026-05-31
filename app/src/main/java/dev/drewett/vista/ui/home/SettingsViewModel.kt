package dev.drewett.vista.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.drewett.vista.data.system.SettingsRepository
import dev.drewett.vista.domain.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)

    val themeMode = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.AUTO)

    fun cycleTheme() {
        val next = when (themeMode.value) {
            ThemeMode.AUTO -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.AUTO
        }
        viewModelScope.launch { settingsRepository.setThemeMode(next) }
    }
}
