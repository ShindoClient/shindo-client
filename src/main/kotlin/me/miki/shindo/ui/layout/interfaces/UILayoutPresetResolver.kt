package me.miki.shindo.ui.layout.interfaces

import me.miki.shindo.ui.layout.model.UILayoutPreset
import me.miki.shindo.ui.layout.model.UILayoutState

/**
 * Resolves which preset currently matches the live layout state.
 */
interface UILayoutPresetResolver {
    fun resolveActivePreset(
        state: UILayoutState,
        themeId: Int?,
        accentName: String?,
        presets: List<UILayoutPreset>
    ): UILayoutPreset?
}

