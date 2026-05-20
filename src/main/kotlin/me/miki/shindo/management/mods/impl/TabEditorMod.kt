package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.management.settings.impl.BooleanSetting
import me.miki.shindo.management.settings.metadata.SettingRegistry.getBooleanSetting

class TabEditorMod : Mod(TranslateText.TAB_EDITOR, TranslateText.TAB_EDITOR_DESCRIPTION, ModCategory.RENDER, Shinconic.MOD_TAB_EDITOR) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.BACKGROUND)
    @JvmField
    var backgroundSetting = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.HEAD)
    @JvmField
    var headSetting = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.PING_NUMBER)
    @JvmField
    var pingSetting = false

    init {
        instance = this
    }

    companion object {
        @JvmField
        var instance: TabEditorMod? = null
    }

    fun getBackgroundSetting(): BooleanSetting? = getBooleanSetting(this, "backgroundSetting")

    fun getHeadSetting(): BooleanSetting? = getBooleanSetting(this, "headSetting")

    fun getPingSetting(): BooleanSetting? = getBooleanSetting(this, "pingSetting")
}
