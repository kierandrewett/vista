package dev.drewett.vista.ui

import kotlinx.coroutines.flow.MutableStateFlow

/** Lightweight cross-cutting UI signals. `reset` ticks when HOME is pressed while already home. */
object VistaSignals {
    val reset = MutableStateFlow(0)
    fun reset() {
        reset.value++
    }
}
