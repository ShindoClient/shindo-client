package me.miki.shindo.management.addons

import me.miki.shindo.management.addons.patcher.PatcherAddon
import me.miki.shindo.management.addons.rpo.RPOAddon
import me.miki.shindo.management.addons.resourcify.core.ResourcifyAddon
import me.miki.shindo.management.settings.Setting
import me.miki.shindo.management.settings.metadata.SettingRegistry
import me.miki.shindo.utils.Sound
import java.util.ArrayList

class AddonManager {

    val addons = ArrayList<Addon>()
    val settings = ArrayList<Setting>()

    fun init() {
        registerAddon(RPOAddon())
        registerAddon(PatcherAddon())
        registerAddon(ResourcifyAddon())
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

    private fun registerAddon(addon: Addon) {
        addons.add(addon)
        SettingRegistry.applyMetadata(addon)
    }

    fun playToggleSound(toggled: Boolean) {
        if (toggled) {
            Sound.play("shindo/audio/positive.wav", true)
        } else {
            Sound.play("shindo/audio/negative.wav", true)
        }
    }
}
