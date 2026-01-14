package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventText
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType

class NameProtectMod : Mod(
    TranslateText.NAME_PROTECT,
    TranslateText.NAME_PROTECT_DESCRIPTION,
    ModCategory.PLAYER,
    LegacyIcon.MOD_NAME_PROTECT,
    "nickhider"
) {
    @Property(type = PropertyType.TEXT, translate = TranslateText.NAME, text = "You")
    private val nameSetting = "You"

    @EventTarget
    fun onText(event: EventText) {
        event.replace(mc.getSession().getUsername(), nameSetting)
    }
}




