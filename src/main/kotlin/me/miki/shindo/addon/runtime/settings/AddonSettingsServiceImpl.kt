package me.miki.shindo.addon.runtime.settings

import me.miki.shindo.addon.api.settings.AddonSettingsService
import me.miki.shindo.Shindo
import me.miki.shindo.management.addons.Addon
import me.miki.shindo.management.addons.loader.ExternalAddonWrapper
import me.miki.shindo.management.settings.impl.BooleanSetting
import me.miki.shindo.management.settings.impl.KeybindSetting
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementação de [AddonSettingsService] que cria Settings internas para addons
 * externos (wrappers de [ShindoAddon]) para aparecerem no painel de settings.
 */
class AddonSettingsServiceImpl : AddonSettingsService {

    private data class Key(val addonId: String, val key: String)

    private val booleanSettings = ConcurrentHashMap<Key, BooleanSetting>()
    private val keybindSettings = ConcurrentHashMap<Key, KeybindSetting>()

    private fun findAddon(addonId: String): Addon? {
        val manager = Shindo.getInstance().addonManager
        return manager.addons.firstOrNull { addon ->
            addon is ExternalAddonWrapper && addon.getMetadata().id == addonId
        }
    }

    override fun registerBoolean(
        addonId: String,
        key: String,
        displayName: String,
        category: String,
        description: String,
        defaultValue: Boolean
    ) {
        val addon = findAddon(addonId) ?: return
        val mapKey = Key(addonId, key)
        if (booleanSettings.containsKey(mapKey)) return

        val setting = BooleanSetting(displayName, addon, defaultValue)
        booleanSettings[mapKey] = setting
    }

    override fun registerKeybind(
        addonId: String,
        key: String,
        displayName: String,
        category: String,
        description: String,
        defaultKeyCode: Int
    ) {
        val addon = findAddon(addonId) ?: return
        val mapKey = Key(addonId, key)
        if (keybindSettings.containsKey(mapKey)) return

        val setting = KeybindSetting(displayName, addon, defaultKeyCode)
        keybindSettings[mapKey] = setting
    }

    override fun getBoolean(addonId: String, key: String, defaultValue: Boolean): Boolean {
        val setting = booleanSettings[Key(addonId, key)] ?: return defaultValue
        return setting.isToggled()
    }

    override fun getInt(addonId: String, key: String, defaultValue: Int): Int {
        val setting = keybindSettings[Key(addonId, key)] ?: return defaultValue
        return setting.getKeyCode()
    }
}

