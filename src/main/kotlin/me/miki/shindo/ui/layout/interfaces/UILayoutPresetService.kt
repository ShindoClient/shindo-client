package me.miki.shindo.ui.layout.interfaces

import me.miki.shindo.ui.layout.model.UILayoutPreset

/**
 * High-level preset API used by UI scenes and manager facade.
 */
interface UILayoutPresetService {
    fun getPresets(): List<UILayoutPreset>
    fun getActivePreset(): UILayoutPreset?
    fun getActivePresetId(): String?
    fun applyPresetById(id: String): Boolean
    fun saveCurrentAsCustomPreset(
        id: String,
        title: String,
        description: String,
        includeAppearance: Boolean
    ): UILayoutPreset

    fun removeCustomPreset(id: String): Boolean
}

