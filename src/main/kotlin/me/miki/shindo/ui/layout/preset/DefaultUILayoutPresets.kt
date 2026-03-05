package me.miki.shindo.ui.layout.preset

import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.ui.layout.enums.UISettingsLayoutMode
import me.miki.shindo.ui.layout.model.UILayoutPreset
import me.miki.shindo.ui.layout.model.UILayoutState

object DefaultUILayoutPresets {

    const val PRESET_MODERN = "layout.modern"
    const val PRESET_LIGHT = "layout.light"
    const val PRESET_DARK = "layout.dark"

    fun createPresets(): List<UILayoutPreset> {
        return listOf(
            UILayoutPreset(
                id = PRESET_MODERN,
                title = "Modern",
                description = "Adaptive contrast and highlight system",
                state = UILayoutState(
                    settingsLayoutMode = UISettingsLayoutMode.STAGGERED_COLUMNS,
                    moduleLayout = InternalSettingsMod.ModuleLayout.TWO_COLUMNS,
                    notificationCorner = InternalSettingsMod.NotificationCorner.BOTTOM_RIGHT,
                    visualPreset = InternalSettingsMod.VisualPreset.MODERN
                )
            ),
            UILayoutPreset(
                id = PRESET_LIGHT,
                title = "Light",
                description = "Global UI tone lifted around fifteen percent",
                state = UILayoutState(
                    settingsLayoutMode = UISettingsLayoutMode.SINGLE_COLUMN,
                    moduleLayout = InternalSettingsMod.ModuleLayout.SINGLE_COLUMN,
                    notificationCorner = InternalSettingsMod.NotificationCorner.TOP_RIGHT,
                    visualPreset = InternalSettingsMod.VisualPreset.LIGHT
                )
            ),
            UILayoutPreset(
                id = PRESET_DARK,
                title = "Dark",
                description = "Global UI tone deepened around fifteen percent",
                state = UILayoutState(
                    settingsLayoutMode = UISettingsLayoutMode.DOUBLE_COLUMN,
                    moduleLayout = InternalSettingsMod.ModuleLayout.TWO_COLUMNS,
                    notificationCorner = InternalSettingsMod.NotificationCorner.BOTTOM_RIGHT,
                    visualPreset = InternalSettingsMod.VisualPreset.DARK
                )
            )
        )
    }
}
