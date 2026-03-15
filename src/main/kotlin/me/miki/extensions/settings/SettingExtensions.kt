@file:JvmName("SettingExtensions")

package me.miki.extensions.settings

import me.miki.shindo.management.settings.Setting
import me.miki.shindo.management.settings.config.ConfigOwner
import me.miki.shindo.management.settings.impl.*
import me.miki.shindo.management.settings.metadata.SettingRegistry
import me.miki.shindo.management.settings.metadata.SettingRegistry.getSetting

/**
 * TODO: Implement Setting extension helpers.
 *
 * Planned scope:
 * - Owner metadata helpers.
 * - Debug key/path helpers.
 * - Typed filter helpers for collections.
 */

fun Setting.getBooleanSetting(owner: ConfigOwner, fieldName: String): BooleanSetting? {
    return SettingRegistry.getBooleanSetting(owner, fieldName)
}

fun Setting.getNumberSetting(owner: ConfigOwner, fieldName: String): NumberSetting? {
    return SettingRegistry.getNumberSetting(owner, fieldName)
}

fun Setting.getTextSetting(owner: ConfigOwner, fieldName: String): TextSetting? {
    return SettingRegistry.getTextSetting(owner, fieldName)
}

fun Setting.getColorSetting(owner: ConfigOwner, fieldName: String): ColorSetting? {
    return SettingRegistry.getColorSetting(owner, fieldName)
}

fun Setting.getKeybindSetting(owner: ConfigOwner, fieldName: String): KeybindSetting? {
    return getSetting(owner, fieldName, KeybindSetting::class.java)
}

fun Setting.getComboSetting(owner: ConfigOwner, fieldName: String): ComboSetting? {
    return getSetting(owner, fieldName, ComboSetting::class.java)
}


