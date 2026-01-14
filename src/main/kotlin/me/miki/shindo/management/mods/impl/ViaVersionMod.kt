package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.utils.Multithreading.runAsync
import me.miki.shindo.viaversion.ViaLoadingBase
import me.miki.shindo.viaversion.ViaShindo
import me.miki.shindo.viaversion.protocolinfo.ProtocolInfo


class ViaVersionMod : Mod(
    TranslateText.VIA_VERSION,
    TranslateText.VIA_VERSION_DESCRIPTION,
    ModCategory.OTHER,
    LegacyIcon.MOD_VIA_VERSION
) {
    var isLoaded: Boolean = false
        private set

    init {
        instance = this
    }

    public override fun onEnable() {
        super.onEnable()

        if (!this.isLoaded) {
            this.isLoaded = true
            runAsync(Runnable {
                ViaShindo.create()
                ViaShindo.getInstance().initAsyncSlider()
            })
        }
    }

    public override fun onDisable() {
        super.onDisable()

        if (this.isLoaded) {
            ViaShindo.getInstance().getAsyncVersionSlider()
                .setVersion(ProtocolInfo.R1_8.getProtocolVersion().getVersion())
            ViaLoadingBase.getInstance().reload(ProtocolInfo.R1_8.getProtocolVersion())
        }
    }

    companion object {
        @JvmField
        var instance: ViaVersionMod? = null
    }
}




