package me.miki.shindo.management.addons.resourcify.core

import me.miki.shindo.Shindo
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.addons.Addon
import me.miki.shindo.management.addons.AddonType
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventTick
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType

class ResourcifyAddon : Addon(
    "Resourcify",
    "Download and update resource packs and shaders",
    LegacyIcon.DOWNLOAD,
    AddonType.OTHER
) {

    @Property(type = PropertyType.BOOLEAN, name = "Auto Check Updates", category = "General", current = 1.0)
    private var autoCheckUpdatesSetting = true

    @Property(type = PropertyType.TEXT, name = "CurseForge API Key", category = "General", text = "")
    private var curseForgeApiKeySetting = ""

    val manager: ResourcifyManager

    private var lastApiKey = ""

    init {
        instance = this
        val configDir = Shindo.getInstance().fileManager.addonConfigDir
        manager = ResourcifyManager(java.io.File(configDir, "resourcify.json"))
        lastApiKey = curseForgeApiKeySetting
        manager.updateCurseForgeApiKey(curseForgeApiKeySetting)
        ShindoLogger.info("[ADDON] Resourcify initialized")
    }

    override fun onEnable() {
        super.onEnable()
        manager.updateCurseForgeApiKey(curseForgeApiKeySetting)
    }

    @EventTarget
    fun onTick(event: EventTick) {
        if (!isToggled()) return
        if (lastApiKey != curseForgeApiKeySetting) {
            lastApiKey = curseForgeApiKeySetting
            manager.updateCurseForgeApiKey(curseForgeApiKeySetting)
        }
    }

    fun shouldAutoCheckUpdates(): Boolean {
        return autoCheckUpdatesSetting
    }

    companion object {
        private var instance: ResourcifyAddon? = null

        @JvmStatic
        fun getInstance(): ResourcifyAddon? = instance
    }
}
