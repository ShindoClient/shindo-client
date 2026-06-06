package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType
import com.shindoclient.shindo.management.settings.impl.BooleanSetting
import com.shindoclient.shindo.management.settings.metadata.SettingRegistry.getBooleanSetting

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
