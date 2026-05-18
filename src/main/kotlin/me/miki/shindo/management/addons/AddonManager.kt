package me.miki.shindo.management.addons

import me.miki.shindo.Shindo
import me.miki.shindo.management.addons.loader.AddonLoader
import me.miki.shindo.management.addons.rpo.RPOAddon
import me.miki.shindo.management.settings.Setting
import me.miki.shindo.management.settings.metadata.SettingRegistry
import me.miki.shindo.management.sound.Sound
import me.miki.shindo.management.sound.Sounds

class AddonManager {
    val addons = ArrayList<Addon>()
    val failedAddons = ArrayList<FailedAddonEntry>()
    val settings = ArrayList<Setting>()

    fun init() {
        registerAddon(RPOAddon())
        AddonLoader.loadExternalAddons(Shindo.getInstance().getFileManager(), this)
    }

    fun getAddonByName(name: String): Addon? {
        for (addon in addons) {
            if (addon.name == name) {
                return addon
            }
        }
        return null
    }

    fun getSettingByAddon(addon: Addon): ArrayList<Setting>? {
        val result = ArrayList<Setting>()
        for (setting in settings) {
            if (setting.parent == addon) {
                result.add(setting)
            }
        }
        return if (result.isEmpty()) null else result
    }

    fun getWords(addon: Addon): String {
        val result = StringBuilder()
        for (entry in addons) {
            if (entry == addon) {
                result.append(entry.name).append(" ")
            }
        }
        return result.toString()
    }

    fun addSettings(vararg settingsList: Setting) {
        settings.addAll(settingsList.asList())
    }

    fun registerAddon(addon: Addon) {
        addons.add(addon)
        SettingRegistry.applyMetadata(addon)
    }

    fun registerFailedAddon(
        jarFileName: String,
        errorMessage: String,
    ) {
        failedAddons.add(FailedAddonEntry(jarFileName, errorMessage))
    }

    fun playToggleSound(toggled: Boolean) {
        if (toggled) {
            Sound.play(Sounds.SHINDO_AUDIO_POSITIVE, true)
        } else {
            Sound.play(Sounds.SHINDO_AUDIO_NEGATIVE, true)
        }
    }
}
