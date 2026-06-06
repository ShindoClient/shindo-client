package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType

class AsyncScreenshotMod :
    Mod(
        TranslateText.ASYNC_SCREENSHOT,
        TranslateText.ASYNC_SCREENSHOT_DESCRIPTION,
        ModCategory.OTHER,
        Shinconic.MOD_ASYNC_SCREENSHOT,
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
