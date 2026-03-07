package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventUpdate
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.management.settings.impl.BooleanSetting
import me.miki.shindo.management.settings.metadata.SettingRegistry.getBooleanSetting

class ClearGlassMod : Mod(
    TranslateText.CLEAR_GLASS,
    TranslateText.CLEAR_GLASS_DESCRIPTION,
    ModCategory.RENDER,
    LegacyIcon.MOD_CLEAR_GLASS
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

    fun getNormalSetting(): BooleanSetting? {
        return getBooleanSetting(this, "normalSetting")
    }

    fun getStainedSetting(): BooleanSetting? {
        return getBooleanSetting(this, "stainedSetting")
    }

    companion object {
        @JvmField
        var instance: ClearGlassMod? = null
    }
}




