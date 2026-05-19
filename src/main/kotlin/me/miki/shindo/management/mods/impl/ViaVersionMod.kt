package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.utils.concurrent.TaskExecutor
import me.miki.shindo.utils.concurrent.ThreadPoolType
import me.miki.viashindo.ViaLoadingBase
import me.miki.viashindo.ViaShindo
import me.miki.viashindo.protocolinfo.ProtocolInfo

class ViaVersionMod :
    Mod(
        TranslateText.VIA_VERSION,
        TranslateText.VIA_VERSION_DESCRIPTION,
        ModCategory.OTHER,
        Shinconic.MOD_VIA_VERSION,
    ) {
    private var loaded: Boolean

    init {
        instance = this
        loaded = false
    }

    override fun onEnable() {
        super.onEnable()

        if (!loaded) {
            loaded = true
            TaskExecutor.runAsync(ThreadPoolType.GENERAL) {
                ViaShindo.create()
                ViaShindo.getInstance()!!.initAsyncSlider()
            }
        }
    }

    override fun onDisable() {
        super.onDisable()

        if (loaded) {
            ViaShindo.getInstance().asyncVersionSlider.setVersion(ProtocolInfo.R1_8.protocolVersion.version)
            ViaLoadingBase.getInstance().reload(ProtocolInfo.R1_8.protocolVersion)
        }
    }

    fun isLoaded(): Boolean = loaded

    companion object {
        private lateinit var instance: ViaVersionMod

        @JvmStatic
        fun getInstance(): ViaVersionMod = instance
    }
}
