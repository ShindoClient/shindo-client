package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyEnum
import me.miki.shindo.management.settings.config.PropertyType
import java.util.*

class NameDisplayMod :
    SimpleHUDMod(TranslateText.NAME_DISPLAY, TranslateText.NAME_DISPLAY_DESCRIPTION, LegacyIcon.MOD_NAME_DISPLAY) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val iconSetting = true

    @Property(type = PropertyType.COMBO, translate = TranslateText.PREFIX)
    private val prefix = Prefix.NAME

    public override fun getText(): String? {
        val label: String?

        if (Objects.requireNonNull<Prefix?>(prefix) == Prefix.IGN) {
            label = "Ign"
        } else {
            label = "Name"
        }

        return label + ": " + mc.getSession().getUsername()
    }

    public override fun getIcon(): String? {
        return if (iconSetting) LegacyIcon.USER else null
    }

    private enum class Prefix(private val translate: TranslateText) : PropertyEnum {
        NAME(TranslateText.NAME),
        IGN(TranslateText.IGN);

        override fun getTranslate(): TranslateText {
            return translate
        }
    }
}


