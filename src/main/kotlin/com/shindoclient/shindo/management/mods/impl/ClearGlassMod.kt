package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventUpdate
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType
import com.shindoclient.shindo.management.settings.impl.BooleanSetting
import com.shindoclient.shindo.management.settings.metadata.SettingRegistry.getBooleanSetting

class ClearGlassMod :
    Mod(
        TranslateText.CLEAR_GLASS,
        TranslateText.CLEAR_GLASS_DESCRIPTION,
        ModCategory.RENDER,
        Shinconic.MOD_CLEAR_GLASS,
    ) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.NORMAL)
    private val normalSetting = true

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.STAINED)
    private val stainedSetting = true

    private var prevNormal = false
    private var prevStained = false

    init {
        instance = this
    }

    @EventTarget
    fun onUpdate(event: EventUpdate?) {
        if (prevNormal != normalSetting) {
            prevNormal = normalSetting
            mc.renderGlobal.loadRenderers()
        }

        if (prevStained != stainedSetting) {
            prevStained = stainedSetting
            mc.renderGlobal.loadRenderers()
        }
    }

    override fun onEnable() {
        prevNormal = normalSetting
        prevStained = stainedSetting
        super.onEnable()
        mc.renderGlobal.loadRenderers()
    }

    override fun onDisable() {
        super.onDisable()
        mc.renderGlobal.loadRenderers()
    }

    fun getNormalSetting(): BooleanSetting? = getBooleanSetting(this, "normalSetting")

    fun getStainedSetting(): BooleanSetting? = getBooleanSetting(this, "stainedSetting")

    companion object {
        @JvmField
        var instance: ClearGlassMod? = null
    }
}
