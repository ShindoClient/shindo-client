package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.utils.concurrent.TaskExecutor
import com.shindoclient.shindo.utils.concurrent.ThreadPoolType
import com.shindoclient.viashindo.ViaLoadingBase
import com.shindoclient.viashindo.ViaShindo
import com.shindoclient.viashindo.protocolinfo.ProtocolInfo

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
