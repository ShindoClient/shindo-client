package me.miki.shindo.management.addons.rpo

import me.miki.shindo.Shindo
import me.miki.shindo.management.addons.Addon
import me.miki.shindo.management.addons.AddonType
import me.miki.shindo.management.addons.rpo.repository.ResourcePackRepositoryCustom
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import net.minecraft.client.Minecraft
import java.io.File

class RPOAddon :
    Addon(
        "Resource Pack Organizer",
        "Organizes the resourcepack Screen",
        TranslateText.ADDON_RPO_DESCRIPTION,
        Shinconic.ADDON_RPO,
        AddonType.RENDER,
    ) {
    private var config: ConfigHandler? = null

    fun init() {
        val configDir: File = Shindo.getInstance().getFileManager().addonConfigDir
        config = ConfigHandler(File(configDir, "rpo.json"))

        val enabled: MutableList<String> = ArrayList(config!!.options.getEnabledPacks())

        ResourcePackRepositoryCustom.overrideRepository(enabled)

        Minecraft
            .getMinecraft()
            .gameSettings.resourcePacks
            .clear()
        Minecraft
            .getMinecraft()
            .gameSettings.resourcePacks
            .addAll(enabled)
        Minecraft.getMinecraft().gameSettings.saveOptions()
        Minecraft.getMinecraft().refreshResources()
    }

    fun get(): ConfigHandler {
        if (config == null) {
            val configDir: File = Shindo.getInstance().getFileManager().addonConfigDir
            config = ConfigHandler(File(configDir, "rpo.json"))
        }
        return config!!
    }

    companion object {
        @JvmStatic
        lateinit var instance: RPOAddon
            private set

        init {
        }
    }

    init {
        instance = this
    }
}
