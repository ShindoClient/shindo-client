package me.miki.shindo.ui.layout.interfaces

import me.miki.shindo.ui.layout.model.UILayoutPreset

/**
 * Storage abstraction for built-in and custom layout presets.
 */
interface UILayoutPresetRepository {
    fun getPresets(): List<UILayoutPreset>
    fun getPresetById(id: String): UILayoutPreset?
    fun saveCustomPreset(preset: UILayoutPreset)
    fun removeCustomPreset(id: String): Boolean
}

