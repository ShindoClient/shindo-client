package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventGamma
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic

class FullbrightMod : Mod(TranslateText.FULLBRIGHT, TranslateText.FULLBRIGHT_DESCRIPTION, ModCategory.PLAYER, Shinconic.MOD_FULLBRIGHT) {
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
