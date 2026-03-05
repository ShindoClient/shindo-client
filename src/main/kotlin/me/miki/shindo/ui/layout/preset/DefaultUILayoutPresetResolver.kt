package me.miki.shindo.ui.layout.preset

import me.miki.shindo.ui.layout.interfaces.UILayoutPresetResolver
import me.miki.shindo.ui.layout.model.UILayoutPreset
import me.miki.shindo.ui.layout.model.UILayoutState

class DefaultUILayoutPresetResolver : UILayoutPresetResolver {

    override fun resolveActivePreset(
        state: UILayoutState,
        themeId: Int?,
        accentName: String?,
        presets: List<UILayoutPreset>
    ): UILayoutPreset? {
        var fallbackStateMatch: UILayoutPreset? = null
        for (preset in presets) {
            if (preset.state != state) {
                continue
            }
            if (fallbackStateMatch == null) {
                fallbackStateMatch = preset
            }

            if (!matchesTheme(preset, themeId)) {
                continue
            }
            if (!matchesAccent(preset, accentName)) {
                continue
            }
            return preset
        }
        return fallbackStateMatch
    }

    private fun matchesTheme(preset: UILayoutPreset, currentThemeId: Int?): Boolean {
        return preset.themeId == null || preset.themeId == currentThemeId
    }

    private fun matchesAccent(preset: UILayoutPreset, currentAccentName: String?): Boolean {
        if (preset.accentName == null) {
            return true
        }
        return preset.accentName.equals(currentAccentName, ignoreCase = true)
    }
}

