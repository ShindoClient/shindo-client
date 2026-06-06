package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventText
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType

class NameProtectMod :
    Mod(
        TranslateText.NAME_PROTECT,
        TranslateText.NAME_PROTECT_DESCRIPTION,
        ModCategory.PLAYER,
        Shinconic.MOD_NAME_PROTECT,
        "nickhider",
    ) {
    @Property(type = PropertyType.TEXT, translate = TranslateText.NAME, text = "You")
    private val nameSetting = "You"

    @EventTarget
    fun onText(event: EventText) {
        event.replace(mc.session.username, nameSetting)
    }
}
