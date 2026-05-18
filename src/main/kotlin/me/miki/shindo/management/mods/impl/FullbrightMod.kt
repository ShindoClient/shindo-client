package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventGamma
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon

class FullbrightMod :
    Mod(TranslateText.FULLBRIGHT, TranslateText.FULLBRIGHT_DESCRIPTION, ModCategory.PLAYER, LegacyIcon.MOD_FULLBRIGHT) {
    @EventTarget
    fun onGamma(event: EventGamma) {
        event.setGamma(20f)
    }

    override fun onEnable() {
        super.onEnable()
        mc.renderGlobal.loadRenderers()
    }

    override fun onDisable() {
        super.onDisable()
        mc.renderGlobal.loadRenderers()
    }
}
