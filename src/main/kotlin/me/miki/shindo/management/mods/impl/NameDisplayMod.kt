package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyEnum
import me.miki.shindo.management.settings.config.PropertyType

class NameDisplayMod : SimpleHUDMod(TranslateText.NAME_DISPLAY, TranslateText.NAME_DISPLAY_DESCRIPTION, Shinconic.MOD_NAME_DISPLAY) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val iconSetting = true

    @Property(type = PropertyType.COMBO, translate = TranslateText.PREFIX)
    private val prefix = Prefix.NAME

    override fun getText(): String {
        val label =
            if ((prefix) == Prefix.IGN) {
                "Ign"
            } else {
                "Name"
            }

        return label + ": " + mc.session.username
    }

    override fun getIcon(): String? = if (iconSetting) Lucide.USER else null

    private enum class Prefix(
        private val translate: TranslateText,
    ) : PropertyEnum {
        NAME(TranslateText.NAME),
        IGN(TranslateText.IGN),
        ;

        override fun getTranslate(): TranslateText = translate
    }
}
