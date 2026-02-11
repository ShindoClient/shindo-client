package me.miki.shindo.ui.layout.interfaces

import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.ui.layout.enums.UIDensityMode
import me.miki.shindo.ui.layout.enums.UISettingsLayoutMode

interface UILayoutConfiguration {
    var settingsLayoutMode: UISettingsLayoutMode
    var settingsDensityMode: UIDensityMode
    var moduleLayout: InternalSettingsMod.ModuleLayout
    var notificationCorner: InternalSettingsMod.NotificationCorner
    var visualPreset: InternalSettingsMod.VisualPreset
}
