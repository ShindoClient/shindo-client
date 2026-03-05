package me.miki.shindo.ui.layout.model

import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.ui.layout.enums.UISettingsLayoutMode

/**
 * Canonical snapshot of the layout-related runtime configuration.
 * This model intentionally mirrors settings currently persisted by InternalSettingsMod.
 */
data class UILayoutState(
    val settingsLayoutMode: UISettingsLayoutMode = UISettingsLayoutMode.SINGLE_COLUMN,
    val moduleLayout: InternalSettingsMod.ModuleLayout = InternalSettingsMod.ModuleLayout.SINGLE_COLUMN,
    val notificationCorner: InternalSettingsMod.NotificationCorner = InternalSettingsMod.NotificationCorner.BOTTOM_RIGHT,
    val visualPreset: InternalSettingsMod.VisualPreset = InternalSettingsMod.VisualPreset.MODERN
)
