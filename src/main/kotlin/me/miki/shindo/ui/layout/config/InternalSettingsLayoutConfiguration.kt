package me.miki.shindo.ui.layout.config

import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.ui.comp.layout.SettingsPanel
import me.miki.shindo.ui.layout.enums.UISettingsLayoutMode
import me.miki.shindo.ui.layout.interfaces.UILayoutConfiguration

class InternalSettingsLayoutConfiguration(
    private val settings: InternalSettingsMod = InternalSettingsMod.instance
) : UILayoutConfiguration {

    override var settingsLayoutMode: UISettingsLayoutMode
        get() = when (settings.settingsLayoutMode ?: SettingsPanel.LayoutMode.SINGLE_COLUMN) {
            SettingsPanel.LayoutMode.DOUBLE_COLUMN -> UISettingsLayoutMode.DOUBLE_COLUMN
            SettingsPanel.LayoutMode.STAGGERED_COLUMNS -> UISettingsLayoutMode.STAGGERED_COLUMNS
            SettingsPanel.LayoutMode.SINGLE_COLUMN -> UISettingsLayoutMode.SINGLE_COLUMN
        }
        set(value) {
            settings.settingsLayoutMode = when (value) {
                UISettingsLayoutMode.DOUBLE_COLUMN -> SettingsPanel.LayoutMode.DOUBLE_COLUMN
                UISettingsLayoutMode.STAGGERED_COLUMNS -> SettingsPanel.LayoutMode.STAGGERED_COLUMNS
                UISettingsLayoutMode.SINGLE_COLUMN -> SettingsPanel.LayoutMode.SINGLE_COLUMN
            }
        }

    override var moduleLayout: InternalSettingsMod.ModuleLayout
        get() = settings.getModuleLayout()
        set(value) {
            settings.setModuleLayout(value)
        }

    override var notificationCorner: InternalSettingsMod.NotificationCorner
        get() = settings.notificationCorner
        set(value) {
            settings.notificationCorner = value
        }

    override var visualPreset: InternalSettingsMod.VisualPreset
        get() = settings.getVisualPreset()
        set(value) {
            settings.setVisualPreset(value)
        }
}
