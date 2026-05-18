package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType

class AsyncScreenshotMod :
    Mod(
        TranslateText.ASYNC_SCREENSHOT,
        TranslateText.ASYNC_SCREENSHOT_DESCRIPTION,
        ModCategory.OTHER,
        LegacyIcon.MOD_ASYNC_SCREENSHOT,
    ) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.MESSAGE)
    val isMessageEnabled: Boolean = true

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.CLIPBOARD)
    val isClipboardEnabled: Boolean = false

    init {
        instance = this
    }

    companion object {
        @JvmField
        var instance: AsyncScreenshotMod? = null
    }
}
